package com.emberjs.hbs

import com.dmarcotte.handlebars.psi.HbMustacheName
import com.emberjs.index.EmberNameIndex
import com.emberjs.lookup.EmberLookupElementBuilder
import com.emberjs.project.EmberProjectComponent
import com.emberjs.resolver.EmberName
import com.emberjs.resolver.EmberResolver
import com.intellij.openapi.util.Condition
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.util.CommonProcessors
import com.intellij.util.FilteringProcessor
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FindSymbolParameters
import com.intellij.psi.PsiElementResolveResult.createResults
class HbsModuleReference(element: HbMustacheName, val moduleType: String) :
        PsiPolyVariantReferenceBase<HbMustacheName>(element, TextRange(0, element.textLength), true) {

    override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
        val rootsSeq = EmberProjectComponent.getInstance(element.project)?.roots?.asSequence() ?: return emptyArray()

        val psiManager = PsiManager.getInstance(element.project)

        // Iterate over Ember.js roots of the project
        return createResults(rootsSeq.flatMap {
            val resolver = EmberResolver(it)

            // Look for type with matching name in root folder
            sequenceOf(value, value.removeSuffix("s"))
                    .map { resolver.resolve("$moduleType:$it") }
                    .distinct()
        }
                .filterNotNull()

                // Convert VirtualFile to PsiFile
                .map { psiManager.findFile(it) }
                .filterNotNull()
                .toList())
    }

    override fun getVariants(): Array<out Any?> {
        val scope = FindSymbolParameters.searchScopeFor(element.project, true)

        val collector = CommonProcessors.CollectProcessor<EmberName>()
        val filter = FilteringProcessor<EmberName>(Condition { it.type == moduleType }, collector)

        val index = FileBasedIndex.getInstance()

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
