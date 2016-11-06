package com.emberjs.yaml

import com.emberjs.utils.parents
import org.jetbrains.yaml.psi.YAMLDocument
import org.jetbrains.yaml.psi.YAMLKeyValue

val YAMLKeyValue.keyPath: String
    get() = this.parents.takeWhile { it !is YAMLDocument }
            .filterIsInstance(YAMLKeyValue::class.java)
            .let { listOf(this, *it.toTypedArray()) }
            .map { it.keyText }
            .reversed()
            .joinToString(".")
