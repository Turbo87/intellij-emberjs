package com.emberjs.execution

import com.intellij.execution.RunProfileStarter
import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.runners.AsyncGenericProgramRunner
import com.intellij.execution.runners.ExecutionEnvironment
import org.jetbrains.concurrency.Promise

class EmberTestProgramRunner : AsyncGenericProgramRunner<EmberTestRunnerSettings>() {

    override fun canRun(p0: String, p1: RunProfile): Boolean {
        return true
    }

    override fun getRunnerId(): String {
        return "foo"
    }

    override fun prepare(p0: ExecutionEnvironment, p1: RunProfileState): Promise<RunProfileStarter> {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

