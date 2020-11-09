package com.emberjs.hbs

import com.dmarcotte.handlebars.parsing.HbTokenTypes
import com.dmarcotte.handlebars.psi.HbMustache
import com.dmarcotte.handlebars.psi.HbParam
import com.dmarcotte.handlebars.psi.HbStringLiteral
import com.dmarcotte.handlebars.psi.impl.HbBlockWrapperImpl
import com.dmarcotte.handlebars.psi.impl.HbPathImpl
import com.emberjs.lookup.HbsInsertHandler
import com.emberjs.utils.*
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder as IntelijLookupElementBuilder
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
import com.intellij.psi.xml.XmlAttribute
import com.intellij.util.ProcessingContext


class LookupElementBuilder {
    companion object {
        fun create(name: String): com.intellij.codeInsight.lookup.LookupElementBuilder {
            return IntelijLookupElementBuilder.create(name)
                    .withInsertHandler(HbsInsertHandler())
        }
    }
}


class HbsLocalCompletion : CompletionProvider<CompletionParameters>() {

    fun resolveJsType(jsType: JSType?, result: CompletionResultSet, suffix:String="") {
        if (jsType is JSRecordTypeImpl) {
            val names = (jsType).propertyNames
            result.addAllElements(names.map { LookupElementBuilder.create(it + suffix) })
            return
        }
        if (jsType?.sourceElement is JSDocCommentImpl) {
            val doc = jsType.sourceElement as JSDocCommentImpl
            if (doc.tags[0].value?.reference?.resolve() != null) {
                resolve(doc.tags[0].value?.reference?.resolve()!!, result)
            }
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
        val file = EmberUtils.followReferences(element.children[0])
        var func: JSFunction? = null
        if (file is JSFunction) {
            func = file
        }
        if (file is PsiFile) {
            func = EmberUtils.resolveHelper(file)
        }

        if (func != null) {
            val hash = func.parameterList?.parameters?.last()
            resolveJsType(hash?.jsType ?: hash?.inferredType, result, "=")
        }
    }

    fun addImportPathCompletions(element: PsiElement, result: CompletionResultSet) {
        if (element.elementType == HbTokenTypes.STRING && element.parents.find { it is HbMustache && it.children[1].text == "import"} != null) {
            var text = element.text.replace("IntellijIdeaRulezzz ", "")
            text = text.substring(1, text.length)
            if (text == "") {
                result.addElement(LookupElementBuilder.create("~/"))
                result.addElement(LookupElementBuilder.create("."))
            }
            var rootFolder = element.originalVirtualFile?.parentEmberModule
            if (text.startsWith(".")) {
                rootFolder = element.originalVirtualFile?.parent
                var i = 1
                while (text[i] == '.') {
                    rootFolder = rootFolder?.parent
                    i++
                }
            }
            if (!text.startsWith(".") && !text.startsWith("~")) {
                rootFolder = rootFolder?.findChild("node_modules")
            }
            var path = text.split("/")
            path = path.dropLast(1)
            path.forEach {
                if (rootFolder !=  null && rootFolder!!.isEmberAddonFolder) {
                    rootFolder = rootFolder?.findChild("addon")
                }
                rootFolder = rootFolder?.findChild(it) ?: rootFolder
            }
            if (rootFolder != null) {
                val validExtensions = arrayOf("css", "js", "ts")
                val names = rootFolder!!.children.filter { validExtensions.contains(it.name.split(".").last()) }
                        .map {
                            val name = it.name + if(it.isDirectory) "/" else ""
                            LookupElementBuilder.create(name.split(".").first())
                        }
                result.addAllElements(names)
            }
        }
    }

    fun addImportCompletions(element: PsiElement, result: CompletionResultSet) {
        val imports = PsiTreeUtil.collectElements(element.containingFile, { it is HbMustache && it.children[1].text == "import"}).map { it }
        imports.forEach() {
            val importNames = it.children[2].text
                    .replace("\"", "")
                    .replace("'", "")
            if (importNames.contains("*")) {
                val name = importNames.split(" as ").last()
                result.addElement(LookupElementBuilder.create(name))
            }
            val names = it.children[2].text.split(",")
            result.addAllElements(names.map { LookupElementBuilder.create(it.replace(" ", "")) })
        }
    }

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val regex = Regex("\\|.*\\|")
        var element = parameters.position
        if (element is LeafPsiElement) {
            element = element.parent
        }
        val txt = element.parents.find { it is HbPathImpl || it is HbStringLiteral }?.text!!.replace("IntellijIdeaRulezzz", "")

        val helperElement = EmberUtils.findFirstHbsParamFromParam(element)
        if (helperElement != null) {
            addHelperCompletions(helperElement, result)
        }

        if (parameters.position.parent.prevSibling.elementType == HbTokenTypes.SEP) {
            resolve(parameters.position.parent.prevSibling?.prevSibling, result)
            return
        }

        addImportPathCompletions(element, result)
        addImportCompletions(element, result)

        // find all |blocks| from mustache
        val blocks = PsiTreeUtil.collectElements(element.containingFile) { it is HbBlockWrapperImpl }
                .filter { it.children[0].text.contains(regex) }
                .filter { it.textRange.contains(element.textRange) }

        // find all |blocks| from component tags, needs html view
        val htmlView = parameters.originalFile.viewProvider.getPsi(Language.findLanguageByID("HTML")!!)
        val angleBracketBlocks = PsiTreeUtil.collectElements(htmlView, { it is XmlAttribute && it.text.startsWith("|") }).map { it.parent }

        // collect blocks which have the element as a child
        val validBlocks = angleBracketBlocks.filter { it ->
            it.textRange.contains(element.textRange)
        }
        for (block in validBlocks) {
            val attrString = block.children.filter { it is XmlAttribute }.map { it.text }.joinToString(" ")
            val names = Regex("\\|.*\\|").find(attrString)!!.groups[0]!!.value.replace("|", "").split(" ")
            result.addAllElements(names.map { LookupElementBuilder.create(it) })
        }
        for (block in blocks) {
            val refs = block.children[0].children.filter { it.elementType == HbTokenTypes.ID }
            result.addAllElements(refs.map { LookupElementBuilder.create(it.text) })
        }
        if ("this".startsWith(txt)) {
            result.addElement(LookupElementBuilder.create("this"))
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
