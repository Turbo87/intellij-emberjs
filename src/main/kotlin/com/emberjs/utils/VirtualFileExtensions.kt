package com.emberjs.utils

import com.intellij.javascript.nodejs.PackageJsonData
import com.intellij.openapi.vfs.VirtualFile

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

/**
 * Searches parent paths to find the Ember application, skipping in-repo addons.
 */
val VirtualFile.parentEmberApp: VirtualFile?
    get() = this.parents.find { it.isEmberFolder }

fun findMainPackageJsonFile(file: VirtualFile) = file.parents.asSequence()
        .filter { it.isEmberFolder }
        .map { it.findChild("package.json") }
        .firstOrNull { it != null }

fun findMainPackageJson(file: VirtualFile) = findMainPackageJsonFile(file)?.let { PackageJsonData.parse(it, null) }
