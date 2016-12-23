package com.emberjs.linter

import com.emberjs.utils.NotLibrary
import com.intellij.framework.FrameworkType
import com.intellij.framework.detection.DetectedFrameworkDescription
import com.intellij.framework.detection.FileContentPattern
import com.intellij.framework.detection.FrameworkDetectionContext
import com.intellij.framework.detection.FrameworkDetector
import com.intellij.lang.javascript.linter.jshint.JSHintConfiguration
import com.intellij.lang.javascript.linter.jshint.JSHintState
import com.intellij.lang.javascript.linter.jshint.config.JSHintConfigFileType
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModifiableModelsProvider
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.patterns.ElementPattern
import com.intellij.util.indexing.FileContent
import icons.JavaScriptLanguageIcons
import javax.swing.Icon

object JSHintFrameworkType : FrameworkType("JSHint") {
    override fun getIcon(): Icon = JavaScriptLanguageIcons.FileTypes.JsHint
    override fun getPresentableName(): String = "JSHint"
}

class JSHintFrameworkDetector : FrameworkDetector("JSHint") {
    override fun getFileType(): FileType = JSHintConfigFileType.INSTANCE

    override fun createSuitableFilePattern(): ElementPattern<FileContent> {
        return FileContentPattern.fileContent()
                .withName(".jshintrc")
                .with(NotLibrary)
    }

    override fun getFrameworkType(): FrameworkType = JSHintFrameworkType

    override fun detect(newFiles: MutableCollection<VirtualFile>, context: FrameworkDetectionContext): MutableList<out DetectedFrameworkDescription> {
        if (newFiles.isNotEmpty() && !isConfigured(context.project)) {
            return mutableListOf(JSHintFrameworkDescription(newFiles))
        }
        return mutableListOf()
    }

    /** Don't suggest the framework if JSHint is alredy enabled */
    private fun isConfigured(project: Project?): Boolean {
        return project != null && JSHintConfiguration.getInstance(project).extendedState.isEnabled
    }

    private inner class JSHintFrameworkDescription(val files: Collection<VirtualFile>) : DetectedFrameworkDescription() {
        override fun getDetector() = this@JSHintFrameworkDetector
        override fun getRelatedFiles() = files
        override fun getSetupText() = "Enable JSHint code quality tool"
        override fun equals(other: Any?) = other is JSHintFrameworkDescription && this.files == other.files
        override fun hashCode() = files.hashCode()

        override fun setupFramework(modifiableModelsProvider: ModifiableModelsProvider, modulesProvider: ModulesProvider) {
            val project = modulesProvider.modules.firstOrNull()?.project ?: return

            JSHintConfiguration.getInstance(project).apply {
                setExtendedState(true, JSHintState.Builder(extendedState.state)
                        .setConfigFileUsed(true)
                        .build())
            }
        }
    }
}