package com.emberjs.project

import com.emberjs.settings.EmberApplicationOptions
import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lang.javascript.library.JSLibraryManager
import com.intellij.lang.javascript.linter.jshint.JSHintConfiguration
import com.intellij.lang.javascript.linter.jshint.JSHintState
import com.intellij.lang.javascript.settings.JSRootConfiguration
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleComponent
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ContentEntry
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.VfsUtil.isAncestor
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.PlatformUtils
import com.intellij.webcore.ScriptingFrameworkDescriptor
import com.intellij.webcore.libraries.ScriptingLibraryModel.LibraryLevel.PROJECT
import org.jetbrains.jps.model.JpsElement
import org.jetbrains.jps.model.java.JavaResourceRootType.RESOURCE
import org.jetbrains.jps.model.java.JavaSourceRootType.SOURCE
import org.jetbrains.jps.model.java.JavaSourceRootType.TEST_SOURCE
import org.jetbrains.jps.model.module.JpsModuleSourceRootType
import com.emberjs.utils.isEmberFolder
import com.emberjs.utils.isInRepoAddon

class EmberModuleComponent(val module: Module) : ModuleComponent {
    override fun getComponentName(): String = this.javaClass.name

    override fun moduleAdded() {
        val roots = ModuleRootManager.getInstance(module).contentRoots
            .filter { it.isEmberFolder }
            .flatMap {
                val inRepoAddons = it.findChild("lib")?.children
                    .orEmpty()
                    .filter { it.isInRepoAddon }

                inRepoAddons.plus(it)
            }

        if (roots.isNotEmpty()) {
            // Adjust JavaScript settings for the project
            setES6LanguageLevel(module.project)

            // Enable JSHint
            if (roots.any { it.findFileByRelativePath(".jshintrc") != null }) {
                enableJSHint(module.project)
            }

            // Add node_modules and bower_components as library folders
            ApplicationManager.getApplication().invokeLater {
                ApplicationManager.getApplication().runWriteAction {
                    roots.forEach {
                        setupLibraries(module.project, it)
                    }

                    setupModule(module, roots)
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

    private fun setupLibraries(project: Project, root: VirtualFile) {
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

    private fun setupModule(module: Module, roots: List<VirtualFile>) {
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

    override fun disposeComponent() {}
    override fun projectClosed() {}
    override fun initComponent() {}
    override fun projectOpened() {}

    companion object {
        private val RESOURCE_IF_AVAILABLE: JpsModuleSourceRootType<out JpsElement> = when {
            PlatformUtils.isIntelliJ() -> RESOURCE
            else -> SOURCE // Using RESOURCE will fail on PHPStorm and WebStorm
        }
    }
}
