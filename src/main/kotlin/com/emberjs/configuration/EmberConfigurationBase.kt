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
    override var nodeInterpreter: String? = null
    override var env: Map<String, String>? = null

    override fun writeExternal(element: Element) {
        super.writeExternal(element)
        element.let {
            options.fields().forEach { optionsField -> optionsField.writeTo(element) }
        }

        val moduleName = module?.name
        when (moduleName) {
            is String -> ElementUtils.writeString(element, "module", moduleName, "NAME")
            else -> ElementUtils.removeField(element, "module", "NAME")
        }

        when (nodeInterpreter) {
            is String -> ElementUtils.writeString(element, "node-interpreter", nodeInterpreter!!)
            else -> ElementUtils.removeField(element, "node-interpreter")
        }

        when (env) {
            is Map -> ElementUtils.writeEnv(element, env!!)
            else -> ElementUtils.removeEnv(element)
        }
    }

    override fun readExternal(element: Element) {
        super.readExternal(element)
        element.let {
            options.fields().forEach { optionsField -> optionsField.readFrom(element) }
        }

        ElementUtils.readString(element, "module", "NAME")?.let { moduleString ->
            module = ModuleManager.getInstance(project).modules.find { it.name == moduleString }
        }
        ElementUtils.readString(element, "node-interpreter")?.let { elementNodeInterpreter ->
            nodeInterpreter = elementNodeInterpreter
        }
        ElementUtils.readEnv(element)?.let { env = it }
    }

}