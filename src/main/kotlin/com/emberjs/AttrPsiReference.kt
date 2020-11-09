package com.emberjs

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference

class AttrPsiReference(private val element: PsiElement) : PsiReference {
    override fun getElement(): PsiElement {
        return element
    }

    override fun getRangeInElement(): TextRange {
        TODO("Not yet implemented")
    }

    override fun resolve(): PsiElement? {
        return element
    }

    override fun getCanonicalText(): String {
        TODO("Not yet implemented")
    }

    override fun handleElementRename(newElementName: String): PsiElement {
        TODO("Not yet implemented")
    }

    override fun bindToElement(element: PsiElement): PsiElement {
        TODO("Not yet implemented")
    }

    override fun isReferenceTo(element: PsiElement): Boolean {
        TODO("Not yet implemented")
    }

    override fun isSoft(): Boolean {
        return true
    }

}
