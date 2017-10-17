package com.emberjs.configuration.test

import com.emberjs.configuration.EmberCommandLineState
import com.emberjs.configuration.EmberConfiguration
import com.intellij.execution.DefaultExecutionResult
import com.intellij.execution.ExecutionResult
import com.intellij.execution.Executor
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil
import com.intellij.execution.testframework.autotest.ToggleAutoTestAction
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil.createAndAttachConsole

class EmberTestCommandLineState(environment: ExecutionEnvironment) : EmberCommandLineState(environment) {
    private val TEST_FRAMEWORK_NAME = "ember-qunit"

    override fun execute(executor: Executor, runner: ProgramRunner<*>): ExecutionResult {
        val configuration = (environment.runProfile as EmberConfiguration)

        // enforce teamcity reporter because converter requires it
        (configuration.options as EmberTestOptions).reporter.value = "teamcity"

        val processHandler: ProcessHandler = startProcess()

        val properties = EmberTestConsoleProperties(configuration as RunConfiguration, TEST_FRAMEWORK_NAME, executor)
        val console = createAndAttachConsole(TEST_FRAMEWORK_NAME, processHandler, properties)

        SMTestRunnerConnectionUtil.createAndAttachConsole(TEST_FRAMEWORK_NAME, processHandler, properties)

        val executionResult = DefaultExecutionResult(console, processHandler, *createActions(console, processHandler))
        executionResult.setRestartActions(ToggleAutoTestAction())
        return executionResult
    }
}