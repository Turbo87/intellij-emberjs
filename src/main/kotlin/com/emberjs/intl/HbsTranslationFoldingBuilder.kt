package com.emberjs.intl

import com.dmarcotte.handlebars.psi.HbParam
import com.dmarcotte.handlebars.psi.HbPsiFile
import com.emberjs.hbs.HbsPatterns
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilder
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.Key
import com.intellij.psi.util.PsiFilter
import java.util.*

class HbsTranslationFoldingBuilder : FoldingBuilder {
    override fun buildFoldRegions(node: ASTNode, document: Document): Array<FoldingDescriptor> {
        val file = node.psi as? HbPsiFile ?: return emptyArray()

        return ArrayList<HbParam>()
                .apply { file.accept(TRANSLATION_KEY_FILTER.createVisitor(this)) }
                .map { buildFoldingDescriptor(it) }
                .toTypedArray()
    }

    private fun buildFoldingDescriptor(element: HbParam): FoldingDescriptor {
        // read translation key from HbParam element
        val key = element.text.substring(1, element.textLength - 1)

        // query translation index for translation key
        val translations = EmberTranslationIndex.getTranslations(key, element.project)

        // store translations on ASTNode user data
        element.node.putUserData(TRANSLATIONS_KEY, translations)

        // return FoldingDescriptor
        return FoldingDescriptor(element, element.parent.textRange)
    }

    override fun getPlaceholderText(node: ASTNode): String? {
        return node.getUserData(TRANSLATIONS_KEY)?.get("en") ?:
                "Missing translation: ${node.text.substring(1, node.textLength - 1)}"
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        return node.getUserData(TRANSLATIONS_KEY).let { it != null && it.isNotEmpty() }
    }

    companion object {
        private val TRANSLATIONS_KEY = Key<Map<String, String>>("ember.translations")

        private val TRANSLATION_KEY_FILTER = object : PsiFilter<HbParam>(HbParam::class.java) {
            override fun accept(element: HbParam) = HbsPatterns.TRANSLATION_KEY.accepts(element)
        }
    }
}
