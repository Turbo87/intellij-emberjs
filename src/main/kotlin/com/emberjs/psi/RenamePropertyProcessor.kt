package com.emberjs.psi

import com.dmarcotte.handlebars.parsing.HbTokenTypes
import com.dmarcotte.handlebars.psi.HbPath
import com.emberjs.EmberAttrDec
import com.intellij.openapi.util.Comparing
import com.intellij.pom.PomTargetPsiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parents
import com.intellij.refactoring.rename.RenamePsiElementProcessor
import com.intellij.refactoring.rename.UnresolvableCollisionUsageInfo
import com.intellij.usageView.UsageInfo
import com.sun.deploy.xml.XMLAttribute
import java.util.ResourceBundle
import kotlin.collections.LinkedHashMap
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.forEach
import kotlin.collections.set


class RenamePropertyProcessor : RenamePsiElementProcessor() {
    override fun canProcessElement(element: PsiElement): Boolean {
        val mustacheId = element.elementType == HbTokenTypes.ID && element.prevSibling.elementType != HbTokenTypes.SEP
        val htmlBlockParam = element is EmberAttrDec
        return mustacheId || htmlBlockParam
    }

    override fun prepareRenaming(element: PsiElement, newName: String,
                                 allRenames: MutableMap<PsiElement, String>) {
        // todo
    }

    override fun findCollisions(element: PsiElement,
                                newName: String,
                                allRenames: Map<out PsiElement, String>,
                                result: MutableList<UsageInfo>) {
        // todo
    }

    override fun isToSearchInComments(element: PsiElement): Boolean {
        return false
    }

    override fun setToSearchInComments(element: PsiElement, enabled: Boolean) {

    }
}
