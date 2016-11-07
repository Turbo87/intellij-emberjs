package com.emberjs.translations

import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementVisitor
import java.util.*

class JSStringPropertyCollector : PsiRecursiveElementVisitor() {
    val properties = ArrayList<JSProperty>()

    override fun visitElement(element: PsiElement) {
        if (element is JSProperty){
            val value = element.value
            if (value is JSLiteralExpression && value.isQuotedLiteral) {
                properties += element
            }
        }

        super.visitElement(element)
    }

    fun collectFrom(file: PsiFile): Iterable<JSProperty> {
        val defaultExport = file.children
                .filterIsInstance(ES6ExportDefaultAssignment::class.java)
                .firstOrNull() ?: return emptyList()

        return collectFrom(defaultExport)
    }

    fun collectFrom(element: PsiElement): Iterable<JSProperty> {
        element.accept(this)
        return properties
    }
}
