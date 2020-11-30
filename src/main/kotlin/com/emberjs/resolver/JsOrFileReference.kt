package com.emberjs.resolver

import com.emberjs.utils.EmberUtils
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil

class JsOrFileReference(element: PsiElement) : PsiReferenceBase<PsiElement>(element) {
    private val refElement: PsiElement?

    init {
        if (this.element is PsiFile) {
            var cls: Any? = EmberUtils.findDefaultExportClass(this.element as PsiFile)
            if (cls == null) {
                val ref = PsiTreeUtil.findChildOfType(cls, JSReferenceExpressionImpl::class.java)
                cls = ref?.resolve()
            }
            if (cls != null) {
                this.refElement = cls as PsiElement?
            } else {
                this.refElement = this.element
            }
        } else {
            this.refElement = this.element
        }

    }
    override fun resolve(): PsiElement? {
        return this.refElement
    }

}
