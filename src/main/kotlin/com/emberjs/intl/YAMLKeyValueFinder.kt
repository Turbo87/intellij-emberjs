package com.emberjs.intl

import com.emberjs.yaml.keyPath
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementVisitor
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLScalar

class YAMLKeyValueFinder(val keyPath: String) : PsiRecursiveElementVisitor() {
    var kv: YAMLKeyValue? = null

    override fun visitElement(element: PsiElement) {
        if (element is YAMLKeyValue && element.value is YAMLScalar && element.keyPath == keyPath) {
            kv = element
        }

        if (kv == null) {
            super.visitElement(element)
        }
    }

    fun findIn(element: PsiElement): YAMLKeyValue? {
        element.accept(this)
        return kv
    }
}
