package com.emberjs.configuration.test

import com.emberjs.configuration.EmberConfigurationBase
import com.emberjs.configuration.test.ui.EmberTestSettingsEditor
import com.intellij.execution.ExecutionException
import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

class EmberTestConfiguration(project: Project, factory: ConfigurationFactory, name: String) : EmberConfigurationBase(project, factory, name) {
    override val options = EmberTestOptions()
    override val command: String = "test"

    @NotNull
    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        return EmberTestSettingsEditor(project)
    }

    @Nullable
    @Throws(ExecutionException::class)
    override fun getState(@NotNull executor: Executor, @NotNull executionEnvironment: ExecutionEnvironment): RunProfileState? {
        return EmberTestCommandLineState(executionEnvironment)
    }
}