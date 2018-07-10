package com.emberjs.configuration.test

import com.intellij.execution.testframework.TestConsoleProperties
import com.intellij.execution.testframework.sm.runner.OutputLineSplitter
import com.intellij.execution.testframework.sm.runner.OutputToGeneralTestEventsConverter
import com.intellij.openapi.util.Key

class EmberTestOutputToGeneralTestEventsConverter(testFrameworkName: String, consoleProperties: TestConsoleProperties) :
        OutputToGeneralTestEventsConverter(testFrameworkName, consoleProperties) {
    var splitter: OutputLineSplitter

    init {
        splitter = object : OutputLineSplitter(true) {
            override fun onLineAvailable(text: String, outputType: Key<*>, tcLikeFakeOutput: Boolean) {
                processConsistentText(text, outputType, tcLikeFakeOutput)
            }
        }
    }

    override fun process(text: String, outputType: Key<*>) {
        splitter.process(text, outputType)
    }
}