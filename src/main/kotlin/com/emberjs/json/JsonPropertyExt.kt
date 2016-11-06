package com.emberjs.json

import com.emberjs.utils.parents
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonProperty

val JsonProperty.keyPath: String
    get() = this.parents.takeWhile { it !is JsonFile }
            .filterIsInstance(JsonProperty::class.java)
            .let { listOf(this, *it.toTypedArray()) }
            .map { it.name }
            .reversed()
            .joinToString(".")
