package com.emberjs.refactoring

import com.intellij.lang.refactoring.RefactoringSupportProvider
import com.intellij.psi.PsiElement

class HbsRefactoringSupportProvider : RefactoringSupportProvider() {
    override fun isSafeDeleteAvailable(element: PsiElement): Boolean {
        return false;
    }

    override fun isMemberInplaceRenameAvailable(element: PsiElement, context: PsiElement?): Boolean {
        return true;
    }

    override fun isInplaceRenameAvailable(element: PsiElement, context: PsiElement?): Boolean {
        return true;
    }

    override fun isInplaceIntroduceAvailable(element: PsiElement, context: PsiElement?): Boolean {
        return true
    }
}
