package com.emberjs.hbs

import com.emberjs.resolver.EmberName
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult.createResults
import com.intellij.psi.ResolveResult

class HbsComponentReference(element: PsiElement) : HbsModuleReference(element, "component") {
    override fun matches(module: EmberName): Boolean {
        return super.matches(module) || (module.type == "template" && module.name == "components/$value")
    }

    override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
        if (internalComponents.properties.map { it.name }.contains(element.text)) {
            val prop = internalComponents.properties.find { it.name == element.text }
            return createResults((prop?.jsType?.sourceElement as JSReferenceExpression).resolve())
        }
        return super.multiResolve(incompleteCode)
    }
}
