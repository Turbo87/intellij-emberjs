package com.emberjs.navigation

import com.intellij.navigation.ItemPresentation
import com.intellij.navigation.NavigationItem

class DelegatingNavigationItem(val base: NavigationItem) : NavigationItem {

    private var presentation: ItemPresentation? = null

    fun withPresentation(presentation: ItemPresentation) = apply {
        this.presentation = presentation
    }

    override fun getName() = base.name
    override fun getPresentation() = presentation ?: base.presentation

    override fun navigate(requestFocus: Boolean) = base.navigate(requestFocus)
    override fun canNavigate() = base.canNavigate()
    override fun canNavigateToSource() = base.canNavigateToSource()
}
