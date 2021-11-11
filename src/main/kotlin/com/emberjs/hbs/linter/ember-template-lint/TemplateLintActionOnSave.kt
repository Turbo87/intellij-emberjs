import com.intellij.ide.actionsOnSave.impl.ActionsOnSaveFileDocumentManagerListener.ActionOnSave
import com.intellij.lang.javascript.linter.eslint.standardjs.StandardJSConfiguration
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex

class TemplateLintActionOnSave : ActionOnSave() {
    override fun isEnabledForProject(project: Project): Boolean = isFixOnSaveEnabled(project)

    override fun processDocuments(project: Project, documents: Array<out Document>) {
        if (!this.isEnabledForProject(project)) return

        val manager = FileDocumentManager.getInstance()
        val fileIndex = ProjectFileIndex.getInstance(project)
        val files = documents
                .mapNotNull { manager.getFile(it) }
                .filter { it.isInLocalFileSystem && fileIndex.isInContent(it) }
                .toTypedArray()
        if (files.isNotEmpty()) {
            TemplateLintFixAction().processFiles(project, files, false, true)
        }
    }

    companion object {
        fun isFixOnSaveEnabled(project: Project): Boolean {
            val config = TemplateLintConfiguration.getInstance(project)
            return config.isEnabled && config.extendedState.state.isRunOnSave
        }
    }
}
