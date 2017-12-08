package com.emberjs.cli

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter
import com.intellij.openapi.project.Project
import org.apache.commons.lang.SystemUtils
import java.io.BufferedReader
import java.util.concurrent.TimeUnit

class EmberCli(val project: Project, vararg val parameters: String) {

    var workDirectory: String? = null

    fun commandLine(): GeneralCommandLine {
        val suffix = when {
            SystemUtils.IS_OS_WINDOWS -> ".cmd"
            else -> ""
        }

        val interpreter = NodeJsInterpreterManager.getInstance(project).default as? NodeJsLocalInterpreter
        val node = interpreter?.interpreterSystemDependentPath ?: "node"

        val workDir = workDirectory
        return GeneralCommandLine(node).apply {
            addParameter("$workDir/node_modules/.bin/ember$suffix")
            addParameters(*parameters)
            withWorkDirectory(workDir)
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
