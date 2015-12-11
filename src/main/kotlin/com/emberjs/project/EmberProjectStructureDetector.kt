package com.emberjs.project

import com.intellij.ide.util.importProject.ModuleDescriptor
import com.intellij.ide.util.importProject.ProjectDescriptor
import com.intellij.ide.util.projectWizard.importSources.DetectedProjectRoot
import com.intellij.ide.util.projectWizard.importSources.DetectedSourceRoot
import com.intellij.ide.util.projectWizard.importSources.ProjectFromSourcesBuilder
import com.intellij.ide.util.projectWizard.importSources.ProjectStructureDetector
import java.io.File

/**
 * Detects folders with an `app/app.js` file as Ember.js projects
 */
class EmberProjectStructureDetector : ProjectStructureDetector() {

    override fun detectRoots(dir: File, children: Array<File>, base: File, result: MutableList<DetectedProjectRoot>):
            DirectoryProcessingResult {

        if (!hasAppJs(children) && !children.any { it.name == ".ember-cli" })
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
    }
}
