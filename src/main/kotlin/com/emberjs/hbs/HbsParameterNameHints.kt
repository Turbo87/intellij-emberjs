package com.emberjs.hbs

import com.dmarcotte.handlebars.parsing.HbTokenTypes
import com.dmarcotte.handlebars.psi.HbMustacheName
import com.dmarcotte.handlebars.psi.HbParam
import com.emberjs.utils.followReferences
import com.emberjs.utils.resolveHelper
import com.emberjs.utils.resolveModifier
import com.intellij.codeInsight.hints.HintInfo
import com.intellij.codeInsight.hints.InlayInfo
import com.intellij.codeInsight.hints.InlayParameterHintsProvider
import com.intellij.codeInsight.hints.Option
import com.intellij.lang.javascript.psi.*
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
                            .find { it.children.getOrNull(0)?.elementType == HbTokenTypes.OPEN }
            if (helperBlock == null) {
                return emptyList<InlayInfo>().toMutableList()
            }
            var index = helperBlock.children.filter { it is HbParam }.indexOfFirst { it.text == psiElement.text }
            val helperElement = helperBlock.children.getOrNull(1)?.children?.getOrNull(0)
            val modifierElement = helperBlock.children.getOrNull(1)
            if (index <= 0 && modifierElement !is HbMustacheName) {
                // if its the helper itself
                return emptyList<InlayInfo>().toMutableList()
            } else {
                index += 1
            }


            var file = followReferences(helperElement)
            if (file == helperElement) {
                file = followReferences(modifierElement)
            }

            if (file != null) {
                var func = if (file is PsiFile) resolveHelper(file) else if (file is JSFunction) file else null
                var arrayName: String? = null
                var array: JSType? =  null

                if (func != null) {
                    arrayName = func.parameters.first().name ?: arrayName
                    array = func.parameters.first().jsType
                } else {
                    val modifier = resolveModifier(file.containingFile)
                    if (modifier != null) {
                        val args = modifier.parameters.lastOrNull()?.inferredType
                        array = null
                        if (args is JSRecordType) {
                            array = args.findPropertySignature("positional")?.jsType
                            arrayName = "positional"
                        }
                    }
                }

                val type = array
                if (type is JSTupleType && type.sourceElement != null) {
                    val name = type.sourceElement!!.children.map { it.text }.getOrNull(index-1)
                    if (name != null) {
                        return mutableListOf(InlayInfo(name, psiElement.startOffset))
                    } else {
                        if (index == 1 && arrayName != null) {
                            return mutableListOf(InlayInfo(arrayName, psiElement.startOffset))
                        }
                    }

                }
            }
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
