package com.emberjs.hbs

import com.emberjs.index.EmberNameIndex
import com.emberjs.lookup.EmberLookupElementBuilder
import com.emberjs.resolver.EmberName
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult.createResults
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.psi.search.ProjectScope

open class HbsModuleReference(element: PsiElement, val moduleType: String) :
        PsiPolyVariantReferenceBase<PsiElement>(element, TextRange(0, element.textLength), true) {

    val project = element.project
    private val scope = ProjectScope.getAllScope(project)

    private val psiManager: PsiManager by lazy { PsiManager.getInstance(project) }

    open fun matches(module: EmberName) =
            module.type == moduleType && module.name == value

    override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
        // Collect all components from the index
        return EmberNameIndex.getFilteredFiles(scope) { matches(it) }
                // Convert search results for LookupElements
                .mapNotNull { psiManager.findFile(it) }
                .let(::createResults)
    }

    override fun getVariants(): Array<out Any?> {
        // Collect all components from the index
        return EmberNameIndex.getFilteredProjectKeys(scope) { it.type == moduleType }
                // Convert search results for LookupElements
                .map { EmberLookupElementBuilder.create(it, dots = false) }
                .toTypedArray()
    }
}
