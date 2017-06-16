package com.emberjs.project

import com.emberjs.icons.EmberIcons
import com.intellij.icons.AllIcons
import com.intellij.openapi.module.ModuleTypeManager
import com.intellij.openapi.module.WebModuleType
import javax.swing.Icon

/**
 * This class exists only for legacy reasons.
 *
 * @deprecated Use the `EmberCliFrameworkDetector` class instead!
 */
class EmberModuleType : WebModuleType() {

    override fun getName() = NAME
    override fun getDescription() = DESCRIPTION

    override fun getNodeIcon(isOpened: Boolean): Icon = AllIcons.Nodes.Module

    companion object {
        val ID = "EMBER_MODULE"
        val NAME = "Ember.js"
        val DESCRIPTION = "Ember.js application module"

        val instance: EmberModuleType
            get() = ModuleTypeManager.getInstance().findByID(ID) as EmberModuleType
    }
}
