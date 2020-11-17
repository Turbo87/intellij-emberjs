package com.emberjs.hbs

import com.dmarcotte.handlebars.parsing.HbTokenTypes
import com.dmarcotte.handlebars.psi.impl.HbBlockWrapperImpl
import com.dmarcotte.handlebars.psi.impl.HbMustacheNameImpl
import com.dmarcotte.handlebars.psi.impl.HbPathImpl
import com.emberjs.utils.parents
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.lang.ecmascript6.psi.JSClassExpression
import com.intellij.lang.ecmascript6.psi.impl.ES6ClassExpressionImpl
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.lang.javascript.JSDocCompositeElementType
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.completion.JSCompletionContributor
import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSField
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.ecma6.impl.TypeScriptVariableImpl
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.lang.javascript.psi.impl.JSThisExpressionImpl
import com.intellij.lang.javascript.psi.impl.JSVariableImpl
import com.intellij.lang.javascript.psi.jsdoc.impl.JSDocCommentImpl
import com.intellij.lang.javascript.psi.types.JSRecordTypeImpl
import com.intellij.lang.javascript.psi.types.JSSimpleRecordTypeImpl
import com.intellij.lang.javascript.psi.types.JSTypeBaseImpl
import com.intellij.lang.javascript.types.JSDocCommentElementType
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.util.Consumer
import com.intellij.util.ProcessingContext
import javax.swing.Icon
import kotlin.math.max


class HbsLocalCompletion : CompletionProvider<CompletionParameters>() {

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

        if (refElement is JSClassExpression) {
            result.addAllElements(refElement.fields.map { LookupElementBuilder.create(it.name!!) })
            result.addAllElements(refElement.functions.map { LookupElementBuilder.create(it.name!!) })
        }

        if (refElement is JSField) {
            if (refElement.jsType?.sourceElement is JSDocCommentImpl) {
                val doc = refElement.jsType?.sourceElement as JSDocCommentImpl
                if (doc.tags[0].value?.reference?.resolve() != null) {
                    resolve(doc.tags[0].value?.reference?.resolve()!!, result)
                }
                return
            }
            if (refElement.jsType is JSRecordTypeImpl) {
                val names = (refElement.jsType as JSRecordTypeImpl).propertyNames
                result.addAllElements(names.map { LookupElementBuilder.create(it) })
                return
            }
            if (refElement is JSVariableImpl<*,*> && refElement.doGetExplicitlyDeclaredType() != null) {
                val jstype = refElement.doGetExplicitlyDeclaredType()
                if (jstype is JSRecordTypeImpl) {
                    result.addAllElements(jstype.propertyNames.map { LookupElementBuilder.create(it) })
                }
            }
        }
    }

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val regex = Regex("\\|.*\\|")
        val txt = parameters.position.parents.find { it is HbPathImpl }?.text!!.replace("IntellijIdeaRulezzz", "")
        val blocks = PsiTreeUtil.collectElements(parameters.originalFile.node.psi) { it.text.contains(regex) }
                .filter { it is HbBlockWrapperImpl || it.toString().endsWith("CONTENT)") }
        for (block in blocks) {
            if (block is HbBlockWrapperImpl) {
                val refs = block.children[0].children.filter { it.elementType.toString().endsWith("ID") }
                result.addAllElements(refs.map { LookupElementBuilder.create(it.text) })
            } else {
                val reg = Regex("\\|([a-z A-Z0-9]*)\\|")
                val refs = reg.find(block.text)!!.groups[1]!!.value.split(" ")
                result.addAllElements(refs.map { LookupElementBuilder.create(it) })
            }
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
