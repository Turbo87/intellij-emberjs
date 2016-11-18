package com.emberjs.cli

import com.emberjs.icons.EmberIcons
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter
import com.intellij.lang.javascript.boilerplate.NpmPackageProjectGenerator
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ContentEntry
import com.intellij.openapi.util.Pair
import com.intellij.openapi.vfs.VirtualFile
import org.intellij.lang.annotations.Language
import java.io.File

class EmberCliProjectGenerator : NpmPackageProjectGenerator() {

    override fun getName() = "Ember CLI"

    @Language("HTML")
    override fun getDescription() = "<html>A framework for creating ambitious web applications: <a href=\"http://emberjs.com/\">http://emberjs.com/</a></html>"

    override fun getIcon() = EmberIcons.ICON_16

    override fun packageName() = "ember-cli"
    override fun presentablePackageName() = "Ember &CLI:"

    override fun executable(path: String) = Companion.executable(path)
    override fun generatorArgs(project: Project, baseDir: VirtualFile) = arrayOf("init", "--name=${baseDir.name}")

    override fun filters(project: Project, baseDir: VirtualFile) = arrayOf(EmberCliFilter(project, baseDir.path))

    override fun customizeModule(baseDir: VirtualFile, entry: ContentEntry?) = Unit

    override fun generateProject(project: Project, baseDir: VirtualFile, settings: Pair<NodeJsInterpreter, String>, module: Module) {
        EmberCliProjectConfigurator.setupEmber(project, module, baseDir)
        super.generateProject(project, baseDir, settings, module)
    }

    companion object {
        fun executable(path: String) = "$path${File.separator}bin${File.separator}ember"
    }
}
