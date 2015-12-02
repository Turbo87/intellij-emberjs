package com.emberjs.utils

import com.emberjs.project.EmberModuleType
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.module.ModuleUtilCore.findModuleForFile
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectLocator
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

fun VirtualFile.guessProject() = ProjectLocator.getInstance().guessProjectForFile(this)

fun VirtualFile.getModule(project: Project) = findModuleForFile(this, project)

fun VirtualFile.getEmberModule(project: Project) =
        getModule(project)?.let { if (ModuleType.get(it) is EmberModuleType) it else null }
