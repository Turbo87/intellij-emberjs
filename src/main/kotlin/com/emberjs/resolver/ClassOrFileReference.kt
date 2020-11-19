package com.emberjs.resolver

import com.intellij.lang.ecmascript6.psi.impl.ES6ClassExpressionImpl
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSFunction
import com.intellij.lang.javascript.psi.impl.JSArgumentListImpl
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.PsiTreeUtil

class ClassOrFileReference(element: PsiElement, target: PsiElement?) : PsiReferenceBase<PsiElement>(element) {
    private val refElement: PsiElement?

    fun resolveHelper(element: PsiElement?): PsiElement? {
        if (element is PsiReference && element.resolve() != null) {
            return resolveHelper(element.resolve()!!)
        }
        if (element is JSCallExpression && element.text.startsWith("helper(")) {
            val res = ((element.children[1] as JSArgumentListImpl).arguments[0] as PsiReference).resolve()!!
            return resolveHelper(res)
        }
        if (element is JSFunction) {
            return element
        }
        return null
    }

    init {
        val psiExp = ES6PsiUtil.findDefaultExport(target ?: this.element)
        if (psiExp != null) {
            var cls: Any? = PsiTreeUtil.findChildOfType(psiExp, ES6ClassExpressionImpl::class.java)
            if (cls == null) {
                val ref = PsiTreeUtil.findChildOfType(psiExp, JSReferenceExpressionImpl::class.java)
                cls = ref?.resolve()
            }
            // for helpers
            if (cls == null) {
                cls = resolveHelper(psiExp.children[0])
            }
            this.refElement = cls as PsiElement?
        } else {
            this.refElement = this.element.containingFile
        }
    }
    override fun resolve(): PsiElement? {
        return this.refElement
    }

}
