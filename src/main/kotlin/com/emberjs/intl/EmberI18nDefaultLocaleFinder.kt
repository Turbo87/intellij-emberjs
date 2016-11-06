package com.emberjs.intl

import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.JSRecursiveWalkingElementVisitor
import com.intellij.psi.PsiElement

class EmberI18nDefaultLocaleFinder : JSRecursiveWalkingElementVisitor() {
    var defaultLocale: String? = null

    override fun visitJSProperty(node: JSProperty) {
        if (node.name == "defaultLocale") {
            stopWalking()

            val value = node.value
            if (value is JSLiteralExpression && value.isQuotedLiteral) {
                defaultLocale = value.valueAsPropertyName
            }
        }

        super.visitJSProperty(node)
    }

    fun findIn(element: PsiElement): String? {
        element.accept(this)
        return defaultLocale
    }
}
