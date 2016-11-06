package com.emberjs.intl

import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementVisitor
import java.util.*

class JsonStringPropertyCollector : PsiRecursiveElementVisitor() {
    val properties = ArrayList<JsonProperty>()

    override fun visitElement(element: PsiElement) {
        if (element is JsonProperty && element.value is JsonStringLiteral) {
            properties += element
        }

        super.visitElement(element)
    }

    fun collectFrom(element: PsiElement): Iterable<JsonProperty> {
        element.accept(this)
        return properties
    }
}
