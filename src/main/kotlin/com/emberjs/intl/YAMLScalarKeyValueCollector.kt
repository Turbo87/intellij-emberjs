package com.emberjs.intl

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementVisitor
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLScalar
import java.util.*

class YAMLScalarKeyValueCollector : PsiRecursiveElementVisitor() {
    val kvs = ArrayList<YAMLKeyValue>()

    override fun visitElement(element: PsiElement) {
        if (element is YAMLKeyValue && element.value is YAMLScalar) {
            kvs += element
        }

        super.visitElement(element)
    }

    fun collectFrom(element: PsiElement): Iterable<YAMLKeyValue> {
        element.accept(this)
        return kvs
    }
}
