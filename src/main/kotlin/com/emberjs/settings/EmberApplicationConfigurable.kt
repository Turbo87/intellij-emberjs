package com.emberjs.settings

import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.ui.VerticalFlowLayout
import com.intellij.ui.components.JBCheckBox
import org.jetbrains.annotations.Nls
import javax.swing.JComponent
import javax.swing.JPanel

class EmberApplicationConfigurable private constructor() : SearchableConfigurable {
    private var excludeNodeModulesCheckbox: JBCheckBox? = null
    private var excludeBowerComponentsCheckbox: JBCheckBox? = null

    init {
        excludeNodeModulesCheckbox = JBCheckBox("Exclude node_modules folder")
        excludeBowerComponentsCheckbox = JBCheckBox("Exclude bower_components folder")
    }

    @Nls
    override fun getDisplayName() = "Ember.js"

    override fun enableSearch(option: String) = null
    override fun getId() = "com.emberjs.settings.application"
    override fun getHelpTopic() = this.id

    override fun createComponent(): JComponent? {
        val panel = JPanel(VerticalFlowLayout())
        panel.add(excludeNodeModulesCheckbox!!)
        panel.add(excludeBowerComponentsCheckbox!!)
        return panel
    }

    override fun isModified(): Boolean {
        return excludeNodeModulesCheckbox!!.isSelected != EmberApplicationOptions.excludeNodeModules ||
                excludeBowerComponentsCheckbox!!.isSelected != EmberApplicationOptions.excludeBowerComponents
    }

    override fun apply() {
        EmberApplicationOptions.excludeNodeModules = excludeNodeModulesCheckbox!!.isSelected
        EmberApplicationOptions.excludeBowerComponents = excludeBowerComponentsCheckbox!!.isSelected
    }

    override fun reset() {
        excludeNodeModulesCheckbox!!.isSelected = EmberApplicationOptions.excludeNodeModules
        excludeBowerComponentsCheckbox!!.isSelected = EmberApplicationOptions.excludeBowerComponents
    }

    override fun disposeUIResources() {
        excludeNodeModulesCheckbox = null
        excludeBowerComponentsCheckbox = null
    }
}
