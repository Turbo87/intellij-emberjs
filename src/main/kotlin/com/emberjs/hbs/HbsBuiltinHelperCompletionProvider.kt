package com.emberjs.hbs

import com.dmarcotte.handlebars.psi.impl.HbPathImpl
import com.emberjs.utils.parents
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.util.parentsWithSelf
import com.intellij.util.ProcessingContext



class HbsBuiltinHelperCompletionProvider(val helpers: List<String>) : CompletionProvider<CompletionParameters>() {
    val lookupElements = helpers.map { LookupElementBuilder.create(it) }

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val path = parameters.position.parentsWithSelf.toList().find { it is HbPathImpl }
        if (path != null && path.text.contains(".")) return
        result.addAllElements(lookupElements)
    }
}
