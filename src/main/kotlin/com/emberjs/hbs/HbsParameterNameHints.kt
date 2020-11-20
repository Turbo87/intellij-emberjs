package com.emberjs.hbs

import com.dmarcotte.handlebars.parsing.HbTokenTypes
import com.dmarcotte.handlebars.psi.HbParam
import com.emberjs.utils.resolveHelper
import com.intellij.codeInsight.hints.HintInfo
import com.intellij.codeInsight.hints.InlayInfo
import com.intellij.codeInsight.hints.InlayParameterHintsProvider
import com.intellij.codeInsight.hints.Option
import com.intellij.lang.javascript.psi.types.JSTupleType
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parents
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import java.util.*


class HbsParameterNameHints : InlayParameterHintsProvider  {

    override fun getParameterHints(psiElement: PsiElement): MutableList<InlayInfo> {
        if (psiElement is HbParam) {
            val helperElement = psiElement.parents
                    .find { it.children.getOrNull(0)?.elementType == HbTokenTypes.OPEN_SEXPR }
                    ?:
                    psiElement.parents
                            .find { it.children.getOrNull(0)?.elementType == HbTokenTypes.OPEN_BLOCK }
            if (helperElement == null) {
                return emptyList<InlayInfo>().toMutableList()
            }
            val file = helperElement.children.getOrNull(0)?.references?.getOrNull(0)?.resolve()?.containingFile
            if (file != null) {
                val func = resolveHelper(file)
                val array = func?.parameters?.first()
                val names = array?.children?.getOrNull(0)?.children?.map { it.text }
                val type = array?.jsType
                val index = helperElement.parent.children.filter { it is HbParam }.indexOfFirst { it.text == psiElement.text }
                if (type is JSTupleType) {
                    val indexType = type.getTypeByIndex(index)
                    val name = names?.get(index) ?: "unknown"
                    if (indexType != null) {
                        return mutableListOf(InlayInfo("$name:", psiElement.startOffset))
                    }
                } else {
                    return mutableListOf(InlayInfo("param[$index]", psiElement.startOffset))
                }
            }
            return emptyList<InlayInfo>().toMutableList()
        }
        return emptyList<InlayInfo>().toMutableList()
    }

    override fun getHintInfo(element: PsiElement): HintInfo? {
        return null
    }

    override fun getDefaultBlackList(): MutableSet<String> {
        return Collections.emptySet()
    }

    override fun getSupportedOptions(): List<Option?> {
        return Collections.emptyList()
    }

    override fun isBlackListSupported(): Boolean {
        return false
    }
}
