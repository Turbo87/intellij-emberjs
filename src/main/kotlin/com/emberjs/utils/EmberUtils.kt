package com.emberjs.utils

import com.emberjs.hbs.HbsModuleReference
import com.intellij.lang.ecmascript6.psi.ES6ImportExportDeclaration
import com.intellij.lang.ecmascript6.psi.JSClassExpression
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiReference
import com.intellij.psi.util.PsiTreeUtil


class EmberUtils {
    companion object {

        fun resolveDefaultExport(file: PsiFile): PsiElement? {
            var exp = ES6PsiUtil.findDefaultExport(file)
            val exportImport = PsiTreeUtil.findChildOfType(file, ES6ImportExportDeclaration::class.java)
            if (exportImport != null && exportImport.children.find { it.text == "default" } != null) {
                exp = ES6PsiUtil.resolveDefaultExport(exportImport).firstOrNull() as JSElement? ?: exp
            }
            var ref: Any? = exp?.children?.find { it is JSReferenceExpression } as JSReferenceExpression?
            while (ref is JSReferenceExpression && ref.resolve() != null) {
                ref = ref.resolve()
            }
            ref = ref as JSElement? ?: exp
            val func = ref?.children?.find { it is JSCallExpression }
            if (func != null) {
                return func
            }
            val cls = ref?.children?.find { it is JSClassExpression }
            if (cls != null) {
                return cls
            }
            val obj = ref?.children?.find { it is JSObjectLiteralExpression }
            if (obj != null) {
                return obj
            }
            return ref
        }

        fun findDefaultExportClass(file: PsiFile): JSClass? {
            val exp = ES6PsiUtil.findDefaultExport(file)
            var cls: Any? = exp?.children?.find { it is JSClass }
            cls = cls ?: exp?.children?.find { it is JSFunction }
            if (cls == null) {
                val ref: JSReferenceExpression? = exp?.children?.find { it is JSReferenceExpression } as JSReferenceExpression?
                cls = ref?.resolve()
                if (cls is JSClass) {
                    return cls
                }
                cls = PsiTreeUtil.findChildOfType(ref?.resolve(), JSClass::class.java)
                return cls
            }
            return cls as JSClass?
        }

        fun resolveReference(reference: PsiReference?, path: String?): PsiElement? {
            var element = reference?.resolve()
            if (element == null && reference is HbsModuleReference) {
                element = reference.multiResolve(false).firstOrNull()?.element
            }
            return element
        }

        fun followReferences(element: PsiElement?, path: String? = null): PsiElement? {

            if (element?.reference != null) {
                return followReferences(resolveReference(element.reference, path), path)
            }
            if (element?.references != null && element.references.isNotEmpty()) {
                val res = element.references.map { resolveReference(it, path) }
                        .filterNotNull()
                        .firstOrNull()
                if (res == element) {
                    return res
                }
                return followReferences(res, path)
            }
            return element
        }
    }
}
