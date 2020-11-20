package com.emberjs.hbs

import com.dmarcotte.handlebars.parsing.HbTokenTypes
import com.dmarcotte.handlebars.psi.HbParam
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
import org.jetbrains.annotations.NotNull


class HbsParameterInfoHandler : ParameterInfoHandler<PsiElement, JSParameterListElement?> {
    override fun couldShowInLookup(): Boolean {
        return true
    }

    override fun getParametersForLookup(item: LookupElement?, context: ParameterInfoContext?): Array<JSParameterListElement>? {
        val psiElement = context?.file?.findElementAt(context.offset)
        val helperElement = psiElement?.parents
                ?.find { it.children.getOrNull(0)?.elementType == HbTokenTypes.OPEN_SEXPR }
                ?:
                psiElement?.parents
                        ?.find { it.children.getOrNull(0)?.elementType == HbTokenTypes.OPEN_BLOCK }
        if (helperElement == null) {
            return null
    }

        val file = helperElement.children.getOrNull(1)?.children?.getOrNull(0)?.references?.getOrNull(0)?.resolve()?.containingFile
        if (file == null) {
            return null
        }
        return resolveHelper(file)?.parameters
    }

    private fun findHelperFunction(psiElement: PsiElement?): JSFunction? {
        val helperBlock = psiElement?.parents
                ?.find { it.children.getOrNull(0)?.elementType == HbTokenTypes.OPEN_SEXPR }
                ?:
                psiElement?.parents
                        ?.find { it.children.getOrNull(0)?.elementType == HbTokenTypes.OPEN_BLOCK }
        if (helperBlock == null) {
            return null
        }
        val helperElement = helperBlock.children.getOrNull(1)?.children?.getOrNull(0)

        val file = followReferences(helperElement)
        return if (file is PsiFile) resolveHelper(file) else if (file is JSFunction) file else null
    }

    override fun findElementForParameterInfo(context: CreateParameterInfoContext): PsiElement? {
        val psiElement = context.file.findElementAt(context.offset)
        val func = findHelperFunction(psiElement)
        if (func != null) {
            val modifier = resolveModifier(func.containingFile)
            if (modifier != null) {
                context.itemsToShow = modifier.parameters.toList().takeLast(1).toTypedArray()
                return modifier
            } else {
                context.itemsToShow = func.parameters
            }

            return psiElement
        }
        return null
    }

    override fun showParameterInfo(element: PsiElement, context: CreateParameterInfoContext) {
        context.showHint(element, element.textOffset, this)
    }

    override fun findElementForUpdatingParameterInfo(context: UpdateParameterInfoContext): PsiElement? {
        val psiElement = context.file.findElementAt(context.offset)
        return findHelperFunction(psiElement)
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
            val names = array.children.getOrNull(0)?.children?.map { it.text }
            val type = array.jsType
            if (type is JSTupleType) {
                text += names.mapIndexed { index, s -> "$s:${type.getTypeByIndex(index) ?: "unknown"}" }
            } else {
                text += "params:" + (array.inferredType?.resolvedTypeText ?: "*")
            }
        }

        if (p.inferredType is JSRecordType) {
            val type = p.inferredType
            if (type != null) {
                text += type.resolvedTypeText
            }
        }

        if (text == "") {
            return
        }
        context.setupUIComponentPresentation(text, -1, -1, false, false, false, context.defaultParameterColor)
    }

}
