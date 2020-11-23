package com.emberjs.hbs

import com.emberjs.translations.EmberTranslationHbsReferenceProvider
import com.intellij.patterns.ElementPattern
import com.intellij.psi.*
import com.intellij.util.ProcessingContext

class HbsReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        with(registrar) {
            register(HbsPatterns.SIMPLE_MUSTACHE_NAME) { HbsComponentReference(it) }
            register(HbsPatterns.BLOCK_MUSTACHE_NAME) { HbsComponentReference(it) }
            register(HbsPatterns.SIMPLE_MUSTACHE_NAME) { HbsModuleReference(it, "helper") }
            register(HbsPatterns.SUB_EXPR_NAME) { HbsModuleReference(it, "helper") }
            register(HbsPatterns.SIMPLE_MUSTACHE_NAME) { HbsModuleReference(it, "modifier") }
            register(HbsPatterns.SUB_EXPR_NAME) { HbsModuleReference(it, "modifier") }
            registerReferenceProvider(HbsPatterns.LINK_TO_BLOCK_TARGET, HbsLinkToReferenceProvider())
            registerReferenceProvider(HbsPatterns.LINK_TO_SIMPLE_TARGET, HbsLinkToReferenceProvider())
            registerReferenceProvider(HbsPatterns.TRANSLATION_KEY, EmberTranslationHbsReferenceProvider())
            registerReferenceProvider(HbsPatterns.TRANSLATION_KEY_IN_SEXPR, EmberTranslationHbsReferenceProvider())
        }
    }

    private fun PsiReferenceRegistrar.register(pattern: ElementPattern<out PsiElement>, fn: (PsiElement) -> PsiReference) {
        registerReferenceProvider(pattern, object : PsiReferenceProvider() {
            override fun getReferencesByElement(element: PsiElement, context: ProcessingContext) = arrayOf(fn(element))
        })
    }
}
