package com.emberjs.hbs

import com.emberjs.hbs.HbsPatterns.BLOCK_MUSTACHE_NAME_ID
import com.emberjs.hbs.HbsPatterns.BLOCK_MUSTACHE_PARAM
import com.emberjs.hbs.HbsPatterns.MUSTACHE_ID_MISSING
import com.emberjs.hbs.HbsPatterns.MUSTACHE_ID
import com.emberjs.hbs.HbsPatterns.SIMPLE_MUSTACHE_NAME_ID
import com.emberjs.hbs.HbsPatterns.SUB_EXPR_NAME_ID
import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionType


val InternalsWithBlock = (
        "component\n" +
        "each\n" +
        "each-in\n" +
        "if\n" +
        "input\n" +
        "let\n" +
        "link-to\n" +
        "mount\n" +
        "outlet\n" +
        "query-params\n" +
        "textarea\n" +
        "unbound\n" +
        "unless\n" +
        "with\n").split("\n")

val InternalsWithoutBlock = ("action\n" +
        "array\n" +
        "component\n" +
        "concat\n" +
        "debugger\n" +
        "fn\n" +
        "get\n" +
        "hasBlock\n" +
        "hasBlockParams\n" +
        "hash\n" +
        "if\n" +
        "in-element\n" +
        "input\n" +
        "link-to\n" +
        "loc\n" +
        "log\n" +
        "mount\n" +
        "mut\n" +
        "on\n" +
        "outlet\n" +
        "query-params\n" +
        "textarea\n" +
        "unbound\n" +
        "unless\n" +
        "yield").split("\n")


/**
 * The `HbsCompletionContributor` class is responsible for registering all
 * available `CompletionProviders` for the Handlebars language to their
 * corresponding `PsiElementPatterns`.
 */
class HbsCompletionContributor : CompletionContributor() {
    init {
        extend(CompletionType.BASIC, SIMPLE_MUSTACHE_NAME_ID, HbsBuiltinHelperCompletionProvider(InternalsWithoutBlock))
        extend(CompletionType.BASIC, BLOCK_MUSTACHE_NAME_ID, HbsBuiltinHelperCompletionProvider(InternalsWithBlock))
        extend(CompletionType.BASIC, SUB_EXPR_NAME_ID, HbsBuiltinHelperCompletionProvider(InternalsWithoutBlock))

        extend(CompletionType.BASIC, SIMPLE_MUSTACHE_NAME_ID, HbsLocalCompletion())
        extend(CompletionType.BASIC, BLOCK_MUSTACHE_NAME_ID, HbsLocalCompletion())
        extend(CompletionType.BASIC, SUB_EXPR_NAME_ID, HbsLocalCompletion())
        extend(CompletionType.BASIC, MUSTACHE_ID, HbsLocalCompletion())
        extend(CompletionType.BASIC, BLOCK_MUSTACHE_PARAM, HbsLocalCompletion())
        extend(CompletionType.BASIC, MUSTACHE_ID_MISSING, HbsLocalCompletion())
    }
}
