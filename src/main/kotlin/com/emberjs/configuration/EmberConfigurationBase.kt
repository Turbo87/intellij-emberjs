package com.emberjs.configuration

import com.emberjs.configuration.utils.ElementUtils
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import org.jdom.Element

abstract class EmberConfigurationBase(project: Project, factory: ConfigurationFactory, name: String) :
        RunConfigurationBase<Any>(project, factory, name),
        EmberConfiguration {
    override var module: Module? = null

    override fun writeExternal(element: Element) {
        super.writeExternal(element)
        element.let {
            options.fields().forEach { optionsField -> optionsField.writeTo(element) }
        }

        val moduleName = module?.name
        when (moduleName) {
            is String -> ElementUtils.writeString(element, "NAME", moduleName, "module")
            else -> ElementUtils.removeField(element, "NAME", "module")
        }
    }

    override fun readExternal(element: Element) {
        super.readExternal(element)
        element.let {
            options.fields().forEach { optionsField -> optionsField.readFrom(element) }
        }

        ElementUtils.readString(element, "NAME", "module")?.let { moduleString ->
            module = ModuleManager.getInstance(project).modules.find { it.name == moduleString }
        }
    }

}