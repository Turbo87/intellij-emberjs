package com.emberjs.project

import com.intellij.ide.util.importProject.ModuleDescriptor
import com.intellij.ide.util.importProject.ProjectDescriptor
import com.intellij.ide.util.projectWizard.ModuleBuilder
import com.intellij.ide.util.projectWizard.importSources.DetectedProjectRoot
import com.intellij.ide.util.projectWizard.importSources.DetectedSourceRoot
import com.intellij.ide.util.projectWizard.importSources.ProjectFromSourcesBuilder
import com.intellij.ide.util.projectWizard.importSources.ProjectStructureDetector
import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lang.javascript.library.JSLibraryManager
import com.intellij.lang.javascript.linter.jshint.JSHintConfiguration
import com.intellij.lang.javascript.linter.jshint.JSHintState
import com.intellij.lang.javascript.settings.JSRootConfiguration
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.webcore.ScriptingFrameworkDescriptor
import com.intellij.webcore.libraries.ScriptingLibraryModel.LibraryLevel.PROJECT
import org.jetbrains.jps.model.java.JavaResourceRootType.RESOURCE
import org.jetbrains.jps.model.java.JavaSourceRootType.SOURCE
import org.jetbrains.jps.model.java.JavaSourceRootType.TEST_SOURCE
import java.io.File

/**
 * Detects folders with an `app/app.js` file as Ember.js projects
 */
class EmberProjectStructureDetector : ProjectStructureDetector() {

    override fun detectRoots(dir: File, children: Array<File>, base: File, result: MutableList<DetectedProjectRoot>):
            DirectoryProcessingResult {

        if (!hasAppJs(children))
            return DirectoryProcessingResult.PROCESS_CHILDREN

        result.add(object : DetectedSourceRoot(dir, null) {
            override fun getRootTypeName() = "Ember.js"
        })

        return DirectoryProcessingResult.SKIP_CHILDREN
    }

    /**
     * Check if the children array contains an `app` directory with an `app.js` file inside
     */
    private fun hasAppJs(children: Array<File>): Boolean {
        return children
                .find { it.isDirectory && it.name == "app" }
                ?.listFiles { it.isFile && it.name == "app.js" }
                ?.isNotEmpty()
                ?: false
    }

    override fun setupProjectStructure(roots: MutableCollection<DetectedProjectRoot>,
                                       projectDescriptor: ProjectDescriptor,
                                       builder: ProjectFromSourcesBuilder) {
        // Add detected folders as Ember.js modules
        if (projectDescriptor.modules.isEmpty()) {
            projectDescriptor.modules = roots.map {
                ModuleDescriptor(it.directory, EmberModuleType.instance, emptyList())
            }
        }

        // Iterate through modules
        projectDescriptor.modules.forEach { module ->
            module.addConfigurationUpdater(object : ModuleBuilder.ModuleConfigurationUpdater() {
                override fun update(module: Module, rootModel: ModifiableRootModel) {
                    setupProject(module.project)
                    setupModule(rootModel)
                }
            })
        }
    }

    private fun setupProject(project: Project) {
        // Adjust JavaScript settings for the project
        val configuration = JSRootConfiguration.getInstance(project)
        configuration?.storeLanguageLevelAndUpdateCaches(JSLanguageLevel.ES6)

        // Enable JSHint
        val jsHint = JSHintConfiguration.getInstance(project)
        val jsHintState = JSHintState.Builder(jsHint.extendedState.state).setConfigFileUsed(true).build()
        jsHint.setExtendedState(true, jsHintState)

        // Add node_modules and bower_components as library folders
        ApplicationManager.getApplication().invokeLater {
            ApplicationManager.getApplication().runWriteAction {
                createLibrary(project, "node_modules", "node_modules")
                createLibrary(project, "bower_components")
            }
        }
    }

    private fun setupModule(rootModel: ModifiableRootModel) {
        // Mark special folders for each module
        rootModel.contentEntries.forEach { entry ->
            entry.addSourceFolder("${entry.url}/app", SOURCE)
            entry.addSourceFolder("${entry.url}/public", RESOURCE)
            entry.addSourceFolder("${entry.url}/tests", TEST_SOURCE)
            entry.addSourceFolder("${entry.url}/tests/unit", TEST_SOURCE)
            entry.addSourceFolder("${entry.url}/tests/integration", TEST_SOURCE)
            entry.addExcludeFolder("${entry.url}/dist")
            entry.addExcludeFolder("${entry.url}/tmp")
        }
    }

    private fun createLibrary(project: Project, name: String, framework: String? = null) {
        val folder = project.baseDir.findChild(name) ?: return

        JSLibraryManager.getInstance(project).apply {
            createLibrary(name, arrayOf(folder), arrayOf(), arrayOf(), PROJECT, true).apply {
                if (framework != null) {
                    frameworkDescriptor = ScriptingFrameworkDescriptor(framework, null)
                }
            }

            libraryMappings.associateWithProject(name)
            commitChanges()
        }
    }
}
