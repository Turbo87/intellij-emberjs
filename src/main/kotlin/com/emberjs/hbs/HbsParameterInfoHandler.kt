package com.emberjs.hbs

import com.dmarcotte.handlebars.parsing.HbTokenTypes
import com.dmarcotte.handlebars.psi.HbParam
import com.emberjs.utils.findFirstHbsParamFromParam
import com.emberjs.utils.followReferences
import com.emberjs.utils.resolveHelper
import com.emberjs.utils.resolveModifier
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.types.JSArrayType
import com.intellij.lang.javascript.psi.types.JSTupleType
import com.intellij.lang.parameterInfo.*
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parents


class HbsParameterInfoHandler : ParameterInfoHandler<PsiElement, JSParameterListElement?> {
    override fun couldShowInLookup(): Boolean {
        return true
    }

    override fun getParametersForLookup(item: LookupElement?, context: ParameterInfoContext?): Array<JSParameterListElement>? {
        val psiElement = context?.file?.findElementAt(context.offset)
        val helper = findFirstHbsParamFromParam(psiElement)

        val file = helper?.references?.getOrNull(0)?.resolve()?.containingFile
        if (file == null) {
            return null
        }
        return resolveHelper(file)?.parameters
    }

    private fun findHelperFunction(psiElement: PsiElement?): JSFunction? {
        val helper = findFirstHbsParamFromParam(psiElement)

        val file = followReferences(helper)
        return if (file is PsiFile) resolveHelper(file) else if (file is JSFunction) file else null
    }

    override fun findElementForParameterInfo(context: CreateParameterInfoContext): PsiElement? {
        val psiElement = context.file.findElementAt(context.offset)
        val block = findFirstHbsParamFromParam(psiElement)
        val ref = followReferences(block)
        if (ref == null) {
            return null
        }
        val func = resolveHelper(ref.containingFile)
        if (func == null) {
            val modifier = resolveModifier(ref.containingFile)
            if (modifier.filterNotNull().isNotEmpty()) {
                val args = emptyList<Any>().toMutableList()
                val argType = modifier.first()?.parameters?.getOrNull(2)?.jsType
                        ?: modifier[1]?.parameters?.getOrNull(1)?.jsType
                        ?: modifier[2]?.parameters?.getOrNull(1)?.jsType
                if (argType is JSRecordType) {
                    val positional = argType.findPropertySignature("positional")
                    if (positional != null) {
                        args.add(positional)
                    }
                    val named = argType.findPropertySignature("named")
                    if (named != null) {
                        args.add(named)
                    }
                }
                context.itemsToShow = args.toTypedArray()
            }
        } else {
            context.itemsToShow = func.parameters
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

    override fun updateUI(p: JSParameterListElement?, context: ParameterInfoUIContext) {
        var text = ""
        if (p == null) {
            return
        }
        if (p.inferredType is JSArrayType || p.inferredType is JSTupleType) {
            val array = p
            val type = array.inferredType
            if (type is JSTupleType) {
                val names = if (p.inferredType is JSTupleType) (p.inferredType as JSTupleType).names else emptyList()
                text += names.mapIndexed { index, s -> "$s:${type.getTypeByIndex(index) ?: "unknown"}" }
            } else {
                val arrayType = type as JSArrayType
                text += array.name + ":" + (arrayType.type?.resolvedTypeText ?: "*")
            }
        }

        if (p.inferredType is JSRecordType) {
            val type = p.inferredType as JSRecordType
            text += type.properties.map { it.memberName + ":" + it.jsType?.resolvedTypeText + "=" }.joinToString(",")
        }

        if (text == "") {
            return
        }
        context.setupUIComponentPresentation(text, 0, 0, false, false, false, context.defaultParameterColor)
    }

}
