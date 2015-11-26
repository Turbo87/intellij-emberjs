package com.emberjs.project

import com.emberjs.icons.EmberIcons
import com.intellij.icons.AllIcons
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.module.ModuleTypeManager
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

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

        fun findModuleForFile(file: VirtualFile, project: Project): Module? {
            val module = ModuleUtilCore.findModuleForFile(file, project) ?: return null

            return when (ModuleType.get(module)) {
                is EmberModuleType -> module
                else -> null
            }
        }
    }
}
