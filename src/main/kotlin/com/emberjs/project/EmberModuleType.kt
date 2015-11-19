package com.emberjs.project

import com.emberjs.EmberIcons
import com.intellij.icons.AllIcons
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.module.ModuleTypeManager

class EmberModuleType : ModuleType<EmberModuleBuilder>(EmberModuleType.ID) {

    override fun getName() = NAME
    override fun getDescription() = DESCRIPTION

    override fun getNodeIcon(isOpened: Boolean) = AllIcons.Nodes.Module
    override fun getBigIcon() = EmberIcons.ICON_24

    override fun createModuleBuilder() = EmberModuleBuilder()

    companion object {
        val ID = "EMBER_MODULE"
        val NAME = "Ember.js"
        val DESCRIPTION = "Ember.js application module"

        val instance: EmberModuleType
            get() = ModuleTypeManager.getInstance().findByID(ID) as EmberModuleType
    }
}
