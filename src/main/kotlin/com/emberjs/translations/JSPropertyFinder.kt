package com.emberjs.translations

import com.emberjs.js.keyPath
import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementVisitor

class JSPropertyFinder(val keyPath: String) : PsiRecursiveElementVisitor() {
    var property: JSProperty? = null

    override fun visitElement(element: PsiElement) {
        if (element is JSProperty) {
            val value = element.value
            if (value is JSLiteralExpression && value.isQuotedLiteral && element.keyPath == keyPath) {
                property = element
            }
        }

        if (property == null) {
            super.visitElement(element)
        }
    }

    fun findIn(file: PsiFile): JSProperty? {
        val defaultExport = file.children
                .filterIsInstance(ES6ExportDefaultAssignment::class.java)
                .firstOrNull() ?: return null

        return findIn(defaultExport)
    }

    fun findIn(element: PsiElement): JSProperty? {
        element.accept(this)
        return property
    }
}
