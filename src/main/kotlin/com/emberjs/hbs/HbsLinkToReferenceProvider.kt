package com.emberjs.hbs

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext

class HbsLinkToReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<out PsiReference> {
        return extractModuleNames(element.text)
                .map { HbsLinkToReference(element, it.value, it.key.replace('.', '/')) }
                .toTypedArray()
    }

    companion object {
        private val DELIMITERS_RE = Regex("^[\"']|\\.|[\"']$")

        /**
         * Decomposes `"foo.bar"` into `foo` and `foo.bar` with the TextRanges of
         * `foo` and `bar`
         */
        fun extractModuleNames(moduleName: String): Map<String, TextRange> {
            if (moduleName.length < 3)
                return emptyMap()

            val matches = DELIMITERS_RE.findAll(moduleName).map { it.range.start }.toList()

            return matches.zip(matches.drop(1)).associate {
                val key = moduleName.slice(1..it.second - 1)
                key to TextRange(it.first + 1, it.second)
            }
        }
    }
}
