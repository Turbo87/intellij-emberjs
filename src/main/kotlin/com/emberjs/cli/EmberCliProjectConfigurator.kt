package com.emberjs.cli

import com.emberjs.settings.EmberApplicationOptions
import com.emberjs.utils.isEmberFolder
import com.emberjs.utils.isInRepoAddon
import com.intellij.ide.projectView.actions.MarkRootActionBase
import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lang.javascript.library.JSLibraryManager
import com.intellij.lang.javascript.linter.jshint.JSHintConfiguration
import com.intellij.lang.javascript.linter.jshint.JSHintState
import com.intellij.lang.javascript.settings.JSRootConfiguration
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ContentEntry
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.util.Ref
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.DirectoryProjectConfigurator
import com.intellij.util.PlatformUtils
import com.intellij.webcore.ScriptingFrameworkDescriptor
import com.intellij.webcore.libraries.ScriptingLibraryModel.LibraryLevel.PROJECT
import org.jetbrains.jps.model.JpsElement
import org.jetbrains.jps.model.java.JavaResourceRootType.RESOURCE
import org.jetbrains.jps.model.java.JavaSourceRootType.SOURCE
import org.jetbrains.jps.model.java.JavaSourceRootType.TEST_SOURCE
import org.jetbrains.jps.model.module.JpsModuleSourceRootType

class EmberCliProjectConfigurator : DirectoryProjectConfigurator {
    override fun configureProject(project: Project, baseDir: VirtualFile, moduleRef: Ref<Module>) {
        val module = ModuleManager.getInstance(project).modules.singleOrNull()
        System.out.println("configureProject: $module ${baseDir.isEmberFolder}")
        if (module != null && baseDir.isEmberFolder) {
            setupEmber(project, module, baseDir)
        }
    }

    companion object {
        fun setupEmber(project: Project, module: Module, baseDir: VirtualFile) {
            val model = ModuleRootManager.getInstance(module).modifiableModel
            val entry = MarkRootActionBase.findContentEntry(model, baseDir)
            System.out.println("setupEmber $entry $baseDir $model")
            if (entry != null) {
                ApplicationManager.getApplication().runWriteAction {
                    setupEmber(project, entry, baseDir)
                    model.commit()
                    project.save()
                }
            } else {
                model.dispose()
            }
        }

        fun setupEmber(project: Project, entry: ContentEntry, root: VirtualFile) {
            // Adjust JavaScript settings for the project
            setES6LanguageLevel(project)

            // Add node_modules and bower_components as library folders
            setupLibraries(project, root)

            // Mark source and exclude directories
            setupModule(entry, root)
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
            System.out.println("setupLibrary: $name $create ${root.findChild(name)}")
            val folder = root.findChild(name) ?: return

            JSLibraryManager.getInstance(project).apply {
                val libName = "$name ${root.name}"
                System.out.println("checking: $libName $create")

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

                if (create) {
                    System.out.println("lib associateWithProject: " + libName)
                    libraryMappings.associateWithProject(libName)
                }
                else
                    libraryMappings.disassociateWithProject(libName)

                commitChanges()
            }
        }

        fun inRepoAddons(baseDir: VirtualFile): List<VirtualFile> {
            // assume the location of in-repo addons; it would be better to parse package.json
            return baseDir.findChild("lib")?.children.orEmpty().filter { it.isInRepoAddon }
        }

        private fun setupModule(entry: ContentEntry, baseDir: VirtualFile) {
            // Mark special folders for each module
            val rootUrl = baseDir.url
            entry.addSourceFolder("$rootUrl/app", SOURCE)
            entry.addSourceFolder("$rootUrl/addon", SOURCE)
            entry.addSourceFolder("$rootUrl/public", RESOURCE_IF_AVAILABLE)
            entry.addSourceFolder("$rootUrl/tests", TEST_SOURCE)
            entry.addSourceFolder("$rootUrl/tests/unit", TEST_SOURCE)
            entry.addSourceFolder("$rootUrl/tests/integration", TEST_SOURCE)
            entry.addSourceFolder("$rootUrl/tests/acceptance", TEST_SOURCE)
            entry.addSourceFolder("$rootUrl/tests/dummy/app", TEST_SOURCE)
            entry.addExcludeFolder("$rootUrl/dist")
            entry.addExcludeFolder("$rootUrl/tmp")
            entry.addExcludeFolder("$rootUrl/.bower_components.ember-try")
            entry.addExcludeFolder("$rootUrl/.node_modules.ember-try")

            inRepoAddons(baseDir).forEach { entry.addSourceFolder("${it.url}/app", SOURCE) }

            if (EmberApplicationOptions.excludeNodeModules)
                entry.addExcludeFolder("$rootUrl/node_modules")

            if (EmberApplicationOptions.excludeBowerComponents)
                entry.addExcludeFolder("$rootUrl/bower_components")
        }

        private val RESOURCE_IF_AVAILABLE: JpsModuleSourceRootType<out JpsElement> = when {
            PlatformUtils.isIntelliJ() -> RESOURCE
            else -> SOURCE // Using RESOURCE will fail on PHPStorm and WebStorm
        }
    }
}
