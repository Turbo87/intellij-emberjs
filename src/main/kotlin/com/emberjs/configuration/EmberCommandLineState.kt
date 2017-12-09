package com.emberjs.configuration

import com.emberjs.cli.EmberCli
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.process.KillableColoredProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment

class EmberCommandLineState(environment: ExecutionEnvironment) : CommandLineState(environment) {
    override fun startProcess(): ProcessHandler {
        val configuration = (environment.runProfile as EmberConfiguration)
        val argList = configuration.options.toCommandLineOptions()

        val cmd = EmberCli(environment.project, configuration.command, *argList)
                .apply { workDirectory = environment.project.basePath }
                .commandLine()
                .apply {
                    // taken from intellij-rust
                    // @see https://github.com/intellij-rust/intellij-rust/blob/3fe2e01828e4bfce0617a301467da5f61cc33202/src/main/kotlin/org/rust/cargo/toolchain/Cargo.kt#L110
                    withCharset(Charsets.UTF_8)
                    withEnvironment("TERM", "ansi")
                    withRedirectErrorStream(true)
                }

        val handler = KillableColoredProcessHandler(cmd)

        ProcessTerminatedListener.attach(handler)

        return handler
    }
}