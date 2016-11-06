package com.emberjs.utils

import com.emberjs.project.EmberModuleType
import com.intellij.javascript.nodejs.PackageJsonData
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.module.ModuleUtilCore.findModuleForFile
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectLocator
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor

val VirtualFile.parents: Iterable<VirtualFile>
    get() = object : Iterable<VirtualFile> {
        override fun iterator(): Iterator<VirtualFile> {
            var file = this@parents

            return object : Iterator<VirtualFile> {
                override fun hasNext() = file.parent != null
                override fun next(): VirtualFile {
                    file = file.parent
                    return file
                }
            }
        }
    }

fun VirtualFile.guessProject() = ProjectLocator.getInstance().guessProjectForFile(this)

fun VirtualFile.getModule(project: Project) = findModuleForFile(this, project)

fun VirtualFile.getEmberModule(project: Project) =
        getModule(project)?.let { if (ModuleType.get(it) is EmberModuleType) it else null }

fun VirtualFile.visitChildrenRecursively(visitor: VirtualFileVisitor<Any>) =
        VfsUtil.visitChildrenRecursively(this, visitor)

val VirtualFile.isEmberFolder: Boolean
    get() = findFileByRelativePath("app/app.js") != null ||
            findFileByRelativePath(".ember-cli") != null

val VirtualFile.isInRepoAddon: Boolean
    get() = findFileByRelativePath("package.json") != null &&
            parent.name == "lib" &&
            parent.parent.isEmberFolder

/**
 * Searches all parent paths until it finds a path containing a `package.json` file.
 */
val VirtualFile.parentModule: VirtualFile?
    get() = this.parents.find { it.findChild("package.json") != null }

/**
 * Searches all parent paths until it finds a path containing a `package.json` file and
 * then checks if the package is an Ember CLI project.
 */
val VirtualFile.parentEmberModule: VirtualFile?
    get() = this.parentModule?.let { if (it.isEmberFolder || it.isInRepoAddon) it else null }

fun findMainPackageJsonFile(file: VirtualFile) = file.parents.asSequence()
        .filter { it.isEmberFolder }
        .map { it.findChild("package.json") }
        .firstOrNull { it != null }

fun findMainPackageJson(file: VirtualFile) = findMainPackageJsonFile(file)?.let { PackageJsonData.parse(it, null) }
