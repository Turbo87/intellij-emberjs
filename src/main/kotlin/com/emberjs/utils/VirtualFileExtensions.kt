package com.emberjs.utils

import com.google.gson.stream.JsonReader
import com.intellij.javascript.nodejs.PackageJsonData
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.text.CharSequenceReader
import java.io.IOException

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


val cache = HashMap<String, Boolean>()

val VirtualFile.isEmberAddonFolder: Boolean

    get() {
        if (cache.contains(this.path)) return cache.getOrDefault(this.path, false)
        val packageJsonFile = findFileByRelativePath("package.json") ?: return false
        val text: String
        try {
            text = String(packageJsonFile.contentsToByteArray())
            val reader = JsonReader(CharSequenceReader(text))
            reader.isLenient = true
            reader.beginObject()
            while (reader.hasNext()) {
                val key = reader.nextName()
                if (key == "keywords") {
                    reader.beginArray()
                    while (reader.hasNext()) {
                        if (reader.nextString() == "ember-addon") {
                            cache[this.path] = true
                            return true
                        }
                    }
                    cache[this.path] = false
                    return false
                }
                reader.skipValue()
            }
            cache[this.path] = false
            return false
        } catch (var3: IOException) {
            return false
        }
    }


val VirtualFile.isEmberFolder: Boolean
    get() = findFileByRelativePath("app/app.js") != null ||
            findFileByRelativePath(".ember-cli") != null ||
            findFileByRelativePath(".ember-cli.js") != null

val VirtualFile.isInRepoAddon: Boolean
    get() = findFileByRelativePath("package.json") != null &&
            parent?.name == "lib" &&
            parent?.parent?.isEmberFolder == true

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
    get() = this.parentModule?.let { if (it.isEmberFolder || it.isInRepoAddon || it.isEmberAddonFolder) it else null }

fun findMainPackageJsonFile(file: VirtualFile) = file.parents.asSequence()
        .filter { it.isEmberFolder }
        .map { it.findChild("package.json") }
        .firstOrNull { it != null }

fun findMainPackageJson(file: VirtualFile) = findMainPackageJsonFile(file)?.let { PackageJsonData.parse(it, null) }
