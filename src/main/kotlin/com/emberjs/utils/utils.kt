package com.emberjs.utils

import com.intellij.lang.ecmascript6.psi.JSClassExpression
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSFunction
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecma6.TypeScriptInterface
import com.intellij.lang.javascript.psi.ecma6.TypeScriptObjectType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptSingleType
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiReference
import com.intellij.psi.util.PsiTreeUtil

fun resolveHelper(file: PsiFile): JSFunction? {
    val exp = ES6PsiUtil.findDefaultExport(file)
    // find class (helpers with class)
    var cls: JSElement? = PsiTreeUtil.findChildOfType(exp, JSClassExpression::class.java)
    // find function (for helpers)
    cls = cls ?: PsiTreeUtil.findChildOfType(exp, JSCallExpression::class.java)
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


fun findDefaultExportClass(file: PsiFile): JSClass? {
    val exp = ES6PsiUtil.findDefaultExport(file)
    var cls: Any? = PsiTreeUtil.findChildOfType(exp, JSClass::class.java)
    if (cls == null) {
        val ref = PsiTreeUtil.findChildOfType(exp, JSReferenceExpression::class.java)
        cls = ref?.resolve()
        if (cls is JSClass) {
            return cls
        }
        cls = PsiTreeUtil.findChildOfType(ref?.resolve(), JSClass::class.java)
        return cls
    }
    return cls
}


fun findComponentArgsType(tsFile: PsiFile): TypeScriptObjectType? {
    val cls = findDefaultExportClass(tsFile)
    val type = PsiTreeUtil.findChildOfType(cls, TypeScriptSingleType::class.java)
    var jsObject: TypeScriptObjectType? = null
    if (type?.children?.getOrNull(0) is JSReferenceExpression) {
        val res = (type.children[0] as PsiReference).resolve()
        if (res is TypeScriptInterface) {
            jsObject = res.body
        }
        if (res is TypeScriptObjectType) {
            jsObject = res
        }
    }
    if (type?.children?.getOrNull(0) is TypeScriptObjectType) {
        jsObject = type.children[0] as TypeScriptObjectType
    }
    return jsObject
}
