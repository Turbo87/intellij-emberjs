package com.emberjs.project

import com.emberjs.utils.visitChildrenRecursively
import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lang.javascript.library.JSLibraryManager
import com.intellij.lang.javascript.linter.jshint.JSHintConfiguration
import com.intellij.lang.javascript.linter.jshint.JSHintState
import com.intellij.lang.javascript.settings.JSRootConfiguration
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.AbstractProjectComponent
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ContentEntry
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.VfsUtil.isAncestor
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor
import com.intellij.webcore.ScriptingFrameworkDescriptor
import com.intellij.webcore.libraries.ScriptingLibraryModel.LibraryLevel.PROJECT
import org.jetbrains.jps.model.java.JavaResourceRootType.RESOURCE
import org.jetbrains.jps.model.java.JavaSourceRootType.SOURCE
import org.jetbrains.jps.model.java.JavaSourceRootType.TEST_SOURCE

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

        if (roots.isNotEmpty()) {
            setupProject(project)

            // Add node_modules and bower_components as library folders
            ApplicationManager.getApplication().invokeLater {
                ApplicationManager.getApplication().runWriteAction {
                    roots.forEach {
                        createLibrary("node_modules", project, it)
                        createLibrary("bower_components", project, it)
                    }

                    setupModules(project)
                }
            }
        }
    }

    private fun setupProject(project: Project) {
        // Adjust JavaScript settings for the project
        JSRootConfiguration.getInstance(project)?.apply {
            storeLanguageLevelAndUpdateCaches(JSLanguageLevel.ES6)
        }

        // Enable JSHint
        JSHintConfiguration.getInstance(project).apply {
            setExtendedState(true, JSHintState.Builder(extendedState.state)
                    .setConfigFileUsed(true)
                    .build())
        }
    }

    private fun createLibrary(name: String, project: Project, root: VirtualFile) {
        val folder = root.findChild(name) ?: return

        JSLibraryManager.getInstance(project).apply {
            val libName = "$name ${root.name}"

            if (getLibraryByName(libName) == null) {
                createLibrary(libName, arrayOf(folder), arrayOf(), arrayOf(), PROJECT, true).apply {
                    if (name == "node_modules") {
                        frameworkDescriptor = ScriptingFrameworkDescriptor(name, null)
                    }
                }
            }

            libraryMappings.associateWithProject(libName)
            commitChanges()
        }
    }

    private fun setupModules(project: Project) {
        ModuleManager.getInstance(project).modules.forEach { module ->
            setupModule(module)
        }
    }

    private fun setupModule(module: Module) {
        ModuleRootManager.getInstance(module).modifiableModel.apply {
            contentEntries.forEach { contentEntry ->
                contentEntry.file?.let { moduleRoot ->
                    roots.filter { isAncestor(moduleRoot, it, false) }
                            .filterNotNull()
                            .forEach { setupModule(contentEntry, it.url) }
                }
            }

            commit()
        }
    }

    private fun setupModule(entry: ContentEntry, rootUrl: String) {
        // Mark special folders for each module
        entry.addSourceFolder("$rootUrl/app", SOURCE)
        entry.addSourceFolder("$rootUrl/public", RESOURCE)
        entry.addSourceFolder("$rootUrl/tests", TEST_SOURCE)
        entry.addSourceFolder("$rootUrl/tests/unit", TEST_SOURCE)
        entry.addSourceFolder("$rootUrl/tests/integration", TEST_SOURCE)
        entry.addExcludeFolder("$rootUrl/dist")
        entry.addExcludeFolder("$rootUrl/tmp")
    }

    companion object {
        private val IGNORED_FOLDERS = listOf("node_modules", "bower_components", "dist", "tmp")

        fun getInstance(project: Project) = project.getComponent(EmberProjectComponent::class.java)
    }
}
