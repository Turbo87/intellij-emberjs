package com.emberjs.hbs

import com.dmarcotte.handlebars.parsing.HbTokenTypes
import com.dmarcotte.handlebars.psi.HbParam
import com.emberjs.utils.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.types.JSArrayType
import com.intellij.lang.javascript.psi.types.JSTupleType
import com.intellij.lang.parameterInfo.*
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parents


class HbsParameterInfoHandler : ParameterInfoHandler<PsiElement, Any?> {
    override fun couldShowInLookup(): Boolean {
        return true
    }

    override fun getParametersForLookup(item: LookupElement?, context: ParameterInfoContext?): Array<*>? {
        val psiElement = context?.file?.findElementAt(context.offset)
        val helper = EmberUtils.findFirstHbsParamFromParam(psiElement)

        val file = helper?.references?.getOrNull(0)?.resolve()?.containingFile
        if (file == null) {
            return null
        }
        return EmberUtils.resolveHelper(file)?.parameters
    }

    private fun findHelperFunction(psiElement: PsiElement?): JSFunction? {
        val helper = EmberUtils.findFirstHbsParamFromParam(psiElement)

        val file = EmberUtils.followReferences(helper)
        return if (file is PsiFile) EmberUtils.resolveHelper(file) else if (file is JSFunction) file else null
    }

    override fun findElementForParameterInfo(context: CreateParameterInfoContext): PsiElement? {
        val psiElement = context.file.findElementAt(context.offset)
        val block = EmberUtils.findFirstHbsParamFromParam(psiElement)
        val ref = EmberUtils.followReferences(block)
        if (ref == null || ref !is JSFunction) {
            return null
        }
        val func: JSFunction = ref
        val args = emptyList<Any>().toMutableList()
        val argType = func.parameters.last().jsType
        if (argType is JSRecordType) {
            val positional = argType.findPropertySignature("positional")
            if (positional != null) {
                args.add(positional)
            }
            val named = argType.findPropertySignature("named")
            if (named != null) {
                args.add(named)
            }
            if (args.size > 0) {
                context.itemsToShow = args.toTypedArray()
            } else {
                context.itemsToShow = func.parameters
            }
        }

        return psiElement
    }

    override fun showParameterInfo(element: PsiElement, context: CreateParameterInfoContext) {
        context.showHint(element, element.textOffset, this)
    }

    override fun findElementForUpdatingParameterInfo(context: UpdateParameterInfoContext): PsiElement? {
        return context.file.findElementAt(context.offset)
    }

    override fun updateParameterInfo(parameterOwner: PsiElement, context: UpdateParameterInfoContext) {
        val psiElement = context.file.findElementAt(context.offset)
        if (psiElement == null) {
            return
        }
        val currentParam = psiElement.parent.children.filter { it is HbParam }.indexOf(psiElement) - 1
        context.setCurrentParameter(currentParam)
    }

    override fun updateUI(p: Any?, context: ParameterInfoUIContext) {
        var text = ""
        if (p == null) {
            return
        }
        var type: JSType? = null
        var arrayName: String? = null
        if (p is JSParameterListElement) {
            arrayName = p.name
            type = p.inferredType
        }
        if (p is JSRecordType.PropertySignature) {
            arrayName = p.memberName
            type = p.jsType
        }
        if (type is JSArrayType || type is JSTupleType) {
            if (type is JSTupleType) {
                val names = type.sourceElement?.children?.map { it.text } ?: emptyList<String>()
                text += names.mapIndexed { index, s -> "$s:${type.getTypeByIndex(index) ?: "unknown"}" }.joinToString(",")
            } else {
                val arrayType = type as JSArrayType
                text += arrayName + ":" + (arrayType.type?.resolvedTypeText ?: "*")
            }
        }

        if (type is JSRecordType) {
            text += type.properties.map { it.memberName + ":" + it.jsType?.resolvedTypeText + "=" }.joinToString(",")
        }


        if (text == "") {
            return
        }
        context.setupUIComponentPresentation(text, 0, 0, false, false, false, context.defaultParameterColor)
    }

}
