package com.emberjs.configuration

import org.jdom.Element
import javax.swing.JComponent

abstract class OptionsField<T>(
        val default: T,
        val field: String,
        val cmdlineOptionName: String
) {
    abstract var value : T

    abstract fun writeTo(component: Any)
    abstract fun readFrom(component: Any)
}