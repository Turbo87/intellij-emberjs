package com.emberjs.hbs

import com.emberjs.index.EmberNameIndex
import com.emberjs.lookup.EmberLookupElementBuilder
import com.emberjs.resolver.ClassOrFileReference
import com.emberjs.resolver.EmberName
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.PsiElementResolveResult.createResults
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

        return EmberNameIndex.getFilteredKeys(scope) { matches(it) }

                // Filter out components that are not related to this project
                .flatMap { EmberNameIndex.getContainingFiles(it, scope) }

                // Convert search results for LookupElements
                .map { psiManager.findFile(it) }
                .filterNotNull()
                .map { ClassOrFileReference(it).resolve() }
                .let(::createResults)
    }

    override fun getVariants(): Array<out Any?> {

        // Collect all components from the index
        return EmberNameIndex.getFilteredKeys(scope) { it.type == moduleType }

                // Filter out components that are not related to this project
                .filter { EmberNameIndex.hasContainingFiles(it, scope) }

                // Convert search results for LookupElements
                .map { EmberLookupElementBuilder.create(it, dots = false) }
                .toTypedArray()
    }
}
