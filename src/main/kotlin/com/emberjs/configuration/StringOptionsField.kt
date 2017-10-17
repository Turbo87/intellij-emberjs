package com.emberjs.configuration

import com.emberjs.configuration.utils.ElementUtils
import com.intellij.ui.EditorTextField
import org.jdom.Element
import javax.swing.JComboBox
import javax.swing.JComponent

class StringOptionsField(
        default: String,
        field: String,
        cmdlineOptionName: String
) : OptionsField<String>(default, field, cmdlineOptionName) {

    override var value: String = ""

    override fun writeToElement(element: Element) {
        ElementUtils.writeString(element, this.field, this.value)
    }

    override fun readFromElement(element: Element) {
        this.value = ElementUtils.readString(element, this.field) ?: this.default
    }

    override fun writeToComponent(component: JComponent) {
        when (component) {
            is EditorTextField -> {
                component.text = value
                component.setPlaceholder(default)
            }
            is JComboBox<*> -> component.selectedItem = if (value.isEmpty()) default else value
        }
    }

    override fun readFromComponent(component: JComponent) {
        when (component) {
            is EditorTextField -> value = component.text
            is JComboBox<*> -> value = component.selectedItem.toString()
        }
    }
}