package com.emberjs.configuration.utils

import org.jdom.Element

// taken from intellij-rust
// @see https://github.com/intellij-rust/intellij-rust/blob/322011b0a8bede40c10fd57cb7b816004047f405/src/main/kotlin/org/rust/cargo/runconfig/command/CargoCommandConfiguration.kt#L170

class ElementUtils {
    companion object {
        fun writeString(element: Element, elementName: String = "option", value: Any, name: String? = null) {
            val opt = org.jdom.Element(elementName)
            if (name != null) {
                opt.setAttribute("name", name)
            }
            opt.setAttribute("value", value.toString())
            element.addContent(opt)
        }

        fun readString(element: Element, elementName: String = "option", name: String? = null): String? {
            return element.children
                    .find {
                        var matches = it.name == elementName
                        if (name != null) {
                            matches = matches && it.getAttributeValue("name") == name
                        }
                        matches
                    }
                    ?.getAttributeValue("value")

        }

        fun writeBool(element: Element, elementName: String = "option", value: Boolean, name: String? = null) {
            writeString(element, elementName, value.toString(), name)
        }

        fun readBool(element: Element, elementName: String = "option", name: String? = null) = readString(element, elementName, name)?.toBoolean()

        fun removeField(element: Element, elementName: String = "option", name: String? = null) {
            element.children
                    .find {
                        var matches = it.name == elementName
                        if (name != null) {
                            matches = matches && it.getAttributeValue("name") == name
                        }
                        matches
                    }
                    ?.let { it.parent.removeContent(it) }
        }

        /**
         * ENV uses the following structure:
         * <envs>
         *   <env name="FOO" value="BAR" />
         * </envs>
         */
        fun readEnv(element: Element): Map<String, String>? {
            val env = mutableMapOf<String, String>()
            val envs = element.children
                    .find { it.name === "envs" } ?: return null

            envs.let {
                it.children
                        .filter { it.name === "env" }
                        .forEach {
                            env[it.getAttributeValue("name")] = it.getAttributeValue("value")
                        }
            }

            return env;
        }

        fun removeEnv(element: Element) {
            element.children
                    .find { it.name === "envs" }
                    ?.let { it.parentElement.removeContent(it) }
        }

        fun writeEnv(element: Element, map: Map<String, String>) {
            val envs = org.jdom.Element("envs")

            // do nothing if map is empty
            if (map.size == 0) return

            map.forEach {
                val env = org.jdom.Element("env")
                env.setAttribute("name", it.key)
                env.setAttribute("value", it.value)
                envs.addContent(env)
            }

            element.addContent(envs)
        }
    }
}