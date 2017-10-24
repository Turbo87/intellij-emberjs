package com.emberjs.configuration.serve

import com.emberjs.icons.EmberIcons
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationTypeBase

class EmberServeConfigurationType : ConfigurationTypeBase(
        "EMBER_SERVE_CONFIGURATION",
        "Ember Serve",
        "Ember Serve Configuration",
        EmberIcons.ICON_16
) {
    override fun getConfigurationFactories(): Array<ConfigurationFactory> {
        return arrayOf(EmberServeConfigurationFactory(this))
    }
}