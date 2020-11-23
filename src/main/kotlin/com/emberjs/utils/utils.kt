package com.emberjs.utils

import com.dmarcotte.handlebars.parsing.HbTokenTypes
import com.emberjs.hbs.HbsModuleReference
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


fun resolveReference(reference: PsiReference?): PsiElement? {
    var element = reference?.resolve()
    if (element == null && reference is HbsModuleReference) {
        element = reference.multiResolve(false).firstOrNull()?.element
    }
    return element
}
