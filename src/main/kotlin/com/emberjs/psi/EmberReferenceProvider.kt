package com.emberjs.psi

import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext

class EmberReferenceProvider(val types: Iterable<String>) : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        return when (element) {
            is JSLiteralExpression -> arrayOf(EmberReference(element, types))
            else -> arrayOf()
        }
    }

    companion object {
        fun forTypes(vararg types: String) = EmberReferenceProvider(types.toList())
    }
}
