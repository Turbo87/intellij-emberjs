package com.emberjs.intl

import com.emberjs.intl.EmberIntlIndex.getFilesWithKey
import com.emberjs.intl.EmberIntlIndex.getTranslationKeys
import com.emberjs.intl.EmberIntlIndexExtension.Companion.findKeyInFile
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult.createResults
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult
import org.jetbrains.yaml.psi.YAMLFile

class EmberIntlHbsReference(element: PsiElement, range: TextRange) :
        PsiPolyVariantReferenceBase<PsiElement>(element, range, true) {

    private val project = element.project
    private val psiManager: PsiManager by lazy { PsiManager.getInstance(project) }

    override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> = getFilesWithKey(value, project)
            .map { fileToElement(it) }
            .filterNotNull()
            .let(::createResults)

    override fun getVariants(): Array<out Any> = getTranslationKeys(project).toTypedArray()

    private fun fileToElement(file: VirtualFile): PsiElement? {
        if (file.extension != "yaml") return null

        val psiFile = psiManager.findFile(file)
        val yamlFile = psiFile as? YAMLFile ?: return null
        return findKeyInFile(value, yamlFile)
    }
}
