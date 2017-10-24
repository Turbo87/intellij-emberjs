package com.emberjs.configuration.serve

import com.emberjs.configuration.EmberCommandLineState
import com.emberjs.configuration.EmberConfiguration
import com.emberjs.configuration.serve.ui.EmberServeSettingsEditor
import com.intellij.execution.ExecutionException
import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import org.jdom.Element
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

class EmberServeConfiguration(project: Project, factory: ConfigurationFactory, name: String) : RunConfigurationBase(project, factory, name), EmberConfiguration {
    override val options = EmberServeOptions()
    override val command: String = "serve"

    @NotNull
    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        return EmberServeSettingsEditor()
    }

    @Throws(RuntimeConfigurationException::class)
    override fun checkConfiguration() {

    }

    @Nullable
    @Throws(ExecutionException::class)
    override fun getState(@NotNull executor: Executor, @NotNull executionEnvironment: ExecutionEnvironment): RunProfileState? {
        return EmberCommandLineState(executionEnvironment)
    }

    override fun writeExternal(element: Element?) {
        super.writeExternal(element)
        element?.let {
            options.fields().forEach { optionsField -> optionsField.writeToElement(element)}
        }
    }

    override fun readExternal(element: Element?) {
        super.readExternal(element)
        element?.let {
            options.fields().forEach { optionsField -> optionsField.readFromElement(element) }
        }
    }
}