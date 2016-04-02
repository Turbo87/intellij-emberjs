package com.emberjs.settings

import com.intellij.ide.util.PropertiesComponent


object EmberApplicationOptions {
    private val EXCLUDE_NODE_MODULES = "com.emberjs.settings.excludeNodeModules"
    private val EXCLUDE_BOWER_COMPONENTS = "com.emberjs.settings.excludeBowerComponents"

    private val props = PropertiesComponent.getInstance()

    var excludeNodeModules: Boolean
        get() = props.getBoolean(EXCLUDE_NODE_MODULES, false)
        set(value) = props.setValue(EXCLUDE_NODE_MODULES, value.toString())

    var excludeBowerComponents: Boolean
        get() = props.getBoolean(EXCLUDE_BOWER_COMPONENTS, false)
        set(value) = props.setValue(EXCLUDE_BOWER_COMPONENTS, value.toString())
}
