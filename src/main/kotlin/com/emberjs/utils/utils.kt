package com.emberjs.utils

import com.intellij.lang.ecmascript6.psi.ES6ImportExportDeclaration
import com.intellij.lang.ecmascript6.psi.JSClassExpression
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.*
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiReference
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.annotations.NotNull

fun resolveHelper(file: PsiFile): JSFunction? {
    var exp = ES6PsiUtil.findDefaultExport(file)
    val exportImport = PsiTreeUtil.findChildOfType(file, ES6ImportExportDeclaration::class.java)
    if (exportImport != null) {
        exp = ES6PsiUtil.resolveDefaultExport(exportImport).firstOrNull() as JSElement? ?: exp
    }

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

fun findHelperParams(file: PsiFile): Array<JSParameterListElement>? {
    return resolveHelper(file)?.parameters
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
    return cls as JSClass
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
        if (res is TypeScriptTypeAlias) {
            jsObject = res.children.find { it is TypeScriptObjectType } as TypeScriptObjectType
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
