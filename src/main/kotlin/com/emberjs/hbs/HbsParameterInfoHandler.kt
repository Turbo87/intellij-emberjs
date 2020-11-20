package com.emberjs.hbs

import com.dmarcotte.handlebars.parsing.HbTokenTypes
import com.dmarcotte.handlebars.psi.HbParam
import com.emberjs.utils.followReferences
import com.emberjs.utils.resolveHelper
import com.emberjs.utils.resolveModifier
import com.intellij.codeInsight.hints.InlayInfo
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.lang.javascript.psi.JSFunction
import com.intellij.lang.javascript.psi.JSParameterListElement
import com.intellij.lang.javascript.psi.types.JSTupleType
import com.intellij.lang.parameterInfo.*
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parents


class HbsParameterInfoHandler : ParameterInfoHandler<PsiElement, JSFunction> {
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

    override fun findElementForParameterInfo(context: CreateParameterInfoContext): JSFunction? {
        val psiElement = context.file.findElementAt(context.offset)
        val func = findHelperFunction(psiElement)
        context.itemsToShow = func?.parameters
        return func
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

    override fun updateUI(p: JSFunction?, context: ParameterInfoUIContext) {
        var text = ""
        if (p == null) {
            return
        }
        val modifier = resolveModifier(p.containingFile)
        if (modifier != null) {
            val param = p.parameters.lastOrNull()
            param?.jsType
        }
        val array = p.parameters.firstOrNull()
        val hash = p.parameters.lastOrNull()
        if (array != null) {
            val names = array.children.getOrNull(0)?.children?.map { it.text }
            val type = array.jsType
            if (type is JSTupleType) {
                text += names.mapIndexed { index, s -> "$s:${type.getTypeByIndex(index) ?: "unknown"}" }
            } else {
                text += "params:" + (array.jsType?.resolvedTypeText ?: "*")
            }
        }

        if (hash != null) {
            val type = hash.jsType
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
