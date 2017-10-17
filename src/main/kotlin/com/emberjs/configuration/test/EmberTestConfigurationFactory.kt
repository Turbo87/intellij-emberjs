package com.emberjs.configuration.test

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.project.Project

class EmberTestConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {

    override fun createTemplateConfiguration(project: Project): RunConfiguration {
        return EmberTestConfiguration(project, this, "Ember test")
    }

    override fun getName(): String {
        return FACTORY_NAME;
    }

    companion object {
        private val FACTORY_NAME = "Ember test configuration factory"
    }
}