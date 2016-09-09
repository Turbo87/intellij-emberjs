package com.emberjs.hbs

import com.dmarcotte.handlebars.parsing.HbTokenTypes
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiElement

object HbsPatterns {
    val SIMPLE_MUSTACHE_NAME_PATTERN: PsiElementPattern.Capture<PsiElement> =
            PlatformPatterns.psiElement(HbTokenTypes.ID)
                    .withSuperParent(3, PlatformPatterns.psiElement(HbTokenTypes.MUSTACHE_NAME)
                            .withParent(PlatformPatterns.psiElement(HbTokenTypes.MUSTACHE))
                            .afterLeaf(PlatformPatterns.psiElement(HbTokenTypes.OPEN)))

    val BLOCK_MUSTACHE_NAME_PATTERN: PsiElementPattern.Capture<PsiElement> =
            PlatformPatterns.psiElement(HbTokenTypes.ID)
                    .withSuperParent(3, PlatformPatterns.psiElement(HbTokenTypes.MUSTACHE_NAME)
                            .withParent(PlatformPatterns.psiElement(HbTokenTypes.OPEN_BLOCK_STACHE))
                            .afterLeaf(PlatformPatterns.psiElement(HbTokenTypes.OPEN_BLOCK)))

    val SUB_EXPR_NAME_PATTERN: PsiElementPattern.Capture<PsiElement> =
            PlatformPatterns.psiElement(HbTokenTypes.ID)
                    .withSuperParent(3, PlatformPatterns.psiElement(HbTokenTypes.MUSTACHE_NAME)
                            .withParent(PlatformPatterns.psiElement(HbTokenTypes.PARAM)))
}
