package com.emberjs.psi

import com.emberjs.patterns.EmberPatterns.inEmberFile
import com.emberjs.patterns.EmberPatterns.jsQuotedArgument
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar

class EmberJSReferenceContributor : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        PROVIDERS.forEach { registrar.registerReferenceProvider(it.first, it.second) }
    }

    companion object {

        private val PROVIDERS = listOf(
                // from Ember.inject
                Pair(argIn("controller"), providerFor("controller")),
                Pair(argIn("service"), providerFor("service")),

                // from Ember.Controller
                Pair(argIn("transitionToRoute"), providerFor("route")),

                // from Ember.Route
                Pair(argIn("controllerFor"), providerFor("controller")),
                Pair(argIn("intermediateTransitionTo"), providerFor("route")),
                Pair(argIn("modelFor"), providerFor("route")),
                Pair(argIn("paramsFor"), providerFor("route")),
                Pair(argIn("render").and(inEmberFile("route")), providerFor("template")),
                Pair(argIn("replaceWith"), providerFor("route")),
                Pair(argIn("transitionTo"), providerFor("route")),

                // from DS
                Pair(argIn("attr").and(inEmberFile("model")), providerFor("transform")),
                Pair(argIn("hasMany").and(inEmberFile("model")), providerFor("model")),
                Pair(argIn("belongsTo").and(inEmberFile("model")), providerFor("model")),

                // from DS.Store
                Pair(argIn("adapterFor"), providerFor("adapter")),
                Pair(argIn("createRecord"), providerFor("model", "adapter")),
                Pair(argIn("filter"), providerFor("model", "adapter")),
                Pair(argIn("findAll"), providerFor("model", "adapter")),
                Pair(argIn("findRecord"), providerFor("model", "adapter")),
                Pair(argIn("hasRecordForId"), providerFor("model")),
                Pair(argIn("modelFor"), providerFor("model")),
                Pair(argIn("normalize"), providerFor("model", "serializer")),
                Pair(argIn("peekAll"), providerFor("model")),
                Pair(argIn("peekRecord"), providerFor("model")),
                Pair(argIn("pushPayload"), providerFor("model")),
                Pair(argIn("query"), providerFor("model", "adapter")),
                Pair(argIn("queryRecord"), providerFor("model", "adapter")),
                Pair(argIn("recordIsLoaded"), providerFor("model")),
                Pair(argIn("unloadAll"), providerFor("model")),
                Pair(argIn("unloadRecord"), providerFor("model"))
        )

        private fun argIn(name: String) = jsQuotedArgument(name, 0)
        private fun providerFor(vararg types: String) = EmberJSReferenceProvider.forTypes(*types)
    }
}
