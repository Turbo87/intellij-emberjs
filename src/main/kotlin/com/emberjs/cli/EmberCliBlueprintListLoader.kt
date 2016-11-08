package com.emberjs.cli

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.javascript.nodejs.CompletionModuleInfo
import com.intellij.javascript.nodejs.NodeModuleSearchUtil
import com.intellij.javascript.nodejs.NodeSettings
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ModificationTracker
import com.intellij.psi.util.CachedValueProvider.Result
import com.intellij.psi.util.CachedValuesManager

object EmberCliBlueprintListLoader {
    object CacheModificationTracker : ModificationTracker {
        var count: Long = 0
        override fun getModificationCount() = count
    }

    fun load(project: Project): Collection<EmberCliBlueprint> = CachedValuesManager.getManager(project).getCachedValue(project) {
        Result.create(doLoad(project), CacheModificationTracker)
    }

    private fun doLoad(project: Project): Collection<EmberCliBlueprint> {
        val interpreter = NodeJsInterpreterManager.getInstance(project).default
        val node = NodeJsLocalInterpreter.tryCast(interpreter) ?: return emptyList()

        val modules: MutableList<CompletionModuleInfo> = mutableListOf()
        val baseDir = project.baseDir
        NodeModuleSearchUtil.findModulesWithName(modules, "ember-cli", baseDir, NodeSettings.create(node), false)

        val modulePath = modules.firstOrNull()?.virtualFile?.path ?: return emptyList()
        val ember = EmberCliProjectGenerator.executable(modulePath)
        val commandLine = GeneralCommandLine(node.interpreterSystemDependentPath, ember, "generate", "--help")
                .withWorkDirectory(baseDir.path)
        val handler = CapturingProcessHandler(commandLine)
        val output = handler.runProcess()

        if (output.exitCode != 0) return emptyList()

        return EmberCliBlueprintListParser().parse(output.stdout).sortedBy { it.name }
    }
}
