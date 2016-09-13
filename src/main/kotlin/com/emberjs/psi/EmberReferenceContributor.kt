package com.emberjs.psi

import com.emberjs.patterns.EmberPatterns.inEmberFile
import com.emberjs.patterns.EmberPatterns.jsQuotedArgument
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar

class EmberReferenceContributor : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        PROVIDERS.forEach { registrar.registerReferenceProvider(it.first, it.second) }
    }

    companion object {

        private val PROVIDERS = listOf(
                // from Ember.inject
                argIn("controller") to providerFor("controller"),
                argIn("service") to providerFor("service"),

                // from Ember.Controller
                argIn("transitionToRoute") to providerFor("route"),

                // from Ember.Route
                argIn("controllerFor") to providerFor("controller"),
                argIn("intermediateTransitionTo") to providerFor("route"),
                argIn("modelFor") to providerFor("route"),
                argIn("paramsFor") to providerFor("route"),
                argIn("render").and(inEmberFile("route")) to providerFor("template"),
                argIn("replaceWith") to providerFor("route"),
                argIn("transitionTo") to providerFor("route"),

                // from DS
                argIn("attr").and(inEmberFile("model")) to providerFor("transform"),
                argIn("hasMany").and(inEmberFile("model")) to providerFor("model"),
                argIn("belongsTo").and(inEmberFile("model")) to providerFor("model"),

                // from DS.Store
                argIn("adapterFor") to providerFor("adapter"),
                argIn("createRecord") to providerFor("model", "adapter"),
                argIn("filter") to providerFor("model", "adapter"),
                argIn("findAll") to providerFor("model", "adapter"),
                argIn("findRecord") to providerFor("model", "adapter"),
                argIn("hasRecordForId") to providerFor("model"),
                argIn("modelFor") to providerFor("model"),
                argIn("normalize") to providerFor("model", "serializer"),
                argIn("peekAll") to providerFor("model"),
                argIn("peekRecord") to providerFor("model"),
                argIn("pushPayload") to providerFor("model"),
                argIn("query") to providerFor("model", "adapter"),
                argIn("queryRecord") to providerFor("model", "adapter"),
                argIn("recordIsLoaded") to providerFor("model"),
                argIn("unloadAll") to providerFor("model"),
                argIn("unloadRecord") to providerFor("model")
        )

        private fun argIn(name: String) = jsQuotedArgument(name, 0)
        private fun providerFor(vararg types: String) = EmberReferenceProvider.forTypes(*types)
    }
}
