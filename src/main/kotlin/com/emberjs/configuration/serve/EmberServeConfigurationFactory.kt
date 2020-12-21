package com.emberjs.configuration.serve

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.project.Project


class EmberServeConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {

    override fun createTemplateConfiguration(project: Project): RunConfiguration {
        return EmberServeConfiguration(project, this, "Ember serve")
    }

    override fun getName(): String {
        return FACTORY_NAME
    }

    override fun getId(): String {
        return "Ember Serve"
    }

    companion object {
        private val FACTORY_NAME = "Ember serve configuration factory"
    }
}
