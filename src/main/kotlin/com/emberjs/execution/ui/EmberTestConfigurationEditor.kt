package com.emberjs.execution.ui

import com.intellij.execution.configurations.RefactoringListenerProvider
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.refactoring.listeners.RefactoringElementListener
import javax.swing.JComponent
import javax.swing.JPanel

class EmberTestConfiguration(project: Project) : SettingsEditor<EmberTestConfiguration>(), RefactoringListenerProvider {

    override fun getRefactoringElementListener(element: PsiElement?): RefactoringElementListener? {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createEditor(): JComponent {
        return JPanel()
    }

    override fun resetEditorFrom(configuration: EmberTestConfiguration) {

    }

    override fun applyEditorTo(configuration: EmberTestConfiguration) {

    }

}
