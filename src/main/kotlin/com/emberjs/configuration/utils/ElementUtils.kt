package com.emberjs.configuration.utils

import org.jdom.Element

// taken from intellij-rust
// @see https://github.com/intellij-rust/intellij-rust/blob/322011b0a8bede40c10fd57cb7b816004047f405/src/main/kotlin/org/rust/cargo/runconfig/command/CargoCommandConfiguration.kt#L170

class ElementUtils {
    companion object {
        fun writeString(element: Element, name: String, value: String) {
            val opt = org.jdom.Element("option")
            opt.setAttribute("name", name)
            opt.setAttribute("value", value)
            element.addContent(opt)
        }

        fun readString(element: Element, name: String): String? =
                element.children
                        .find { it.name == "option" && it.getAttributeValue("name") == name }
                        ?.getAttributeValue("value")

        fun writeBool(element: Element, name: String, value: Boolean) {
            writeString(element, name, value.toString())
        }

        fun readBool(element: Element, name: String) = readString(element, name)?.toBoolean()
    }
}