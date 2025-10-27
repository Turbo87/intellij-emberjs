package com.emberjs.configuration.test

import com.intellij.execution.testframework.TestConsoleProperties
import com.intellij.execution.testframework.sm.runner.OutputEventSplitter
import com.intellij.execution.testframework.sm.runner.OutputToGeneralTestEventsConverter
import com.intellij.openapi.util.Key

class EmberTestOutputToGeneralTestEventsConverter(testFrameworkName: String, consoleProperties: TestConsoleProperties) :
        OutputToGeneralTestEventsConverter(testFrameworkName, consoleProperties) {

    private val splitter = object : OutputEventSplitter(true) {
        override fun onTextAvailable(text: String, outputType: Key<*>) {
            processConsistentText(text, outputType)
        }
    }

    override fun process(text: String, outputType: Key<*>) {
        splitter.process(text, outputType)
    }
}
