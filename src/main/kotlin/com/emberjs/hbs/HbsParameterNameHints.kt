package com.emberjs.hbs

import com.dmarcotte.handlebars.parsing.HbTokenTypes
import com.dmarcotte.handlebars.psi.HbParam
import com.emberjs.utils.followReferences
import com.emberjs.utils.resolveHelper
import com.intellij.codeInsight.hints.HintInfo
import com.intellij.codeInsight.hints.InlayInfo
import com.intellij.codeInsight.hints.InlayParameterHintsProvider
import com.intellij.codeInsight.hints.Option
import com.intellij.lang.javascript.psi.JSFunction
import com.intellij.lang.javascript.psi.types.JSTupleType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parents
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import java.util.*


class HbsParameterNameHints : InlayParameterHintsProvider  {

    override fun getParameterHints(psiElement: PsiElement): MutableList<InlayInfo> {
        if (psiElement is HbParam) {
            val helperBlock = psiElement.parents
                    .find { it.children.getOrNull(0)?.elementType == HbTokenTypes.OPEN_SEXPR }
                    ?:
                    psiElement.parents
                            .find { it.children.getOrNull(0)?.elementType == HbTokenTypes.OPEN_BLOCK }
            if (helperBlock == null) {
                return emptyList<InlayInfo>().toMutableList()
            }
            val index = helperBlock.children.filter { it is HbParam }.indexOfFirst { it.text == psiElement.text }
            if (index <= 0) {
                // if its the helper itself
                return emptyList<InlayInfo>().toMutableList()
            }

            val helperElement = helperBlock.children.getOrNull(1)?.children?.getOrNull(0)
            val file = followReferences(helperElement)
            if (file != null) {
                val func = if (file is PsiFile) resolveHelper(file) else if (file is JSFunction) file else null
                val array = func?.parameters?.first()
                val names = array?.children?.getOrNull(0)?.children?.map { it.text }
                val type = array?.jsType
                if (type is JSTupleType) {
                    val name = names?.getOrNull(index-1) ?: "unknown"
                    return mutableListOf(InlayInfo(name, psiElement.startOffset))
                } else {
                    if (index == 0 && array?.name != null) {
                        return mutableListOf(InlayInfo(array.name!!, psiElement.startOffset))
                    }
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
