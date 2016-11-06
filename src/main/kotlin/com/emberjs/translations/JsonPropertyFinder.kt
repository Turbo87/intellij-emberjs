package com.emberjs.translations

import com.emberjs.json.keyPath
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementVisitor

class JsonPropertyFinder(val keyPath: String) : PsiRecursiveElementVisitor() {
    var property: JsonProperty? = null

    override fun visitElement(element: PsiElement) {
        if (element is JsonProperty && element.value is JsonStringLiteral && element.keyPath == keyPath) {
            property = element
        }

        if (property == null) {
            super.visitElement(element)
        }
    }

    fun findIn(element: PsiElement): JsonProperty? {
        element.accept(this)
        return property
    }
}
