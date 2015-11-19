package com.emberjs

import com.intellij.ide.util.projectWizard.ModuleBuilder
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.util.IconLoader

class EmberModuleBuilder : ModuleBuilder() {

    override fun getNodeIcon() = IconLoader.getIcon("/com/emberjs/module.png")

    override fun setupRootModel(modifiableRootModel: ModifiableRootModel) {
    }

    override fun getModuleType() = EmberModuleType.instance
}
