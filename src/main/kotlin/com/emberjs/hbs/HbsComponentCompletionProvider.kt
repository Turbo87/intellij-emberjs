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

/**
 * The `HbsComponentCompletionProvider` class adds all component names
 * from the `EmberNameIndex` to the `CompletionResultSet`.
 */
class HbsComponentCompletionProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext?, result: CompletionResultSet) {
        val collector = CollectProcessor<EmberName>()
        val filter = FilteringProcessor<EmberName>(Condition { it.type == "component" }, collector)
        val scope = parameters.position.resolveScope

        FileBasedIndex.getInstance().processAllKeys(EmberNameIndex.NAME, filter, scope, null)

        result.addAllElements(collector.results.map { EmberLookupElementBuilder.create(it) })
    }
}
