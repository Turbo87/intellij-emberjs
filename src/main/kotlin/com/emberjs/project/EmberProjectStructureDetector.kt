package com.emberjs.project

import com.intellij.ide.util.importProject.ModuleDescriptor
import com.intellij.ide.util.importProject.ProjectDescriptor
import com.intellij.ide.util.projectWizard.ModuleBuilder
import com.intellij.ide.util.projectWizard.importSources.DetectedProjectRoot
import com.intellij.ide.util.projectWizard.importSources.DetectedSourceRoot
import com.intellij.ide.util.projectWizard.importSources.ProjectFromSourcesBuilder
import com.intellij.ide.util.projectWizard.importSources.ProjectStructureDetector
import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.ModifiableRootModel
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
                    setupModule(rootModel)
                }
            })
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
}
