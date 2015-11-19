package com.emberjs.project

import com.emberjs.EmberIcons
import com.intellij.ide.util.projectWizard.ModuleBuilder
import com.intellij.openapi.roots.ModifiableRootModel

class EmberModuleBuilder : ModuleBuilder() {

    override fun getNodeIcon() = EmberIcons.ICON_16

    override fun setupRootModel(modifiableRootModel: ModifiableRootModel) {
    }

    override fun getModuleType() = EmberModuleType.instance
}
