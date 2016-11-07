package com.emberjs

import com.emberjs.utils.isEmberFolder
import com.emberjs.utils.parents
import com.intellij.openapi.vfs.VirtualFile

object Ember {
    /**
     * Traverses the `file` parents until it finds root folder of the Ember.js project.
     *
     * This does not stop for in-repo-addon roots.
     */
    fun findProjectFolder(file: VirtualFile) = file.parents.asSequence().firstOrNull { it.isEmberFolder }

    fun findEnvironmentConfigFile(file: VirtualFile) =
            Ember.findProjectFolder(file)?.findFileByRelativePath("config/environment.js")
}

