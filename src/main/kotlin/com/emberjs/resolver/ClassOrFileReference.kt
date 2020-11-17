package com.emberjs.resolver

import com.intellij.lang.ecmascript6.psi.impl.ES6ClassExpressionImpl
import com.intellij.lang.ecmascript6.psi.impl.ES6ClassImpl
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.annotations.NotNull

class ClassOrFileReference(element: @NotNull PsiElement, target: PsiElement?) : PsiReferenceBase<PsiElement>(element) {
    private val refElement: PsiElement?

    init {
        val psiExp = ES6PsiUtil.findDefaultExport(target ?: this.element)
        if (psiExp != null) {
            var cls: Any? = PsiTreeUtil.findChildOfType(psiExp, ES6ClassExpressionImpl::class.java)
            if (cls == null) {
                val ref = PsiTreeUtil.findChildOfType(psiExp, JSReferenceExpressionImpl::class.java)
                cls = ref?.resolve()
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
