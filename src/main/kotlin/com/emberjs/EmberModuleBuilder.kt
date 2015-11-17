package com.emberjs

import com.intellij.ide.util.projectWizard.ModuleBuilder
import com.intellij.openapi.roots.ModifiableRootModel

class EmberModuleBuilder : ModuleBuilder() {

    override fun setupRootModel(modifiableRootModel: ModifiableRootModel) {
    }

    override fun getModuleType() = EmberModuleType.instance
}
