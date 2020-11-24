package com.emberjs.refactoring

import com.dmarcotte.handlebars.parsing.HbTokenTypes
import com.emberjs.EmberAttrDec
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.refactoring.listeners.RefactoringElementListener
import com.intellij.refactoring.rename.RenamePsiElementProcessor
import com.intellij.usageView.UsageInfo

class RenameHbsIdProcessor : RenamePsiElementProcessor() {
    override fun canProcessElement(element: PsiElement): Boolean {
        val mustacheId = element.elementType == HbTokenTypes.ID && element.prevSibling.elementType != HbTokenTypes.SEP
        val htmlBlockParam = element is EmberAttrDec
        return mustacheId || htmlBlockParam
    }

    override fun renameElement(element: PsiElement, newName: String, usages: Array<out UsageInfo>, listener: RefactoringElementListener?) {
        element
    }

    override fun prepareRenaming(element: PsiElement, newName: String,
                                 allRenames: MutableMap<PsiElement, String>) {
        val x = allRenames
    }

    override fun findCollisions(element: PsiElement,
                                newName: String,
                                allRenames: Map<out PsiElement, String>,
                                result: MutableList<UsageInfo>) {

    }

    override fun isToSearchInComments(element: PsiElement): Boolean {
        return false
    }

    override fun setToSearchInComments(element: PsiElement, enabled: Boolean) {

    }
}
