package com.emberjs.configuration.test

import com.intellij.execution.Executor
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.testframework.TestConsoleProperties
import com.intellij.execution.testframework.sm.SMCustomMessagesParsing
import com.intellij.execution.testframework.sm.runner.OutputToGeneralTestEventsConverter
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties

class EmberTestConsoleProperties(
        configuration: RunConfiguration,
        TEST_FRAMEWORK_NAME: String,
        executor: Executor) : SMTRunnerConsoleProperties(
        configuration,
        TEST_FRAMEWORK_NAME,
        executor
), SMCustomMessagesParsing {
    override fun createTestEventsConverter(testFrameworkName: String, consoleProperties: TestConsoleProperties): OutputToGeneralTestEventsConverter {
        return EmberTestOutputToGeneralTestEventsConverter(testFrameworkName, consoleProperties);
    }
}