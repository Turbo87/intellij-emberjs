package com.emberjs.js

import com.emberjs.utils.parents
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.lang.javascript.psi.JSProperty

val JSProperty.keyPath: String
    get() = this.parents.takeWhile { it !is JSFile }
            .filterIsInstance(JSProperty::class.java)
            .let { listOf(this, *it.toTypedArray()) }
            .map { it.name }
            .reversed()
            .joinToString(".")
