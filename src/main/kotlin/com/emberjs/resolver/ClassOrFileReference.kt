package com.emberjs.resolver

import com.emberjs.utils.findDefaultExportClass
import com.emberjs.utils.resolveHelper
import com.intellij.lang.ecmascript6.psi.impl.ES6ClassExpressionImpl
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSFunction
import com.intellij.lang.javascript.psi.impl.JSArgumentListImpl
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil

class ClassOrFileReference(element: PsiElement) : PsiReferenceBase<PsiElement>(element) {
    private val refElement: PsiElement?

    init {
        if (this.element is PsiFile) {
            var cls: Any? = findDefaultExportClass(this.element as PsiFile)
            if (cls == null) {
                val ref = PsiTreeUtil.findChildOfType(cls, JSReferenceExpressionImpl::class.java)
                cls = ref?.resolve()
            }
            // for helpers
            if (cls == null) {
                cls = resolveHelper(this.element as PsiFile)
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
