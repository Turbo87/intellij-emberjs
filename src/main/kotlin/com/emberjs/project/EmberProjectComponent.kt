package com.emberjs.project

import com.emberjs.settings.EmberApplicationOptions
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
import com.intellij.util.PlatformUtils
import org.jetbrains.jps.model.JpsElement
import org.jetbrains.jps.model.module.JpsModuleSourceRootType

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
                    file.isEmberFolder -> { roots.add(file); false }

                    // traverse the tree one level deeper
                    else -> true
                }
            }
        })

        // assume the location of in-repo addons; it would be better to parse package.json
        projectRoot.findChild("lib")?.children.orEmpty()
                .filter { it.isInRepoAddon }
                .forEach { roots.add(it) }

        if (roots.isNotEmpty()) {
            // Adjust JavaScript settings for the project
            setES6LanguageLevel(project)

            // Enable JSHint
            if (roots.any { it.findFileByRelativePath(".jshintrc") != null }) {
                enableJSHint(project)
            }

            // Add node_modules and bower_components as library folders
            ApplicationManager.getApplication().invokeLater {
                ApplicationManager.getApplication().runWriteAction {
                    roots.forEach {
                        setupLibraries(it)
                    }

                    setupModules(project)
                }
            }
        }
    }

    private fun enableJSHint(project: Project) {
        JSHintConfiguration.getInstance(project).apply {
            setExtendedState(true, JSHintState.Builder(extendedState.state)
                    .setConfigFileUsed(true)
                    .build())
        }
    }

    private fun setES6LanguageLevel(project: Project) {
        JSRootConfiguration.getInstance(project)?.apply {
            storeLanguageLevelAndUpdateCaches(JSLanguageLevel.ES6)
        }
    }

    private fun setupLibraries(root: VirtualFile) {
        setupLibrary("node_modules", project, root, !EmberApplicationOptions.excludeNodeModules)
        setupLibrary("bower_components", project, root, !EmberApplicationOptions.excludeBowerComponents)
    }

    private fun setupLibrary(name: String, project: Project, root: VirtualFile, create: Boolean) {
        val folder = root.findChild(name) ?: return

        JSLibraryManager.getInstance(project).apply {
            val libName = "$name ${root.name}"

            val library = getLibraryByName(libName)
            if (create && library == null) {
                createLibrary(libName, arrayOf(folder), emptyArray(), emptyArray(), PROJECT, true).apply {
                    if (name == "node_modules") {
                        frameworkDescriptor = ScriptingFrameworkDescriptor(name, null)
                    }
                }
            }
            if (!create && library != null) {
                removeLibrary(library)
            }

            if (create)
                libraryMappings.associateWithProject(libName)
            else
                libraryMappings.disassociateWithProject(libName)

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
        entry.addSourceFolder("$rootUrl/addon", SOURCE)
        entry.addSourceFolder("$rootUrl/public", RESOURCE_IF_AVAILABLE)
        entry.addSourceFolder("$rootUrl/tests", TEST_SOURCE)
        entry.addSourceFolder("$rootUrl/tests/unit", TEST_SOURCE)
        entry.addSourceFolder("$rootUrl/tests/integration", TEST_SOURCE)
        entry.addSourceFolder("$rootUrl/tests/acceptance", TEST_SOURCE)
        entry.addExcludeFolder("$rootUrl/dist")
        entry.addExcludeFolder("$rootUrl/tmp")

        if (EmberApplicationOptions.excludeNodeModules)
            entry.addExcludeFolder("$rootUrl/node_modules")

        if (EmberApplicationOptions.excludeBowerComponents)
            entry.addExcludeFolder("$rootUrl/bower_components")
    }

    private val VirtualFile.isEmberFolder: Boolean
        get() = findFileByRelativePath("app/app.js") != null ||
                findFileByRelativePath(".ember-cli") != null

    private val VirtualFile.isInRepoAddon: Boolean
        get() = isDirectory &&
                findFileByRelativePath("package.json") != null &&
                findFileByRelativePath("index.js") != null

    companion object {
        private val IGNORED_FOLDERS = listOf("node_modules", "bower_components", "dist", "tmp")

        private val RESOURCE_IF_AVAILABLE: JpsModuleSourceRootType<out JpsElement> = when {
            PlatformUtils.isIntelliJ() -> RESOURCE
            else -> SOURCE // Using RESOURCE will fail on PHPStorm and WebStorm
        }

        fun getInstance(project: Project) = project.getComponent(EmberProjectComponent::class.java)
    }
}
