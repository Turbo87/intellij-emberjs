package com.emberjs.hbs

import com.dmarcotte.handlebars.psi.HbMustacheName
import com.emberjs.index.EmberNameIndex
import com.emberjs.lookup.EmberLookupElementBuilder
import com.emberjs.resolver.EmberName
import com.intellij.openapi.util.Condition
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElementResolveResult.createResults
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.psi.search.ProjectScope
import com.intellij.util.CommonProcessors.CollectProcessor
import com.intellij.util.FilteringProcessor
import com.intellij.util.indexing.FileBasedIndex

open class HbsModuleReference(element: HbMustacheName, val moduleType: String) :
        PsiPolyVariantReferenceBase<HbMustacheName>(element, TextRange(0, element.textLength), true) {

    val project = element.project
    private val scope = ProjectScope.getAllScope(project)

    private val index: FileBasedIndex by lazy { FileBasedIndex.getInstance() }
    private val psiManager: PsiManager by lazy { PsiManager.getInstance(project) }

    open fun matches(module: EmberName) =
            module.type == moduleType && module.name == value

    override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
        val collector = CollectProcessor<EmberName>()
        val filter = FilteringProcessor<EmberName>(Condition { matches(it) }, collector)

        // Collect all components from the index
        index.processAllKeys(EmberNameIndex.NAME, filter, scope, null)

        return collector.results

                // Filter out components that are not related to this project
                .flatMap { index.getContainingFiles(EmberNameIndex.NAME, it, scope) }

                // Convert search results for LookupElements
                .map { psiManager.findFile(it) }
                .filterNotNull()
                .let { createResults(it) }
    }

    override fun getVariants(): Array<out Any?> {
        val collector = CollectProcessor<EmberName>()
        val filter = FilteringProcessor<EmberName>(Condition { it.type == moduleType }, collector)

        // Collect all components from the index
        index.processAllKeys(EmberNameIndex.NAME, filter, scope, null)

        return collector.results

                // Filter out components that are not related to this project
                .filter { index.getContainingFiles(EmberNameIndex.NAME, it, scope).isNotEmpty() }

                // Convert search results for LookupElements
                .map { EmberLookupElementBuilder.create(it) }
                .toTypedArray()
    }
}
