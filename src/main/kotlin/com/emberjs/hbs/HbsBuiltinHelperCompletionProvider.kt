package com.emberjs.hbs

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.util.ProcessingContext

class HbsBuiltinHelperCompletionProvider(vararg helpers: String) : CompletionProvider<CompletionParameters>() {
    val lookupElements = helpers.map { LookupElementBuilder.create(it) }

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext?, result: CompletionResultSet) {
        result.addAllElements(lookupElements)
    }
}
