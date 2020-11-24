package com.emberjs.hbs

import com.emberjs.hbs.HbsPatterns.BLOCK_MUSTACHE_NAME_ID
import com.emberjs.hbs.HbsPatterns.BLOCK_MUSTACHE_PARAM
import com.emberjs.hbs.HbsPatterns.IMPORT_NAMES
import com.emberjs.hbs.HbsPatterns.IMPORT_PATH_AUTOCOMPLETE
import com.emberjs.hbs.HbsPatterns.MUSTACHE_ID_MISSING
import com.emberjs.hbs.HbsPatterns.MUSTACHE_ID
import com.emberjs.hbs.HbsPatterns.SIMPLE_MUSTACHE_NAME_ID
import com.emberjs.hbs.HbsPatterns.SUB_EXPR_NAME_ID
import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionType


val InternalsWithBlock = arrayListOf(
        "component",
        "each",
        "each-in",
        "if",
        "input",
        "let",
        "link-to",
        "mount",
        "outlet",
        "query-params",
        "textarea",
        "unbound",
        "unless",
        "with")

val InternalsWithoutBlock = arrayListOf(
        "action",
        "array",
        "component",
        "concat",
        "debugger",
        "fn",
        "get",
        "hasBlock",
        "hasBlockParams",
        "hash",
        "if",
        "in-element",
        "input",
        "link-to",
        "loc",
        "log",
        "mount",
        "mut",
        "on",
        "outlet",
        "query-params",
        "textarea",
        "unbound",
        "unless",
        "yield")


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
        extend(CompletionType.BASIC, IMPORT_NAMES, HbsLocalCompletion())
        extend(CompletionType.BASIC, IMPORT_PATH_AUTOCOMPLETE, HbsLocalCompletion())
        extend(CompletionType.BASIC, BLOCK_MUSTACHE_PARAM, HbsLocalCompletion())
        extend(CompletionType.BASIC, MUSTACHE_ID_MISSING, HbsLocalCompletion())
    }
}
