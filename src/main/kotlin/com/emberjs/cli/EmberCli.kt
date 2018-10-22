package com.emberjs.cli

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef
import com.intellij.openapi.project.Project
import org.apache.commons.lang.SystemUtils
import java.io.BufferedReader
import java.util.concurrent.TimeUnit

class EmberCli(val project: Project, vararg val parameters: String) {

    var workDirectory: String? = null
    var nodeInterpreter: String? = null

    private fun nodeCommandLine(): GeneralCommandLine {
        val interpreterRef = NodeJsInterpreterRef.create(nodeInterpreter)
        val interpreter = interpreterRef.resolve(project)

        return GeneralCommandLine(interpreter?.referenceName).also {
            it.addParameter("$workDirectory/node_modules/.bin/ember")
        }
    }

    private fun emberBinCommandLine(): GeneralCommandLine {
        val suffix = when {
            SystemUtils.IS_OS_WINDOWS -> ".cmd"
            else -> ""
        }

        val command = "$workDirectory/node_modules/.bin/ember$suffix"

        return GeneralCommandLine(command)
    }

    fun commandLine(): GeneralCommandLine {
        val commandLine = when (nodeInterpreter) {
            is String -> nodeCommandLine()
            else -> emberBinCommandLine()
        }

        return commandLine.also {
            it.addParameters(*parameters)
            it.withWorkDirectory(workDirectory)
        }
    }

    fun run(): BufferedReader {
        val process = commandLine().createProcess()

        YesThread(process).start()

        if (!process.waitFor(30, TimeUnit.SECONDS)) {
            process.destroy()
            throw Exception("Process timed out. Please try again on the command line.")
        }

        val exitValue = process.exitValue()
        if (exitValue != 0) {
            throw Exception("Process did not exit properly (code = $exitValue). Please try again on the command line.")
        }

        return process.inputStream.bufferedReader()
    }

    companion object {
        val BLUEPRINTS = mapOf(
                "acceptance-test" to "Acceptance Test",
                "adapter" to "Adapter",
                "adapter-test" to "Adapter Test",
                "component" to "Component",
                "component-test" to "Component Test",
                "controller" to "Controller",
                "controller-test" to "Controller Test",
                "helper" to "Helper",
                "helper-test" to "Helper Test",
                "initializer" to "Initializer",
                "initializer-test" to "Initializer Test",
                "mixin" to "Mixin",
                "mixin-test" to "Mixin Test",
                "model" to "Model",
                "model-test" to "Model Test",
                "route" to "Route",
                "route-test" to "Route Test",
                "serializer" to "Serializer",
                "serializer-test" to "Serializer Test",
                "service" to "Service",
                "service-test" to "Service Test",
                "template" to "Template",
                "test-helper" to "Test Helper",
                "transform" to "Transform",
                "transform-test" to "Transform Test",
                "view" to "View",
                "view-test" to "View Test"
        )
    }
}
