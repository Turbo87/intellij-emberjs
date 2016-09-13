package com.emberjs.hbs

import com.dmarcotte.handlebars.parsing.HbTokenTypes
import com.dmarcotte.handlebars.psi.HbMustacheName
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.patterns.PsiElementPattern.Capture
import com.intellij.psi.PsiElement

object HbsPatterns {
    val SIMPLE_MUSTACHE_NAME: Capture<HbMustacheName> = psiElement(HbMustacheName::class.java)
            .withParent(psiElement(HbTokenTypes.MUSTACHE))
            .afterLeaf(psiElement(HbTokenTypes.OPEN))

    val SIMPLE_MUSTACHE_NAME_ID: Capture<PsiElement> = psiElement(HbTokenTypes.ID)
            .withSuperParent(3, SIMPLE_MUSTACHE_NAME)

    val BLOCK_MUSTACHE_NAME: Capture<HbMustacheName> = psiElement(HbMustacheName::class.java)
            .withParent(psiElement(HbTokenTypes.OPEN_BLOCK_STACHE))
            .afterLeaf(psiElement(HbTokenTypes.OPEN_BLOCK))

    val BLOCK_MUSTACHE_NAME_ID: Capture<PsiElement> = psiElement(HbTokenTypes.ID)
            .withSuperParent(3, BLOCK_MUSTACHE_NAME)

    val SUB_EXPR_NAME: Capture<HbMustacheName> = psiElement(HbMustacheName::class.java)
            .withParent(psiElement(HbTokenTypes.PARAM))

    val SUB_EXPR_NAME_ID: Capture<PsiElement> = psiElement(HbTokenTypes.ID)
            .withSuperParent(3, SUB_EXPR_NAME)
}
