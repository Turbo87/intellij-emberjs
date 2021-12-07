package com.emberjs.cli

import com.emberjs.utils.NotLibrary
import com.intellij.framework.FrameworkType
import com.intellij.framework.detection.DetectedFrameworkDescription
import com.intellij.framework.detection.FileContentPattern
import com.intellij.framework.detection.FrameworkDetectionContext
import com.intellij.framework.detection.FrameworkDetector
import com.intellij.ide.projectView.actions.MarkRootActionBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModifiableModelsProvider
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.patterns.ElementPattern
import com.intellij.util.indexing.FileContent

class EmberCliFrameworkDetector : FrameworkDetector("Ember") {
    /** The `.ember-cli` file is detected as plain text */
    override fun getFileType(): FileType = PlainTextFileType.INSTANCE

    override fun createSuitableFilePattern(): ElementPattern<FileContent> {
        return FileContentPattern.fileContent()
                .withName(".ember-cli")
                .with(NotLibrary)
    }

    override fun getFrameworkType(): FrameworkType = EmberFrameworkType

    override fun detect(newFiles: MutableCollection<out VirtualFile>, context: FrameworkDetectionContext): MutableList<out DetectedFrameworkDescription> {
        val rootDir = newFiles.firstOrNull()?.parent
        if (rootDir != null && !isConfigured(newFiles, context.project)) {
            return mutableListOf(EmberFrameworkDescription(rootDir, newFiles))
        }
        return mutableListOf()
    }

    private fun isConfigured(files: Collection<VirtualFile>, project: Project?): Boolean {
        if (project == null) return false

        return files.any { file ->
            // assume the project has been configured if a /tmp directory is excluded
            val module = ModuleUtilCore.findModuleForFile(file, project) ?: return false
            val excluded = ModuleRootManager.getInstance(module).excludeRootUrls
            return excluded.any { it == file.parent.url + "/tmp" }
        }
    }

    private inner class EmberFrameworkDescription(val root: VirtualFile, val files: Collection<VirtualFile>) : DetectedFrameworkDescription() {
        override fun getDetector() = this@EmberCliFrameworkDetector
        override fun getRelatedFiles() = files
        override fun getSetupText() = "Configure this module for Ember.js development"
        override fun equals(other: Any?) = other is EmberFrameworkDescription && this.files == other.files
        override fun hashCode() = files.hashCode()

        override fun setupFramework(modifiableModelsProvider: ModifiableModelsProvider, modulesProvider: ModulesProvider) {
            modulesProvider.modules
                .filter { ModuleRootManager.getInstance(it).contentRoots.contains(root) }
                .forEach { module ->
                    val model = modifiableModelsProvider.getModuleModifiableModel(module)
                    val entry = MarkRootActionBase.findContentEntry(model, root)
                    if (entry != null) {
                        EmberCliProjectConfigurator.setupEmber(model.project, entry, root)
                        modifiableModelsProvider.commitModuleModifiableModel(model)
                    } else {
                        modifiableModelsProvider.disposeModuleModifiableModel(model)
                    }
                }
        }
    }
}