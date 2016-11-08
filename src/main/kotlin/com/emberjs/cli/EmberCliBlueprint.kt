package com.emberjs.cli

class EmberCliBlueprint(val name: String, val description: String?, val args: List<String>) {
    override fun toString() = name
}
