package com.emberjs.hbs

import com.emberjs.hbs.HbsPatterns.BLOCK_MUSTACHE_NAME_PATTERN
import com.emberjs.hbs.HbsPatterns.SIMPLE_MUSTACHE_NAME_PATTERN
import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionType

/**
 * The `HbsCompletionContributor` class is responsible for registering all
 * available `CompletionProviders` for the Handlebars language to their
 * corresponding `PsiElementPatterns`.
 */
class HbsCompletionContributor : CompletionContributor() {
    init {
        extend(CompletionType.BASIC, SIMPLE_MUSTACHE_NAME_PATTERN, HbsComponentCompletionProvider())
        extend(CompletionType.BASIC, BLOCK_MUSTACHE_NAME_PATTERN, HbsComponentCompletionProvider())
    }
}
