package com.emberjs.hbs

import com.emberjs.index.EmberNameIndex
import com.emberjs.lookup.EmberLookupElementBuilder
import com.emberjs.resolver.EmberName
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.openapi.util.Condition
import com.intellij.util.CommonProcessors.CollectProcessor
import com.intellij.util.FilteringProcessor
import com.intellij.util.ProcessingContext
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FindSymbolParameters.searchScopeFor

/**
 * The `HbsHelperCompletionProvider` class adds all helper names
 * from the `EmberNameIndex` to the `CompletionResultSet`.
 */
class HbsHelperCompletionProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext?, result: CompletionResultSet) {
        val project = parameters.originalFile.project
        val scope = searchScopeFor(project, true)

        val collector = CollectProcessor<EmberName>()
        val filter = FilteringProcessor<EmberName>(Condition { it.type == "helper" }, collector)

        val index = FileBasedIndex.getInstance()

        // Collect all components from the index
        index.processAllKeys(EmberNameIndex.NAME, filter, scope, null)

        val elements = collector.results

                // Filter out components that are not related to this project
                .filter { index.getContainingFiles(EmberNameIndex.NAME, it, scope).isNotEmpty() }

                // Convert search results for LookupElements
                .map { EmberLookupElementBuilder.create(it) }

        result.addAllElements(elements)
    }
}
