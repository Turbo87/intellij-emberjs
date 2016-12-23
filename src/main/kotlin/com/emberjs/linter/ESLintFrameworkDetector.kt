package com.emberjs.linter

import com.emberjs.utils.NotLibrary
import com.intellij.framework.FrameworkType
import com.intellij.framework.detection.DetectedFrameworkDescription
import com.intellij.framework.detection.FileContentPattern
import com.intellij.framework.detection.FrameworkDetectionContext
import com.intellij.framework.detection.FrameworkDetector
import com.intellij.lang.javascript.JavaScriptFileType
import com.intellij.lang.javascript.linter.eslint.EslintConfiguration
import com.intellij.lang.javascript.linter.eslint.EslintUtil
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModifiableModelsProvider
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PatternCondition
import com.intellij.patterns.StandardPatterns
import com.intellij.util.ProcessingContext
import com.intellij.util.indexing.FileContent
import icons.JavaScriptLanguageIcons
import javax.swing.Icon

object ESLintFrameworkType : FrameworkType("ESLint") {
    override fun getIcon(): Icon = JavaScriptLanguageIcons.FileTypes.Eslint
    override fun getPresentableName(): String = "ESLint"
}

class ESLintFrameworkDetector : FrameworkDetector("ESLint") {
    // FIXME: only detects .eslintrc.js, because we can only specify one file type
    override fun getFileType(): FileType = JavaScriptFileType.INSTANCE

    override fun createSuitableFilePattern(): ElementPattern<FileContent> {
        return FileContentPattern.fileContent()
                .withName(StandardPatterns.string().startsWith(".eslintrc"))
                .with(IsEslintConfigFile)
                .with(NotLibrary)
    }

    private object IsEslintConfigFile : PatternCondition<FileContent>("isEslint") {
        override fun accepts(content: FileContent, context: ProcessingContext?): Boolean {
            return EslintUtil.isEslintConfigFile(content.file)
        }
    }

    override fun getFrameworkType(): FrameworkType = ESLintFrameworkType

    override fun detect(newFiles: MutableCollection<VirtualFile>, context: FrameworkDetectionContext): MutableList<out DetectedFrameworkDescription> {
        if (newFiles.isNotEmpty() && !isConfigured(context.project)) {
            return mutableListOf(ESLintFrameworkDescription(newFiles))
        }
        return mutableListOf()
    }

    /** Don't suggest the framework if ESLint is alredy enabled */
    private fun isConfigured(project: Project?): Boolean {
        return project != null && EslintConfiguration.getInstance(project).extendedState.isEnabled
    }

    private inner class ESLintFrameworkDescription(val files: Collection<VirtualFile>) : DetectedFrameworkDescription() {
        override fun getDetector() = this@ESLintFrameworkDetector
        override fun getRelatedFiles() = files
        override fun getSetupText() = "Enable ESLint code quality tool"
        override fun equals(other: Any?) = other is ESLintFrameworkDescription && this.files == other.files
        override fun hashCode() = files.hashCode()

        override fun setupFramework(modifiableModelsProvider: ModifiableModelsProvider, modulesProvider: ModulesProvider) {
            val project = modulesProvider.modules.firstOrNull()?.project ?: return

            EslintConfiguration.getInstance(project).apply {
                setExtendedState(true, extendedState.state)
            }
        }
    }
}