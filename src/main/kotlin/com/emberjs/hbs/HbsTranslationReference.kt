package com.emberjs.hbs

import com.emberjs.intl.EmberTranslationIndex
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult

class HbsTranslationReference(element: PsiElement, range: TextRange) :
        PsiPolyVariantReferenceBase<PsiElement>(element, range, true) {

    private val project = element.project

    override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
        return emptyArray()
    }

    override fun getVariants(): Array<out Any> = EmberTranslationIndex.getTranslationKeys(project).toTypedArray()
}
