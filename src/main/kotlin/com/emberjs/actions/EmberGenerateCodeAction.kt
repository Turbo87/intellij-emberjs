package com.emberjs.actions

import com.emberjs.cli.EmberCli
import com.emberjs.cli.EmberCliGenerateTask
import com.emberjs.icons.EmberIcons
import com.emberjs.utils.emberRoot
import com.emberjs.utils.getIdeView
import com.emberjs.utils.hasEmberRoot
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.DumbAwareAction

class EmberGenerateCodeAction() :
        DumbAwareAction("Ember.js Files", "Generates new Ember.js files via ember-cli", EmberIcons.ICON_16) {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val workDir = e.emberRoot ?: return
        val view = e.getIdeView()

        // Prepare blueprint chooser dialog
        val dialog = EmberGenerateCodeDialog(project).apply {
            EmberCli.BLUEPRINTS.forEach { addBlueprint(it.key, it.value) }
            selectedTemplate = DEFAULT_BLUEPRINT
        }

        // Show blueprint chooser dialog
        if (!dialog.showAndGet()) {
            return
        }

        // Save all open documents before running ember-cli
        FileDocumentManager.getInstance().saveAllDocuments()

        // Run ember-cli as modal task
        EmberCliGenerateTask(project, workDir, dialog.selectedTemplate, dialog.enteredName, view).queue()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.hasEmberRoot
    }

    companion object {
        private val DEFAULT_BLUEPRINT = "route"
    }
}

