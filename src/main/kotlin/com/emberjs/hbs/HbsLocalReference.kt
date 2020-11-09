package com.emberjs.hbs

import com.dmarcotte.handlebars.parsing.HbTokenTypes
import com.dmarcotte.handlebars.psi.*
import com.dmarcotte.handlebars.psi.impl.HbOpenBlockMustacheImpl
import com.dmarcotte.handlebars.psi.impl.HbPsiElementImpl
import com.dmarcotte.handlebars.psi.impl.HbStatementsImpl
import com.emberjs.index.EmberNameIndex
import com.emberjs.utils.*
import com.intellij.lang.Language
import com.intellij.lang.ecmascript6.psi.ES6ImportDeclaration
import com.intellij.lang.ecmascript6.psi.JSClassExpression
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSTypeOwner
import com.intellij.lang.javascript.psi.ecma6.JSTypedEntity
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.lang.javascript.psi.impl.JSVariableImpl
import com.intellij.lang.javascript.psi.jsdoc.impl.JSDocCommentImpl
import com.intellij.lang.javascript.psi.types.JSArrayType
import com.intellij.lang.javascript.psi.types.JSRecordTypeImpl
import com.intellij.lang.javascript.psi.types.JSSimpleRecordTypeImpl
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.html.HtmlTag
import com.intellij.psi.impl.source.html.HtmlTagImpl
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parentsWithSelf
import com.intellij.psi.xml.XmlAttribute
import kotlin.math.max

class ImportNameReferences(element: PsiElement) : PsiPolyVariantReferenceBase<PsiElement>(element, TextRange(0, element.textLength), true) {
    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val names = element.text.split(",")
        val named = names.map {
            if (it.contains(" as ")) {
                it.split(" as ").first()
            } else {
                it
            }
        }
        val mustache = element.parents.find { it is HbMustache }!!
        val path = mustache.children.findLast { it is HbParam }
        val fileRef = path?.references?.firstOrNull()?.resolve()
        if (fileRef is PsiDirectory) {
            return named
                    .map { fileRef.findFile(it) ?: fileRef.findSubdirectory(it) }
                    .filterNotNull()
                    .map { PsiElementResolveResult(it) }
                    .toTypedArray()
        }
        if (fileRef == null) {
            return emptyArray()
        }
        val ref = EmberUtils.resolveToEmber(fileRef as PsiFile)
        return arrayOf(PsiElementResolveResult(ref ?: fileRef))
    }
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

    companion object {
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
                val helper = EmberUtils.resolveHelper(any)
                if (helper != null) {
                    return resolveToJs(helper, path, resolveIncomplete)
                }
                val modifier = EmberUtils.resolveDefaultModifier(any)
                if (modifier != null) {
                    return modifier
                }
                return EmberUtils.resolveDefaultExport(any)
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

            val insideImport = element.parents.find { it is HbMustache && it.children.getOrNull(1)?.text == "import"} != null

            if (insideImport && element.text != "from" && element.text != "import") {
                return null
            }

            val importRef = EmberUtils.referenceImports(element, name)
            if (importRef != null) {
                return HbsLocalReference(element, importRef)
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
                    val r = blockParams.find { it.text.matches(Regex("^\\|*$name\\|*$")) }
                    val desc = tag.descriptor?.getAttributeDescriptor(r)
                    return HbsLocalReference(element, desc?.declaration?.references?.getOrNull(0)?.resolve())
                }
                return HbsLocalReference(element, blockVal ?: blockRef)
            }
            return null
        }
    }
}
