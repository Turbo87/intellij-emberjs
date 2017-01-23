package com.emberjs.execution

import com.intellij.execution.Executor
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.LocatableConfigurationBase
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project


class EmberTestConfiguration(project: Project, factory: ConfigurationFactory, name: String?) : LocatableConfigurationBase(project, factory, name) {

    override fun getState(p0: Executor, p1: ExecutionEnvironment): RunProfileState? {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

