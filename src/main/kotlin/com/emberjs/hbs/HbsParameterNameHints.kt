package com.emberjs.hbs

import com.dmarcotte.handlebars.parsing.HbTokenTypes
import com.dmarcotte.handlebars.psi.HbMustacheName
import com.dmarcotte.handlebars.psi.HbParam
import com.emberjs.utils.findFirstHbsParamFromParam
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
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parents
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import java.lang.Integer.max
import java.util.*


class HbsParameterNameHints : InlayParameterHintsProvider {

    override fun getParameterHints(psiElement: PsiElement): MutableList<InlayInfo> {
        if (psiElement is HbParam) {
            val firstParam = findFirstHbsParamFromParam(psiElement)
            if (firstParam == null) {
                return emptyList<InlayInfo>().toMutableList()
            }
            var index = max(
                    firstParam.parent.children.filter { it is HbParam }.indexOf(psiElement),
                    firstParam.parent.parent.children.filter { it is HbParam }.indexOf(psiElement)
            )
            if (firstParam !is HbParam) {
                index += 1
            }
            if (index <= 0) {
                // if its the helper itself
                return emptyList<InlayInfo>().toMutableList()
            }

            var file = followReferences(firstParam)
            if (file == firstParam) {
                file = followReferences(firstParam.children[0])
                if (file == firstParam.children[0]) {
                    val id = PsiTreeUtil.collectElements(firstParam) { it !is LeafPsiElement && it.elementType == HbTokenTypes.ID }.lastOrNull()
                    file = followReferences(id)
                }
            }

            if (file != null) {
                var func = resolveHelper(file.containingFile)
                var arrayName: String? = null
                var array: JSType? = null

                if (func != null) {
                    arrayName = func.parameters.first().name ?: arrayName
                    array = func.parameters.first().jsType
                } else {
                    val modifier = resolveModifier(file.containingFile)
                    val args = modifier.first()?.parameters?.getOrNull(2)?.jsType
                            ?: modifier[1]?.parameters?.getOrNull(1)?.jsType
                            ?: modifier[2]?.parameters?.getOrNull(1)?.jsType
                    array = null
                    if (args is JSRecordType) {
                        array = args.findPropertySignature("positional")?.jsType
                        arrayName = "positional"
                    }
                }

                val type = array
                if (type is JSTupleType && type.sourceElement != null) {
                    val name = type.sourceElement!!.children.map { it.text }.getOrNull(index - 1)
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
