package com.emberjs.cli

import com.emberjs.utils.waitFor
import com.intellij.execution.configurations.GeneralCommandLine
import java.io.BufferedReader
import java.util.concurrent.TimeUnit

class EmberCli(vararg val parameters: String) {

    var workDirectory: String? = null

    fun run(): BufferedReader {
        val process = GeneralCommandLine("ember")
                .withParameters(*parameters)
                .withWorkDirectory(workDirectory)
                .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
                .createProcess()

        YesThread(process).start()

        if (!process.waitFor(10, TimeUnit.SECONDS)) {
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
                Pair("acceptance-test", "Acceptance Test"),
                Pair("adapter", "Adapter"),
                Pair("adapter-test", "Adapter Test"),
                Pair("component", "Component"),
                Pair("component-test", "Component Test"),
                Pair("controller", "Controller"),
                Pair("controller-test", "Controller Test"),
                Pair("helper", "Helper"),
                Pair("helper-test", "Helper Test"),
                Pair("initializer", "Initializer"),
                Pair("initializer-test", "Initializer Test"),
                Pair("mixin", "Mixin"),
                Pair("mixin-test", "Mixin Test"),
                Pair("model", "Model"),
                Pair("model-test", "Model Test"),
                Pair("route", "Route"),
                Pair("route-test", "Route Test"),
                Pair("serializer", "Serializer"),
                Pair("serializer-test", "Serializer Test"),
                Pair("service", "Service"),
                Pair("service-test", "Service Test"),
                Pair("template", "Template"),
                Pair("test-helper", "Test Helper"),
                Pair("transform", "Transform"),
                Pair("transform-test", "Transform Test"),
                Pair("view", "View"),
                Pair("view-test", "View Test")
        )
    }
}
