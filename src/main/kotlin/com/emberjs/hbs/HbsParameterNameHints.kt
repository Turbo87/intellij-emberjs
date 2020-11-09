package com.emberjs.hbs

import com.dmarcotte.handlebars.parsing.HbTokenTypes
import com.dmarcotte.handlebars.psi.HbParam
import com.emberjs.utils.EmberUtils
import com.intellij.codeInsight.hints.HintInfo
import com.intellij.codeInsight.hints.InlayInfo
import com.intellij.codeInsight.hints.InlayParameterHintsProvider
import com.intellij.codeInsight.hints.Option
import com.intellij.lang.javascript.psi.JSDestructuringArray
import com.intellij.lang.javascript.psi.JSFunction
import com.intellij.lang.javascript.psi.JSRecordType
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTupleType
import com.intellij.lang.javascript.psi.ecma6.impl.TypeScriptTupleTypeImpl
import com.intellij.lang.javascript.psi.types.JSTupleType
import com.intellij.lang.javascript.psi.types.JSTypeImpl
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.refactoring.suggested.startOffset
import java.lang.Integer.max
import java.util.*


class HbsParameterNameHints : InlayParameterHintsProvider {

    override fun getParameterHints(psiElement: PsiElement): MutableList<InlayInfo> {
        if (psiElement is HbParam) {
            val firstParam = EmberUtils.findFirstHbsParamFromParam(psiElement)
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

            var func = EmberUtils.followReferences(firstParam)
            if (func == firstParam) {
                func = EmberUtils.followReferences(firstParam.children[0])
                if (func == firstParam.children[0]) {
                    val id = PsiTreeUtil.collectElements(firstParam) { it !is LeafPsiElement && it.elementType == HbTokenTypes.ID }.lastOrNull()
                    func = EmberUtils.followReferences(id, psiElement.text)
                }
            }

            if (func is JSFunction) {
                var arrayName: String? = null
                var array: JSType?

                var args = func.parameters.lastOrNull()?.jsType
                array = null

                if (args is JSTypeImpl) {
                   args = args.asRecordType()
                }

                if (args is JSRecordType) {
                    array = args.findPropertySignature("positional")?.jsType
                    arrayName = "positional"
                }

                if (array == null) {
                    arrayName = func.parameters.first().name ?: arrayName
                    array = func.parameters.first().jsType
                }

                val type = array
                if (type is JSTupleType) {
                    var name: String? = null
                    if (type.sourceElement is TypeScriptTupleTypeImpl) {
                        name = (type.sourceElement as TypeScriptTupleTypeImpl).members.getOrNull(index - 1)?.tupleMemberName
                    }
                    if (type.sourceElement is JSDestructuringArray) {
                        name = (type.sourceElement as JSDestructuringArray).elementsWithRest.getOrNull(index - 1)?.text
                    }
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
