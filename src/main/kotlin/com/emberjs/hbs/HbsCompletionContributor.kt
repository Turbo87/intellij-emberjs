package com.emberjs.hbs

import com.emberjs.hbs.HbsPatterns.BLOCK_MUSTACHE_NAME_ID
import com.emberjs.hbs.HbsPatterns.SIMPLE_MUSTACHE_NAME_ID
import com.emberjs.hbs.HbsPatterns.SUB_EXPR_NAME_ID
import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionType

/**
 * The `HbsCompletionContributor` class is responsible for registering all
 * available `CompletionProviders` for the Handlebars language to their
 * corresponding `PsiElementPatterns`.
 */
class HbsCompletionContributor : CompletionContributor() {
    init {
        extend(CompletionType.BASIC, SIMPLE_MUSTACHE_NAME_ID, HbsBuiltinHelperCompletionProvider(
                "action", "component", "debugger", "get", "if", "input", "link-to", "loc", "log",
                "outlet", "partial", "readonly", "render", "textarea", "unbound"))

        extend(CompletionType.BASIC, BLOCK_MUSTACHE_NAME_ID, HbsBuiltinHelperCompletionProvider(
                "component", "each", "each-in", "if", "link-to", "unless", "with"))

        extend(CompletionType.BASIC, SUB_EXPR_NAME_ID, HbsBuiltinHelperCompletionProvider(
                "action", "component", "concat", "get", "hash", "if", "loc", "mut", "query-params",
                "readonly", "unbound", "unless"))
    }
}
