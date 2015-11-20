package com.emberjs.utils

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

fun Iterable<VirtualFile>.findAppFolder() =
        find { it.name == "app" && it.isDirectory }
