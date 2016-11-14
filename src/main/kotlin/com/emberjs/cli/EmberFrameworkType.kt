package com.emberjs.cli

import com.emberjs.icons.EmberIcons
import com.intellij.framework.FrameworkType
import javax.swing.Icon

object EmberFrameworkType : FrameworkType("Ember") {
    override fun getIcon(): Icon = EmberIcons.ICON_16
    override fun getPresentableName(): String = "Ember.js"
}