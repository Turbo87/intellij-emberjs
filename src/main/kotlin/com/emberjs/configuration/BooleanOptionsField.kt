package com.emberjs.configuration

import com.emberjs.configuration.utils.ElementUtils
import org.jdom.Element
import javax.swing.JCheckBox
import javax.swing.JComponent

class BooleanOptionsField(
        default: Boolean,
        field: String,
        cmdlineOptionName: String
) : OptionsField<Boolean>(default, field, cmdlineOptionName) {
    override var value: Boolean = default

    override fun writeToElement(element: Element) {
        ElementUtils.writeBool(element, this.field, this.value)
    }

    override fun readFromElement(element: Element) {
        this.value = ElementUtils.readBool(element, this.field) ?: this.default
    }

    override fun writeToComponent(component: JComponent) {
        when (component) {
            is JCheckBox -> component.isSelected = value
        }
    }

    override fun readFromComponent(component: JComponent) {
        when (component) {
            is JCheckBox -> value = component.isSelected
        }
    }
}