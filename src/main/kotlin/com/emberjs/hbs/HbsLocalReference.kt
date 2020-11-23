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
        var cls = ES6PsiUtil.findDefaultExport(any)
        // find class (components or helpers with class)
        cls = PsiTreeUtil.findChildOfType(cls, JSClassExpression::class.java)
        // find function (for helpers)
        cls = cls ?: PsiTreeUtil.findChildOfType(cls, JSCallExpression::class.java)
        if (cls == null) {
            val ref = PsiTreeUtil.findChildOfType(any, JSReferenceExpressionImpl::class.java)
            cls = ref?.resolve() as JSElement?
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
                return resolveToJs(res, path.subList(1, max(path.lastIndex, 1)), resolveIncomplete)
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
            if (mustacheName == "let") {
                return HbsLocalReference(element, param)
            }
            if (mustacheName == "each") {
                if (param?.references?.first()?.resolve() is HbPsiElement) {
                    return HbsLocalReference(element, param.parent)
                } else {
                    var jsRef = param?.references?.first()?.resolve()
                    jsRef = resolveToJs(jsRef, emptyList(), false)
                    if (jsRef is JSTypeOwner && jsRef.jsType is JSArrayType) {
                        return HbsLocalReference(element, (jsRef.jsType as JSArrayType).type?.sourceElement)
                    }
                }
            }
        }
    }
    return null
}

fun referenceImports(name: String) {
    val imports = PsiTreeUtil.collectElements(element.containingFile, { it.elementType == HbTokenTypes.OPEN && it.parent.text == "{{import"}).map { it.parent }
    imports.find { it.children[2].text.split(",").contains(name) }
}

fun toLocalReference(element: PsiElement): PsiReference? {
    val name = element.text.replace("IntellijIdeaRulezzz", "")
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

    if (element.parents.find { it is HbOpenBlockMustache && it.text.startsWith("{{import")}) {

    }

    val importRef = referenceImports(element)

    val ref = handleEmberHelpers(element)
    if (ref != null) {
        return ref
    }

    // for this.x.y
    if (sibling != null && sibling.references.find { it is HbsLocalReference } != null) {
        return HbsLocalReference(element, resolveToJs(sibling.references.find { it is HbsLocalReference }!!.resolve(), listOf(element.text)))
    }

    // any |block param|
    val hbblockRefs = PsiTreeUtil.collectElements(element.containingFile, { it is HbOpenBlockMustacheImpl })
            .filter {
                PsiTreeUtil.collectElements(PsiTreeUtil.getNextSiblingOfType(it, HbStatementsImpl::class.java), { it == element }).isNotEmpty()
            }
    val htmlView = element.containingFile.viewProvider.getPsi(Language.findLanguageByID("HTML")!!)
    val angleBracketBlocks = PsiTreeUtil.collectElements(htmlView, { it is XmlAttribute && it.text.startsWith("|") })
            .filter{ it.text.replace("|", "").split(" ").contains(name) }
            .map { it.parent }

    val validBlock = angleBracketBlocks.filter { it ->
        val hbsFragments = PsiTreeUtil.collectElements(it) { it.elementType == HbTokenTypes.OUTER_ELEMENT_TYPE }.toList()
        val hbsParts = hbsFragments.map { element.containingFile.findElementAt(it.textOffset)!!.parent.parent }
        hbsParts.find { PsiTreeUtil.collectElements(it) { it == element }.isNotEmpty() } != null
    }.firstOrNull()

    val blockRef = hbblockRefs.find { it.text.contains(Regex("\\|.*$name.*\\|")) }
    val blockVal = blockRef?.children?.filter { it.elementType == HbTokenTypes.ID }?.find { it.text == name }


    if (blockRef != null || blockVal != null || validBlock != null) {
        if (validBlock != null) {

            val tag  = validBlock as HtmlTagImpl?
            val r = tag?.attributes?.find { it.text.startsWith("|") }!!
            val desc = tag.descriptor?.getAttributeDescriptor(r)
            val i = r.text.split("|")[1].split(" ").indexOf(name)
            return HbsLocalReference(element, desc?.declaration?.references?.getOrNull(i)?.resolve())
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
