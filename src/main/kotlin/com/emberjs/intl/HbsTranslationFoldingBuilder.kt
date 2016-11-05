package com.emberjs.intl

import com.dmarcotte.handlebars.psi.HbParam
import com.dmarcotte.handlebars.psi.HbPsiFile
import com.emberjs.hbs.HbsPatterns
import com.emberjs.utils.collect
import com.emberjs.utils.toFilter
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilder
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.Key

class HbsTranslationFoldingBuilder : FoldingBuilder {
    override fun buildFoldRegions(node: ASTNode, document: Document): Array<FoldingDescriptor> {
        val file = node.psi as? HbPsiFile ?: return emptyArray()

        val simpleMustaches = file.collect(HbsPatterns.TRANSLATION_KEY.toFilter())
        val subexpressions = file.collect(HbsPatterns.TRANSLATION_KEY_IN_SEXPR.toFilter())

        return (simpleMustaches + subexpressions).map { buildFoldingDescriptor(it) }.toTypedArray()
    }

    private fun buildFoldingDescriptor(element: HbParam): FoldingDescriptor {
        // read translation key from HbParam element
        val key = element.text.substring(1, element.textLength - 1)

        // query translation index for translation key
        val translations = EmberTranslationIndex.getTranslations(key, element.project)

        // store translations on ASTNode user data
        element.node.putUserData(TRANSLATIONS_KEY, translations)

        return FoldingDescriptor(element, element.parent.textRange)
    }

    override fun getPlaceholderText(node: ASTNode): String? = when {
        node.psi.parent is HbParam -> "\"${getRawPlaceholderText(node)}\""
        else -> getRawPlaceholderText(node)
    }

    fun getRawPlaceholderText(node: ASTNode): String {
        return node.getUserData(TRANSLATIONS_KEY)?.get("en") ?:
                "Missing translation: ${node.text.substring(1, node.textLength - 1)}"
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        return node.getUserData(TRANSLATIONS_KEY).let { it != null && it.isNotEmpty() }
    }

    companion object {
        private val TRANSLATIONS_KEY = Key<Map<String, String>>("ember.translations")
    }
}
