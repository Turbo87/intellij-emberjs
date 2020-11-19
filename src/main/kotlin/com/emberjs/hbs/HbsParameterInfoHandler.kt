package com.emberjs.hbs

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.lang.parameterInfo.*
import com.intellij.psi.PsiElement

class HbsParameterInfoHandler : ParameterInfoHandler<PsiElement, Any> {
    override fun couldShowInLookup(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getParametersForLookup(item: LookupElement?, context: ParameterInfoContext?): Array<Any?>? {
        TODO("Not yet implemented")
    }

    override fun findElementForParameterInfo(context: CreateParameterInfoContext): PsiElement? {
        TODO("Not yet implemented")
    }

    override fun showParameterInfo(element: PsiElement, context: CreateParameterInfoContext) {
        TODO("Not yet implemented")
    }

    override fun findElementForUpdatingParameterInfo(context: UpdateParameterInfoContext): PsiElement? {
        TODO("Not yet implemented")
    }

    override fun updateParameterInfo(parameterOwner: PsiElement, context: UpdateParameterInfoContext) {
        TODO("Not yet implemented")
    }

    override fun updateUI(p: Any?, context: ParameterInfoUIContext) {
        TODO("Not yet implemented")
    }

}
