package com.emberjs.configuration.serve

import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.runners.DefaultProgramRunner

class EmberServeProgramRunner : DefaultProgramRunner() {
    override fun getRunnerId(): String = "EmberServeRunner"

    override fun canRun(executorId: String, profile: RunProfile): Boolean =
            executorId == DefaultRunExecutor.EXECUTOR_ID && profile is EmberServeConfiguration
}