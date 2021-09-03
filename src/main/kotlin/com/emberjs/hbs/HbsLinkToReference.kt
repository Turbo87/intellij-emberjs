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

class HbsLinkToReference(element: PsiElement, range: TextRange, val moduleName: String) :
        PsiPolyVariantReferenceBase<PsiElement>(element, range, true) {

    private val project = element.project
    private val scope = ProjectScope.getAllScope(project)

    private val psiManager: PsiManager by lazy { PsiManager.getInstance(project) }

    override fun getVariants(): Array<out Any> {
        // Collect all components from the index
        return EmberNameIndex.getFilteredProjectKeys(scope) { matches(it) }
                // Convert search results for LookupElements
                .map { EmberLookupElementBuilder.create(it) }
                .toTypedArray()
    }

    override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
        // Collect all components from the index
        return EmberNameIndex.getFilteredFiles(scope) { matches(it) && it.name == moduleName }
                // Convert search results for LookupElements
                .mapNotNull { psiManager.findFile(it) }
                .let(::createResults)
    }

    fun matches(module: EmberName): Boolean {
        if (module.type == "template")
            return !module.name.startsWith("components/")

        return module.type in MODULE_TYPES
    }

    companion object {
        val MODULE_TYPES = arrayOf("controller", "route")
    }
}
