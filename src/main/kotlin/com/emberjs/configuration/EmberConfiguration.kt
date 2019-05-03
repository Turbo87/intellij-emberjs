package com.emberjs.configuration

import com.intellij.openapi.module.Module

interface EmberConfiguration {
    val command: String
    val options: EmberOptions
    var module: Module?
    var nodeInterpreter: String?
    var env: Map<String, String>?
}