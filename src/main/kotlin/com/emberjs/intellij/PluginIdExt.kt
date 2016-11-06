package com.emberjs.intellij

import com.intellij.ide.plugins.PluginManager
import com.intellij.openapi.extensions.PluginId

val PluginId?.isEnabled: Boolean
    get() = PluginManager.getPlugin(this)?.isEnabled ?: false
