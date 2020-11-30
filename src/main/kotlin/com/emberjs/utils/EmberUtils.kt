package com.emberjs.utils

import com.intellij.lang.ecmascript6.psi.ES6ImportExportDeclaration
import com.intellij.lang.ecmascript6.psi.JSClassExpression
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil


class EmberUtils {
    companion object {

        fun resolveModifier(file: PsiFile): Array<JSFunction?> {
            val func = resolveDefaultExport(file)
            val installer: JSFunction? = PsiTreeUtil.collectElements(func) { it is JSFunction && it.name == "installModifier" }.firstOrNull() as JSFunction?
            val updater: JSFunction? = PsiTreeUtil.collectElements(func, { it is JSFunction && it.name == "updateModifier"}).firstOrNull() as JSFunction?
            val destroyer: JSFunction? = PsiTreeUtil.collectElements(func, { it is JSFunction && it.name == "destroyModifier"}).firstOrNull() as JSFunction?
            return arrayOf(installer, updater, destroyer)
        }

        fun resolveDefaultModifier(file: PsiFile): JSFunction? {
            val modifier = resolveModifier(file)
            val args = modifier.first()?.parameters?.getOrNull(2)
                    ?: modifier[1]?.parameters?.getOrNull(1)
                    ?: modifier[2]?.parameters?.getOrNull(1)
            return modifier.find { it != null && it.parameters.contains(args) }
        }

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

        fun resolveHelper(file: PsiFile): JSFunction? {
            val cls = resolveDefaultExport(file)
            if (cls is JSCallExpression && cls.argumentList != null) {
                var func: PsiElement? = cls.argumentList!!.arguments.last()
                while (func is JSReferenceExpression) {
                    func = func.resolve()
                }
                if (func is JSFunction) {
                    return func
                }
            }
            return null
        }

        fun resolveComponent(file: PsiFile): PsiElement? {
            val cls = resolveDefaultExport(file)
            if (cls is JSClassExpression) {
                return cls
            }
            return null
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

    }
}
