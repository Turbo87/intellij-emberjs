package com.emberjs.hbs

import com.emberjs.translations.EmberTranslationHbsReferenceProvider
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.XmlTagPattern
import com.intellij.psi.*
import com.intellij.psi.xml.XmlAttribute
import com.intellij.util.ProcessingContext

fun filter(element: PsiElement, fn: (PsiElement) -> PsiReference?): PsiReference? {
    if (element.text.contains(".")) {
        return null
    }
    if (HbsLocalReference.createReference(element) != null) {
        return null
    }
    return fn(element)
}

class HbsReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        with(registrar) {
            registerReferenceProvider(XmlTagPattern.Capture(), TagReferencesProvider())
            register(PlatformPatterns.psiElement(XmlAttribute::class.java)) { toAttributeReference(it as XmlAttribute) }
            register(HbsPatterns.SIMPLE_MUSTACHE_NAME) { filter(it) { HbsComponentReference(it) } }
            register(HbsPatterns.BLOCK_MUSTACHE_NAME) { filter(it) { HbsComponentReference(it) } }
            register(HbsPatterns.MUSTACHE_ID) { HbsLocalReference.createReference(it) }
            register(HbsPatterns.SIMPLE_MUSTACHE_NAME) { filter(it) { HbsModuleReference(it, "helper") } }
            register(HbsPatterns.SIMPLE_MUSTACHE_NAME) { filter(it) { HbsModuleReference(it, "modifier") } }
            register(HbsPatterns.SUB_EXPR_NAME) { filter(it) { HbsModuleReference(it, "helper") } }
            register(HbsPatterns.SUB_EXPR_NAME) { filter(it) { HbsModuleReference(it, "modifier") } }
            registerReferenceProvider(HbsPatterns.LINK_TO_BLOCK_TARGET, HbsLinkToReferenceProvider())
            registerReferenceProvider(HbsPatterns.LINK_TO_SIMPLE_TARGET, HbsLinkToReferenceProvider())
            registerReferenceProvider(HbsPatterns.TRANSLATION_KEY, EmberTranslationHbsReferenceProvider())
            registerReferenceProvider(HbsPatterns.TRANSLATION_KEY_IN_SEXPR, EmberTranslationHbsReferenceProvider())
        }
    }

    private fun PsiReferenceRegistrar.register(pattern: ElementPattern<out PsiElement>, fn: (PsiElement) -> PsiReference?) {
        registerReferenceProvider(pattern, object : PsiReferenceProvider() {
            override fun getReferencesByElement(element: PsiElement, context: ProcessingContext) = arrayOf(fn(element)).filterNotNull().toTypedArray()
        })
    }
}
