package com.emberjs.configuration

import com.emberjs.configuration.utils.ElementUtils
import com.emberjs.configuration.utils.PublicStringAddEditDeleteListPanel
import com.intellij.ui.EditorTextField
import org.jdom.Element
import javax.swing.ButtonGroup
import javax.swing.JComboBox

class StringOptionsField(
        default: String,
        field: String,
        cmdlineOptionName: String
) : OptionsField<String>(default, field, cmdlineOptionName) {

    override var value: String = ""

    override fun writeTo(component: Any) {
        when (component) {
            is Element -> ElementUtils.writeString(component, this.field, this.value)
            is EditorTextField -> component.apply {
                text = value
                setPlaceholder(default)
            }
            is JComboBox<*> -> component.selectedItem = if (value.isEmpty()) default else value
            is PublicStringAddEditDeleteListPanel -> component.replaceItems(value.split(",").filter { it.isNotEmpty() })
            is ButtonGroup -> {
                component.elements.toList()
                        .find { it.actionCommand == value }
                        ?.let {
                            it.isSelected = true
                            component.setSelected(it.model, true)
                        }
            }
        }
    }

    override fun readFrom(component: Any) {
        when (component) {
            is Element -> value = ElementUtils.readString(component, this.field) ?: this.default
            is EditorTextField -> value = component.text
            is JComboBox<*> -> value = component.selectedItem.toString()
            is PublicStringAddEditDeleteListPanel -> value = component.listItems.joinToString(",")
            is ButtonGroup -> component.selection?.let { value = it.actionCommand }
        }
    }
}