package com.emberjs.configuration.serve

import com.emberjs.configuration.EmberCommandLineState
import com.emberjs.configuration.EmberConfigurationBase
import com.emberjs.configuration.serve.ui.EmberServeSettingsEditor
import com.intellij.execution.ExecutionException
import com.intellij.execution.Executor
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

class EmberServeConfiguration(project: Project, factory: ConfigurationFactory, name: String) :
        EmberConfigurationBase(project, factory, name) {
    override val options = EmberServeOptions()
    override val command: String = "serve"

    @NotNull
    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        return EmberServeSettingsEditor()
    }

    @Nullable
    @Throws(ExecutionException::class)
    override fun getState(@NotNull executor: Executor, @NotNull executionEnvironment: ExecutionEnvironment): RunProfileState? {
        return EmberCommandLineState(executionEnvironment)
    }
}