package com.emberjs.hbs

import com.dmarcotte.handlebars.psi.HbMustache
import com.dmarcotte.handlebars.psi.HbParam
import com.emberjs.index.EmberNameIndex
import com.emberjs.translations.EmberTranslationHbsReferenceProvider
import com.emberjs.utils.*
import com.intellij.openapi.util.TextRange
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.XmlTagPattern
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.xml.XmlAttribute
import com.intellij.util.ProcessingContext

fun filter(element: PsiElement, fn: (PsiElement) -> PsiReference?): PsiReference? {
    if (element.text.contains(".")) {
        return null
    }
    if (HbsLocalReference.createReference(element) != null) {
        return null
    }
    return fn(element)
}

class RangedReference(element: PsiElement, val target: PsiElement?, val range: TextRange) : PsiReferenceBase<PsiElement>(element) {
    override fun resolve(): PsiElement? {
        return target
    }

    override fun getRangeInElement(): TextRange {
        return range
    }
}

class ImportPathReferencesProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<RangedReference> {
        val psiManager: PsiManager by lazy { PsiManager.getInstance(element.project) }
        val path = element.text.substring(1, element.text.lastIndex)
        var resolvedFile: PsiFileSystemItem? = element.originalElement.containingFile.parent
        var parts = path.split("/")
        if (path.startsWith("~")) {
            resolvedFile = psiManager.findDirectory(element.originalVirtualFile!!.parentEmberModule!!)
        }
        if (!path.startsWith("~") && !path.startsWith(".")) {
            resolvedFile = psiManager.findDirectory(element.originalVirtualFile!!.parentEmberModule!!)
            resolvedFile = resolvedFile?.findSubdirectory("node_modules")
        }
        val files = parts.map { s ->
            if (s == "." || s == "~") {
                // do nothing
            }
            else if (s == "..") {
                resolvedFile = resolvedFile?.parent
            } else {
                if (resolvedFile is PsiDirectory) {
                    var dir = resolvedFile as PsiDirectory?
                    if (resolvedFile!!.virtualFile.isEmberAddonFolder || resolvedFile!!.virtualFile.isEmberFolder) {
                        dir = dir?.findSubdirectory("addon") ?: dir?.findSubdirectory("app")
                    }
                    resolvedFile = dir?.findSubdirectory(s) ?: dir?.children?.find { it is PsiFile && it.name.split(".").first() == s } as PsiFileSystemItem?
                            ?: resolvedFile
                } else {
                    resolvedFile = null
                }
            }
            resolvedFile
        }.toMutableList()

        val file: PsiFile?
        if (resolvedFile is PsiDirectory) {
            val resolvedPath = resolvedFile!!.virtualFile.path.replace("addon/", "").replace("app/", "")
            val scope = GlobalSearchScope.allScope(element.project)
            file = EmberNameIndex.getFilteredKeys(scope) { it.importPath.isNotBlank() && resolvedPath.endsWith(it.importPath) }
                    // Filter out files that are not related to this project
                    .flatMap { EmberNameIndex.getContainingFiles(it, scope) }
                    .map { psiManager.findFile(it) }
                    .firstOrNull()
        } else {
            file = resolvedFile as PsiFile?
        }
        files[files.lastIndex] = file ?: files.last()
        return files.mapIndexed() { index, it ->
            val p = parts.subList(0, index).joinToString("/")
            val offset: Int
            if (p.isEmpty()) {
                offset = 1
            } else {
                offset = p.length + 2
            }
            val range = TextRange(offset, offset + parts[index].length)
            RangedReference(element, it, range)
        }.toTypedArray()
    }
}

class ImportNameReferencesProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val names = element.text.split(",")
        val named = names.map {
            if (it.contains(" as ")) {
                it.split(" as ").first()
            } else {
                it
            }
        }
        val mustache = element.parents.find { it is HbMustache }!!
        val path = mustache.children.findLast { it is HbParam }
        val fileRef = path?.children?.get(0)?.children?.get(0)?.references?.lastOrNull()?.resolve()
        if (fileRef is PsiDirectory) {
            return named
                    .map { fileRef.findFile(it) ?: fileRef.findSubdirectory(it) }
                    .filterNotNull()
                    .map { RangedReference(element, it, TextRange(element.text.indexOf(it.name), it.name.length)) }
                    .toTypedArray()
        }
        if (fileRef == null) {
            return emptyArray()
        }
        val ref = EmberUtils.resolveToEmber(fileRef as PsiFile)
        return arrayOf(RangedReference(element, ref, TextRange(0, element.textLength)))
    }
}

class HbsReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        with(registrar) {
            registerReferenceProvider(XmlTagPattern.Capture(), TagReferencesProvider())
            register(PlatformPatterns.psiElement(XmlAttribute::class.java)) { toAttributeReference(it as XmlAttribute) }
            register(HbsPatterns.SIMPLE_MUSTACHE_NAME) { filter(it) { HbsComponentReference(it) } }
            register(HbsPatterns.BLOCK_MUSTACHE_NAME) { filter(it) { HbsComponentReference(it) } }
            register(HbsPatterns.MUSTACHE_ID) { HbsLocalReference.createReference(it) }
            register(HbsPatterns.SIMPLE_MUSTACHE_NAME) { filter(it) { HbsModuleReference(it, "helper") } }
            register(HbsPatterns.SIMPLE_MUSTACHE_NAME) { filter(it) { HbsModuleReference(it, "modifier") } }
            register(HbsPatterns.SUB_EXPR_NAME) { filter(it) { HbsModuleReference(it, "helper") } }
            registerReferenceProvider(HbsPatterns.IMPORT_NAMES, ImportNameReferencesProvider())
            registerReferenceProvider(HbsPatterns.IMPORT_PATH_REF, ImportPathReferencesProvider())
            registerReferenceProvider(HbsPatterns.LINK_TO_BLOCK_TARGET, HbsLinkToReferenceProvider())
            registerReferenceProvider(HbsPatterns.LINK_TO_SIMPLE_TARGET, HbsLinkToReferenceProvider())
            registerReferenceProvider(HbsPatterns.TRANSLATION_KEY, EmberTranslationHbsReferenceProvider())
            registerReferenceProvider(HbsPatterns.TRANSLATION_KEY_IN_SEXPR, EmberTranslationHbsReferenceProvider())
        }
    }

    private fun PsiReferenceRegistrar.register(pattern: ElementPattern<out PsiElement>, fn: (PsiElement) -> PsiReference?) {
        registerReferenceProvider(pattern, object : PsiReferenceProvider() {
            override fun getReferencesByElement(element: PsiElement, context: ProcessingContext) = arrayOf(fn(element)).filterNotNull().toTypedArray()
        })
    }
}

