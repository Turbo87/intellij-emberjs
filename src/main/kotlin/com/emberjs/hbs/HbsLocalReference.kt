package com.emberjs.hbs

import com.dmarcotte.handlebars.parsing.HbTokenTypes
import com.dmarcotte.handlebars.psi.HbMustacheName
import com.dmarcotte.handlebars.psi.HbOpenBlockMustache
import com.dmarcotte.handlebars.psi.HbParam
import com.dmarcotte.handlebars.psi.HbPsiElement
import com.dmarcotte.handlebars.psi.impl.HbOpenBlockMustacheImpl
import com.dmarcotte.handlebars.psi.impl.HbSimpleMustacheImpl
import com.dmarcotte.handlebars.psi.impl.HbStatementsImpl
import com.emberjs.utils.parents
import com.emberjs.utils.resolveDefaultExport
import com.emberjs.utils.resolveDefaultModifier
import com.emberjs.utils.resolveHelper
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.Language
import com.intellij.lang.ecmascript6.psi.JSClassExpression
import com.intellij.lang.ecmascript6.psi.impl.ES6ClassExpressionImpl
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.lang.javascript.psi.*
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
import com.intellij.psi.html.HtmlTag
import com.intellij.psi.impl.source.html.HtmlTagImpl
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.templateLanguages.OuterLanguageElement
import com.intellij.psi.util.*
import com.intellij.psi.xml.XmlAttribute
import org.jetbrains.annotations.NotNull
import kotlin.math.max
import kotlin.math.min

fun resolveToJs(any: Any?, path: List<String>, resolveIncomplete: Boolean = false): PsiElement? {

    if (any is PsiElement && any.references.find { it is HbsLocalReference } != null) {
        val ref = any.references.find { it is HbsLocalReference }
        return resolveToJs(ref?.resolve(), path, resolveIncomplete)
    }

    if (any is HbParam) {
        if (any.children[0].elementType == HbTokenTypes.OPEN_SEXPR) {
            if (any.children[1].text == "hash") {
                val name = path.first()
                val res = any.children.find { it.elementType == HbTokenTypes.HASH && it.children[0].text == name }
                val ref = PsiTreeUtil.collectElements(res, { it.references.find { it is HbsLocalReference } != null }).firstOrNull()
                if (ref != null) {
                    val hbsRef = ref.references.find { it is HbsLocalReference }!!
                    return resolveToJs(hbsRef.resolve(), path.subList(1, max(path.lastIndex, 1)), resolveIncomplete)
                }
                return res
            }
        }
        if (any.children[0] is HbMustacheName) {
            val lastId = any.children[0].children[0].children.findLast { it.elementType == HbTokenTypes.ID }
            return resolveToJs(lastId, path, resolveIncomplete)
        }
    }

    if (any is PsiFile) {
        val helper = resolveHelper(any)
        if (helper != null) {
            return resolveToJs(helper, path, resolveIncomplete)
        }
        val modifier = resolveDefaultModifier(any)
        if (modifier != null) {
            return modifier
        }
        return resolveDefaultExport(any)
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
                return resolveToJs(res, path, resolveIncomplete)
            }
        }
        if (jsType is JSSimpleRecordTypeImpl) {
            val elem = jsType.findPropertySignature(path.first())?.memberSource?.singleElement
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

fun handleEmberHelpers(element: PsiElement): HbsLocalReference? {
    if (element.parent is HbOpenBlockMustache) {
        val mustacheName = element.parent.children.find { it is HbMustacheName }?.text
        if (mustacheName == "let" || mustacheName == "each") {
            val param = PsiTreeUtil.findSiblingBackward(element, HbTokenTypes.PARAM, null)
            if (param == null) {
                return null
            }
            if (mustacheName == "let") {
                return HbsLocalReference(element, param)
            }
            if (mustacheName == "each") {
                val refResolved = param.references.firstOrNull()?.resolve()
                        ?:
                        PsiTreeUtil.collectElements(param, { it.elementType == HbTokenTypes.ID })
                                .filter { it !is LeafPsiElement }
                                .lastOrNull()?.references?.firstOrNull()?.resolve()
                if (refResolved is HbPsiElement) {
                    return HbsLocalReference(element, param.parent)
                }
                val jsRef = resolveToJs(refResolved, emptyList(), false)
                if (jsRef is JSTypeOwner && jsRef.jsType is JSArrayType) {
                    return HbsLocalReference(element, (jsRef.jsType as JSArrayType).type?.sourceElement)
                }
            }
        }
    }
    return null
}

fun createReference(element: PsiElement): PsiReference? {
    val name = element.text.replace("IntellijIdeaRulezzz", "")
    val sibling = PsiTreeUtil.findSiblingBackward(element, HbTokenTypes.ID, null)
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

    val ref = handleEmberHelpers(element)
    if (ref != null) {
        return ref
    }

    // for this.x.y
    if (sibling != null && sibling.references.find { it is HbsLocalReference } != null) {
        return HbsLocalReference(element, resolveToJs(sibling.references.find { it is HbsLocalReference }!!.resolve(), listOf(element.text)))
    }

    // any |block param|
    // as mustache
    val hbblockRefs = PsiTreeUtil.collectElements(element.containingFile, { it is HbOpenBlockMustacheImpl })
            .filter {
                PsiTreeUtil.collectElements(PsiTreeUtil.getNextSiblingOfType(it, HbStatementsImpl::class.java), { it == element }).isNotEmpty()
            }

    // as html tag
    val htmlView = element.containingFile.viewProvider.getPsi(Language.findLanguageByID("HTML")!!)
    val angleBracketBlocks = PsiTreeUtil.collectElements(htmlView, { it is XmlAttribute && it.text.startsWith("|") })
            .filter{ (it.parent as HtmlTag).attributes.map { it.text }.joinToString(" ").contains(Regex("\\|.*$name.*\\|")) }
            .map { it.parent }

    // validate if the element is a child of the tag
    val validBlock = angleBracketBlocks.filter { it ->
        it.textRange.contains(element.textRange)
    }.firstOrNull()

    val blockRef = hbblockRefs.find { it.text.contains(Regex("\\|.*$name.*\\|")) }
    val blockVal = blockRef?.children?.filter { it.elementType == HbTokenTypes.ID }?.find { it.text == name }


    if (blockRef != null || blockVal != null || validBlock != null) {
        if (validBlock != null) {
            val tag  = validBlock as HtmlTagImpl
            val index = tag.attributes.indexOfFirst { it.text == "as" }
            val blockParams = tag.attributes.toList().subList(index + 1, tag.attributes.size)
            val r = blockParams.find { it.text.matches(Regex("^\\|*$name\\|*$")) }!!
            val desc = tag.descriptor?.getAttributeDescriptor(r)
            return HbsLocalReference(element, desc?.declaration?.references?.getOrNull(0)?.resolve())
        }
        return HbsLocalReference(element, blockVal ?: blockRef)
    }
    return null
}

class HbsLocalReference(private val leaf: PsiElement, val target: PsiElement?) : PsiReferenceBase<PsiElement>(leaf) {
    override fun resolve(): PsiElement? {
        return target
    }

    override fun getRangeInElement(): TextRange {
        return TextRange(0, leaf.textLength)
    }

    override fun calculateDefaultRangeInElement(): TextRange {
        return leaf.textRangeInParent
    }
}
