package com.emberjs.cli

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class EmberCliAddonProjectGenerator : EmberCliProjectGenerator() {

    override fun getName() = "Ember CLI Addon"
    override fun generatorArgs(project: Project, baseDir: VirtualFile) = arrayOf("init", "--name=${baseDir.name}", "--blueprint=addon")
}
