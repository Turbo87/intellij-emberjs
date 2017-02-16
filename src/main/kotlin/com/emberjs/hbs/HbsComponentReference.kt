package com.emberjs.hbs

import com.emberjs.resolver.EmberName
import com.intellij.psi.PsiElement

class HbsComponentReference(element: PsiElement) : HbsModuleReference(element, "component") {
    override fun matches(module: EmberName): Boolean {
        return super.matches(module) || (module.type == "template" && module.name == "components/$value")
    }
}
