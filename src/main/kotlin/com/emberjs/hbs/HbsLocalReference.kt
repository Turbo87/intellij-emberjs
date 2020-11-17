package com.emberjs.hbs

import com.dmarcotte.handlebars.parsing.HbTokenTypes
import com.dmarcotte.handlebars.psi.HbMustacheName
import com.dmarcotte.handlebars.psi.HbOpenBlockMustache
import com.dmarcotte.handlebars.psi.HbPsiElement
import com.dmarcotte.handlebars.psi.impl.HbOpenBlockMustacheImpl
import com.dmarcotte.handlebars.psi.impl.HbSimpleMustacheImpl
import com.dmarcotte.handlebars.psi.impl.HbStatementsImpl
import com.emberjs.utils.parents
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.ecmascript6.psi.JSClassExpression
import com.intellij.lang.ecmascript6.psi.impl.ES6ClassExpressionImpl
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.lang.javascript.psi.JSField
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSTypeOwner
import com.intellij.lang.javascript.psi.ecma6.JSTypedEntity
import com.intellij.lang.javascript.psi.ecma6.impl.TypeScriptFieldImpl
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.lang.javascript.psi.impl.JSVariableImpl
import com.intellij.lang.javascript.psi.jsdoc.impl.JSDocCommentImpl
import com.intellij.lang.javascript.psi.types.JSArrayType
import com.intellij.lang.javascript.psi.types.JSRecordTypeImpl
import com.intellij.lang.javascript.psi.types.JSSimpleRecordTypeImpl
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.*
import org.jetbrains.annotations.NotNull
import kotlin.math.max
import kotlin.math.min

fun resolveToJs(any: Any?, path: List<String>, resolveIncomplete: Boolean = false): PsiElement? {

    if (any is PsiElement && any.references.find { it is HbsLocalReference } != null) {
        val ref = any.references.find { it is HbsLocalReference }
        return resolveToJs(ref?.resolve(), path, resolveIncomplete)
    }

    if (any is PsiFile) {
        var cls = ES6PsiUtil.findDefaultExport(any)
        cls = PsiTreeUtil.findChildOfType(cls, JSClassExpression::class.java)
        if (cls == null) {
            val ref = PsiTreeUtil.findChildOfType(any, JSReferenceExpressionImpl::class.java)
            cls = ref?.resolve() as ES6ClassExpressionImpl?
        }
        return resolveToJs(cls, path, resolveIncomplete)
    }

    if (path.isEmpty()) {
        return any as PsiElement?
    }

    if (any is JSClassExpression) {
        val n = path.first()
        val f = any.fields.find { it.name == n } ?: any.functions.find { it.name == n }
        if (f == null && resolveIncomplete) {
            return any;
        }
        return resolveToJs(f, path.subList(1, max(path.lastIndex, 1)), resolveIncomplete)
    }

    var jsType: JSType? = null
    if (any is JSTypedEntity) {
        jsType = any.jsType
    }
    if (any is JSTypeOwner) {
        jsType = any.jsType
    }
    if (jsType != null) {
        if (jsType.sourceElement is JSDocCommentImpl) {
            val doc = jsType.sourceElement as JSDocCommentImpl
            val tag = doc.tags.find { it.text.startsWith("@type") }
            val res = tag?.value?.reference?.resolve()
            if (res != null) {
                return resolveToJs(res, path.slice(IntRange(1, max(path.lastIndex, 1))), resolveIncomplete)
            }
        }
        if (jsType is JSSimpleRecordTypeImpl) {
            val elem = (jsType as JSSimpleRecordTypeImpl).findPropertySignature(path.first())?.memberSource?.singleElement
            return resolveToJs(elem, path.subList(1, max(path.lastIndex, 1)), resolveIncomplete)
        }
        if (any is JSVariableImpl<*, *> && any.doGetExplicitlyDeclaredType() != null) {
            val jstype = any.doGetExplicitlyDeclaredType()
            if (jstype is JSRecordTypeImpl) {
                return resolveToJs(jstype.sourceElement, path.subList(1, max(path.lastIndex, 1)), resolveIncomplete)
            }
        }
    }
    return null
}

fun toLocalReference(element: PsiElement): PsiReference? {
    val name = element.text
    var sibling = PsiTreeUtil.findSiblingBackward(element, HbTokenTypes.ID, null)
    if (name == "this" && sibling == null) {
        val fname = element.containingFile.name.split(".").first()
        var fileName = fname
        if (fileName == "template") {
            fileName = "component"
        }
        val dir = element.containingFile.originalFile.containingDirectory
        val file = dir?.findFile("$fileName.js")
                ?: dir?.findFile("$fileName.ts")
                ?: dir?.findFile("controller.js")
                ?: dir?.findFile("controller.ts")
        if (file != null) {
            return HbsLocalReference(element, resolveToJs(file, listOf()))
        }
    }

    if (element.parent is HbOpenBlockMustache) {
        val mustacheName = element.parent.children.find { it is HbMustacheName }?.text
        if (mustacheName == "let" || mustacheName == "each") {
            val param = PsiTreeUtil.findSiblingBackward(element, HbTokenTypes.PARAM, null)
            val ref = PsiTreeUtil.collectElements(param, { it.elementType == HbTokenTypes.ID }).last()
            if (mustacheName == "let") {
                return HbsLocalReference(element, ref.parent)
            }
            if (mustacheName == "each") {
                if (ref.parent.references.first().resolve() is HbPsiElement) {
                    return HbsLocalReference(element, ref.parent)
                } else {
                    var jsRef = ref.parent.references.first().resolve()
                    jsRef = resolveToJs(jsRef, emptyList(), false)
                    if (jsRef is JSTypeOwner && jsRef.jsType is JSArrayType) {
                        return HbsLocalReference(element, (jsRef.jsType as JSArrayType).type?.sourceElement)
                    }
                }
            }
        }
    }

    if (sibling != null && sibling.references.find { it is HbsLocalReference } != null) {
        return HbsLocalReference(element, resolveToJs(sibling.references.find { it is HbsLocalReference }!!.resolve(), listOf(element.text)))
    }
    val hbblockRefs = PsiTreeUtil.collectElements(element.containingFile, { it is HbOpenBlockMustacheImpl })
            .filter {
                PsiTreeUtil.collectElements(PsiTreeUtil.getNextSiblingOfType(it, HbStatementsImpl::class.java), { it == element }) != null
            }
    val angleBracketblocks = PsiTreeUtil.collectElements(element.containingFile, { it.elementType == HbTokenTypes.CONTENT })
            .filter { it.siblings().find { PsiTreeUtil.collectElements(it, { it == element }).isNotEmpty() } != null }
    val angleBlockRef = angleBracketblocks.find { it.text.contains(Regex("\\|.*$name.*\\|")) }
    val blockRef = hbblockRefs.find { it.text.contains(Regex("\\|.*$name.*\\|")) }
    val blockVal = blockRef?.children?.filter { it.elementType.toString().endsWith("ID") }?.find { it.text == name }

    if (blockRef != null || blockVal != null || angleBlockRef != null) {
        return HbsLocalReference(element, blockVal ?: blockRef ?: angleBlockRef!!)
    }
    return null
}

class HbsLocalReference(private val leaf: PsiElement, val target: PsiElement?) : PsiReferenceBase<PsiElement>(leaf) {
    override fun resolve(): PsiElement? {
        return target
    }

    override fun getRangeInElement(): TextRange {
        return leaf.textRangeInParent
    }

    override fun calculateDefaultRangeInElement(): TextRange {
        return leaf.textRangeInParent
    }
}
