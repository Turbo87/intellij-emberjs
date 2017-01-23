package com.emberjs.execution

import com.intellij.execution.configurations.ConfigurationTypeBase
import com.emberjs.icons.EmberIcons.ICON_16

val id = "ember-test-runner"
val name = "Ember Test Suite Runner"
val description = "Ember test runner and reporter"

class EmberTestConfigurationType: ConfigurationTypeBase(id, name, description, ICON_16) {

    init {
        addFactory(EmberTestConfigurationFactory(name, this))
    }

}
