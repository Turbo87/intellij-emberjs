package com.emberjs.translations

import com.emberjs.translations.EmberTranslationIndices.getFilesWithKey
import com.emberjs.translations.EmberTranslationIndices.getTranslationKeys
import com.intellij.json.psi.JsonFile
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.PsiElementResolveResult.createResults
import org.jetbrains.yaml.psi.YAMLFile

class EmberTranslationHbsReference(element: PsiElement, range: TextRange) :
        PsiPolyVariantReferenceBase<PsiElement>(element, range, true) {

    private val project = element.project
    private val psiManager: PsiManager by lazy { PsiManager.getInstance(project) }

    override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> = getFilesWithKey(value, project)
            .map { fileToElement(it) }
            .filterNotNull()
            .let(::createResults)

    override fun getVariants(): Array<out Any> = getTranslationKeys(project).toTypedArray()

    private fun fileToElement(file: VirtualFile) = psiManager.findFile(file)?.let { fileToElement(it) }

    private fun fileToElement(file: PsiFile) = when (file) {
        is JSFile -> JSPropertyFinder(value).findIn(file)?.value
        is JsonFile -> JsonPropertyFinder(value).findIn(file)?.value
        is YAMLFile -> YAMLKeyValueFinder(value).findIn(file)?.value
        else -> null
    }
}
