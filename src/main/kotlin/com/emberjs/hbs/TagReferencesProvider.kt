package com.emberjs.hbs

import com.dmarcotte.handlebars.parsing.HbTokenTypes
import com.dmarcotte.handlebars.psi.HbHash
import com.dmarcotte.handlebars.psi.HbParam
import com.dmarcotte.handlebars.psi.impl.HbOpenBlockMustacheImpl
import com.emberjs.EmberXmlElementDescriptor
import com.emberjs.index.EmberNameIndex
import com.emberjs.resolver.JsOrFileReference
import com.emberjs.utils.EmberUtils
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.Language
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.search.ProjectScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parents
import com.intellij.psi.util.parentsWithSelf
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.util.ProcessingContext

/**
 * this is just to remove leading and trailing `|` from attribute references
 */
fun toAttributeReference(attribute: XmlAttribute): RangedReference? {
    attribute.ownReferences
    val name = attribute.name
    if (name.startsWith("|") || name.endsWith("|")) {
        var range = TextRange(0, name.length)
        if (name.startsWith("|")) {
            range = TextRange(1, range.endOffset)
        }
        if (name.endsWith("|")) {
            range = TextRange(range.startOffset, range.endOffset - 1)
        }
        return RangedReference(attribute, attribute.descriptor!!.declaration!!, range)
    }
    return null
}


class TagReference(element: PsiElement, val target: PsiElement?, val range: TextRange) : PsiReferenceBase<PsiElement>(element) {
    override fun resolve(): PsiElement? {
        return target
    }

    override fun getRangeInElement(): TextRange {
        return range
    }
}

class TagReferencesProvider : PsiReferenceProvider() {

    fun fromNamedYields(tag: XmlTag, name: String): PsiElement? {
        val angleComponents = tag.parents.find {
            it is XmlTag && it.descriptor is EmberXmlElementDescriptor
        } as XmlTag
        val data = (angleComponents.descriptor as EmberXmlElementDescriptor).getReferenceData()
        val tplYields = data.yields

        return tplYields
                .map { it.yieldBlock }
                .filterNotNull()
                .find {
                    it.children.find { it is HbHash && it.hashName == "to" && it.children.last().text.replace(Regex("\"|'"), "") == name} != null
                }
    }

    fun fromLocalBlock(tag: XmlTag, fullName: String): PsiElement? {
        val name = fullName.split(".").first()
        if (name.startsWith(":")) {
            return fromNamedYields(tag, name.removePrefix(":"))
        }
        // find html blocks with attribute |name|
        var refPsi: PsiElement? = null
        val angleBracketBlock: XmlTag? = tag.parentsWithSelf
                .find {
                    it is XmlTag && it.attributes.find {
                        it.text.startsWith("|") && it.text.replace("|", "").split(" ").contains(name)
                    } != null
                } as XmlTag?

        if (angleBracketBlock != null) {
            refPsi = angleBracketBlock.attributes.find {
                it.text.startsWith("|") && it.text.replace("|", "").split(" ").contains(name)
            }
        }

        // find mustache block |params| which has tag as a child
        val hbsView = tag.containingFile.viewProvider.getPsi(Language.findLanguageByID("Handlebars")!!)
        val hbBlockRef = PsiTreeUtil.collectElements(hbsView, { it is HbOpenBlockMustacheImpl })
                .filter { it.text.contains(Regex("\\|.*$name.*\\|")) }
                .map { it.parent }
                .find { block ->
                    block.textRange.contains(tag.textRange)
                }

        val param = hbBlockRef?.children?.firstOrNull()?.children?.find { it.elementType == HbTokenTypes.ID && it.text == name}
        val parts = fullName.split(".")
        var ref = param ?: refPsi
        parts.subList(1, parts.size).forEach { part ->
            ref = EmberUtils.followReferences(ref)
            if (ref is HbParam && ref!!.children[1].text == "hash") {
                ref = ref!!.children.filter { c -> c is HbHash }.find { c -> (c as HbHash).hashName == part }
                if (ref is HbHash) {
                    ref = (ref as HbHash).hashNameElement
                }
            }
        }
        return EmberUtils.followReferences(ref)
    }

    fun fromImports(tag: XmlTag): PsiElement? {
        return EmberUtils.referenceImports(tag, tag.name.split(".").first())
    }

    fun forTag(tag: XmlTag?, fullName: String): PsiElement? {
        if (tag == null) return null
        val local = fromLocalBlock(tag, fullName) ?: fromImports(tag)
        if (local != null) {
            return local
        }

        val internalComponentsFile = PsiFileFactory.getInstance(tag.project).createFileFromText("intellij-emberjs/internal/components-stub", Language.findLanguageByID("TypeScript")!!, this::class.java.getResource("/com/emberjs/external/ember-components.ts").readText())
        val internalComponents = EmberUtils.resolveDefaultExport(internalComponentsFile) as JSObjectLiteralExpression

        if (internalComponents.properties.map { it.name }.contains(tag.name)) {
            val prop = internalComponents.properties.find { it.name == tag.name }
            return (prop?.jsType?.sourceElement as JSReferenceExpression).resolve()
        }

        val project = tag.project
        val scope = ProjectScope.getAllScope(project)
        val psiManager: PsiManager by lazy { PsiManager.getInstance(project) }

        val componentTemplate = // Filter out components that are not related to this project
                EmberNameIndex.getFilteredKeys(scope) { it.isComponentTemplate && it.tagName == tag.name }
                        // Filter out components that are not related to this project
                        .flatMap { EmberNameIndex.getContainingFiles(it, scope) }
                        .mapNotNull { psiManager.findFile(it) }
                        .firstOrNull()

        if (componentTemplate != null) return componentTemplate

        val component = EmberNameIndex.getFilteredKeys(scope) { it.type == "component" && it.tagName == tag.name }
                .flatMap { EmberNameIndex.getContainingFiles(it, scope) }
                .mapNotNull { psiManager.findFile(it) }
                .map { JsOrFileReference(it).resolve() }
                .firstOrNull()

        if (component != null) return component

        return null
    }
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<TagReference> {
        val tag = element as XmlTag
        val parts = tag.name.split(".")
        val references = parts.mapIndexed { index, s ->
            val p = parts.subList(0, index).joinToString(".")
            val fullName = parts.subList(0, index + 1).joinToString(".")
            val elem = forTag(tag, fullName)
            if (elem == null) {
                return@mapIndexed null
            }

            val offset: Int
            if (p.length == 0) {
                // <
                offset = 1
            } else {
                // < and .
                offset = p.length + 2
            }

            val range = TextRange(offset, offset + s.length)
            TagReference(tag, elem, range)
        }
        return references.filterNotNull().toTypedArray()
    }
}
