package com.emberjs.hbs

import com.dmarcotte.handlebars.parsing.HbTokenTypes
import com.dmarcotte.handlebars.psi.HbParam
import com.dmarcotte.handlebars.psi.impl.HbBlockWrapperImpl
import com.dmarcotte.handlebars.psi.impl.HbPathImpl
import com.emberjs.utils.followReferences
import com.emberjs.utils.parents
import com.emberjs.utils.resolveHelper
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.Language
import com.intellij.lang.ecmascript6.psi.JSClassExpression
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.impl.JSVariableImpl
import com.intellij.lang.javascript.psi.jsdoc.impl.JSDocCommentImpl
import com.intellij.lang.javascript.psi.types.JSRecordTypeImpl
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parentsWithSelf
import com.intellij.psi.xml.XmlAttribute
import com.intellij.util.ProcessingContext


class HbsLocalCompletion : CompletionProvider<CompletionParameters>() {

    fun resolveJsType(jsType: JSType?, result: CompletionResultSet, suffix:String="") {
        if (jsType?.sourceElement is JSDocCommentImpl) {
            val doc = jsType.sourceElement as JSDocCommentImpl
            if (doc.tags[0].value?.reference?.resolve() != null) {
                resolve(doc.tags[0].value?.reference?.resolve()!!, result)
            }
            return
        }
        if (jsType is JSRecordTypeImpl) {
            val names = (jsType).propertyNames
            result.addAllElements(names.map { LookupElementBuilder.create(it + suffix) })
            return
        }

    }

    fun resolve(anything: PsiElement?, result: CompletionResultSet) {
        var refElement: Any? = anything
        if (anything == null) {
            return
        }

        if (anything.references.find { it is HbsLocalReference } != null) {
            resolve(anything.references.find { it is HbsLocalReference }!!.resolve(), result)
        }

        if (anything.reference is HbsLocalReference) {
            resolve(anything.reference?.resolve(), result)
        }

        if (refElement is HbParam) {
            if (refElement.children.find { it is HbParam }?.text == "hash") {
                val names = refElement.children.filter { it.elementType == HbTokenTypes.HASH }.map { it.children[0].text }
                result.addAllElements(names.map { LookupElementBuilder.create(it) })
            }
            val ids = PsiTreeUtil.collectElements(refElement, {it.elementType == HbTokenTypes.ID && it !is LeafPsiElement } )
            if (ids.size == 1) {
                resolve(ids.first(), result)
            }
        }

        if (refElement is JSClassExpression) {
            result.addAllElements(refElement.fields.map { LookupElementBuilder.create(it.name!!) })
            result.addAllElements(refElement.functions.map { LookupElementBuilder.create(it.name!!) })
        }

        if (refElement is JSField) {
            resolveJsType(refElement.jsType, result)
            if (refElement is JSVariableImpl<*,*> && refElement.doGetExplicitlyDeclaredType() != null) {
                val jstype = refElement.doGetExplicitlyDeclaredType()
                resolveJsType(jstype, result)
            }
        }
    }

    fun addHelperCompletions(element: PsiElement, result: CompletionResultSet) {
        val file = followReferences(element.children[0])
        var func: JSFunction? = null
        if (file is JSFunction) {
            func = file
        }
        if (file is PsiFile) {
            func = resolveHelper(file)
        }

        if (func != null) {
            val hash = func.parameterList?.parameters?.last()
            resolveJsType(hash?.jsType ?: hash?.inferredType, result, "=")
        }
    }

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val regex = Regex("\\|.*\\|")
        val element = parameters.position
        val txt = element.parents.find { it is HbPathImpl }?.text!!.replace("IntellijIdeaRulezzz", "")

        val helperElement = element.parentsWithSelf
                .find { it.children.getOrNull(0)?.elementType == HbTokenTypes.OPEN_SEXPR }
                ?.children?.getOrNull(1)
        if (helperElement != null) {
            addHelperCompletions(helperElement, result)
        }
        // find all |blocks| from mustache
        val blocks = PsiTreeUtil.collectElements(parameters.originalFile) { it is HbBlockWrapperImpl }
                .filter { it.children[0].text.contains(regex) }
                .filter { PsiTreeUtil.collectElements(it, { it == element }).isNotEmpty() }

        // find all |blocks| from component tags, needs html view
        val htmlView = parameters.originalFile.viewProvider.getPsi(Language.findLanguageByID("HTML")!!)
        val angleBracketBlocks = PsiTreeUtil.collectElements(htmlView, { it is XmlAttribute && it.text.startsWith("|") }).map { it.parent }

        // collect blocks which have the element as a child
        val validBlocks = angleBracketBlocks.filter { it ->
            val hbsFragments = PsiTreeUtil.collectElements(it) { it.elementType == HbTokenTypes.OUTER_ELEMENT_TYPE }.toList()
            val hbsParts = hbsFragments.map { element.containingFile.findElementAt(it.textOffset)!!.parent.parent }
            hbsParts.find { PsiTreeUtil.collectElements(it) { it == element.parent }.isNotEmpty() } != null
        }
        for (block in validBlocks) {
            val names = block.text.replace("|", "").split(" ")
            result.addAllElements(names.map { LookupElementBuilder.create(it) })
        }
        for (block in blocks) {
            val refs = block.children[0].children.filter { it.elementType == HbTokenTypes.ID }
            result.addAllElements(refs.map { LookupElementBuilder.create(it.text) })
        }
        if ("this".startsWith(txt)) {
            result.addElement(LookupElementBuilder.create("this"))
        }
        if (parameters.position.parent.prevSibling.elementType == HbTokenTypes.SEP) {
            resolve(parameters.position.parent.prevSibling?.prevSibling, result)
        }
        val mustache = parameters.position.parent
        val res = mustache?.references?.find { it.resolve() != null }
        if (res?.resolve() != null) {
            val psiExp = res.resolve()
            if (psiExp != null) {
                resolve(psiExp, result)
            }
        }
    }
}
