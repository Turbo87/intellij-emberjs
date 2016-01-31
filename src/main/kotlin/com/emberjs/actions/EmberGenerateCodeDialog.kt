package com.emberjs.actions

import com.emberjs.icons.EmberIconProvider
import com.emberjs.icons.EmberIcons
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.openapi.project.Project
import javax.swing.Icon

/**
 * This class is used to expose some of the `protected` properties of the `CreateFileFromTemplateDialog`
 * base class, that would otherwise not be accessible.
 */
class EmberGenerateCodeDialog(project: Project) : CreateFileFromTemplateDialog(project) {

    init {
        title = "Generate Ember.js Files"
        setTemplateKindComponentsVisible(true)
    }

    fun addBlueprint(id: String, name: String = id) {
        val icon = EmberIconProvider.getIcon(id) ?: EmberIcons.EMPTY_16
        kindCombo.addItem(name, icon, id)
    }

    /*
     * overwritten for IntelliJ 14 compat
     */
    override fun doOKAction() {
        processDoNotAskOnOk(OK_EXIT_CODE)

        if (okAction.isEnabled) {
            close(OK_EXIT_CODE)
        }
    }

    var selectedTemplate: String
        get() = kindCombo.selectedName
        set(value) = kindCombo.setSelectedName(value)

    val enteredName: String
        get() {
            return nameField.text.trim { it <= ' ' }.apply {
                nameField.text = this
            }
        }
}
