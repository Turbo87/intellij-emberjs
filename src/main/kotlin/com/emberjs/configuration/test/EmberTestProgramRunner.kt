package com.emberjs.configuration.test

import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.runners.DefaultProgramRunner

class EmberTestProgramRunner : DefaultProgramRunner() {
    override fun getRunnerId(): String = "EmberTestRunner"

    override fun canRun(executorId: String, profile: RunProfile): Boolean =
            executorId == DefaultRunExecutor.EXECUTOR_ID && profile is EmberTestConfiguration
}