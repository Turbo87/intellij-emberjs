package com.emberjs.navigation

import com.emberjs.icons.EmberIconProvider
import com.emberjs.icons.EmberIcons
import com.emberjs.resolver.EmberName
import com.intellij.navigation.GotoRelatedItem
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

class EmberGotoRelatedItem(val name: EmberName, element: PsiElement) : GotoRelatedItem(element) {

    override fun getCustomName() = name.displayName
    override fun getCustomContainerName() = ""
    override fun getCustomIcon() = EmberIconProvider().getIcon(name) ?: EmberIcons.EMPTY_16

    companion object {
        fun from(name: EmberName?, psiFile: PsiFile?): EmberGotoRelatedItem? {
            return EmberGotoRelatedItem(name ?: return null, psiFile ?: return null)
        }
    }
}
