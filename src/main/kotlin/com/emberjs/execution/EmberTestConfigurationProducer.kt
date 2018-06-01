package com.emberjs.execution

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.RunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement

class EmberTestConfigurationProducer(configurationFactory: ConfigurationFactory?) : RunConfigurationProducer<EmberTestConfiguration>(configurationFactory) {

    override fun isConfigurationFromContext(p0: EmberTestConfiguration?, p1: ConfigurationContext?): Boolean {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setupConfigurationFromContext(p0: EmberTestConfiguration?, p1: ConfigurationContext?, p2: Ref<PsiElement>?): Boolean {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

