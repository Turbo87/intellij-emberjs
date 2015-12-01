package com.emberjs.actions

import com.emberjs.cli.EmberCli
import com.emberjs.cli.EmberCliGenerateTask
import com.emberjs.icons.EmberIcons
import com.emberjs.utils.getEmberModule
import com.emberjs.utils.getIdeView
import com.emberjs.utils.hasEmberModule
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileDocumentManager

class EmberGenerateCodeAction() :
        AnAction("Ember.js Code", "Generates new code from blueprints", EmberIcons.ICON_16) {

    override fun actionPerformed(e: AnActionEvent) {
        val module = e.getEmberModule() ?: return
        val view = e.getIdeView()

        // Prepare blueprint chooser dialog
        val dialog = EmberGenerateCodeDialog(module.project).apply {
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
        EmberCliGenerateTask(module, dialog.selectedTemplate, dialog.enteredName, view).queue()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.hasEmberModule()
    }

    companion object {
        private val DEFAULT_BLUEPRINT = "route"
    }
}

