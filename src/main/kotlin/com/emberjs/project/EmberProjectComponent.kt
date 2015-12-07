package com.emberjs.project

import com.emberjs.utils.visitChildrenRecursively
import com.intellij.openapi.components.AbstractProjectComponent
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor

/**
 * This class is responsible for looking for folders with an `app/app.js` file on project load
 * and keeping the list of those folders around for other parts of the plugin.
 */
class EmberProjectComponent(val project: Project) : AbstractProjectComponent(project) {

    val roots = arrayListOf<VirtualFile>()

    override fun projectOpened() {
        val projectRoot = project.baseDir ?: return

        projectRoot.visitChildrenRecursively(object : VirtualFileVisitor<Any>() {
            override fun visitFile(file: VirtualFile): Boolean {
                return when {
                    // skip further processing if this is not a folder
                    !file.isDirectory -> false

                    // skip further processing if the folder is blacklisted
                    file.name in IGNORED_FOLDERS -> false

                    // skip further processing and add folder to `roots` list if an `app/app.js` file was found
                    file.findFileByRelativePath("app/app.js") != null -> { roots.add(file); false }

                    // traverse the tree one level deeper
                    else -> true
                }
            }
        })
    }

    companion object {
        private val IGNORED_FOLDERS = listOf("node_modules", "bower_components", "dist", "tmp")

        fun getInstance(project: Project) = project.getComponent(EmberProjectComponent::class.java)
    }
}
