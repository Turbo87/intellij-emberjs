package com.emberjs.configuration

import org.jdom.Element
import javax.swing.JComponent

abstract class OptionsField<T>(
        val default: T,
        val field: String,
        val cmdlineOptionName: String
) {
    abstract var value : T
    abstract fun writeToElement(element: Element)
    abstract fun readFromElement(element: Element)

    abstract fun writeToComponent(component: JComponent)
    abstract fun readFromComponent(component: JComponent)
}