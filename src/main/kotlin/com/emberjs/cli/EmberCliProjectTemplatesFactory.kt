package com.emberjs.cli

import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.module.WebModuleBuilder
import com.intellij.platform.ProjectTemplate
import com.intellij.platform.ProjectTemplatesFactory

class EmberCliProjectTemplatesFactory : ProjectTemplatesFactory() {
    override fun createTemplates(group: String?, context: WizardContext): Array<out ProjectTemplate> =
            arrayOf(EmberCliProjectGenerator(), EmberCliAddonProjectGenerator())

    override fun getGroups(): Array<String> = arrayOf(WebModuleBuilder.GROUP_NAME)
}
