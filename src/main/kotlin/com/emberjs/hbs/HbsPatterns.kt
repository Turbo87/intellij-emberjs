package com.emberjs.hbs

import com.dmarcotte.handlebars.parsing.HbTokenTypes
import com.dmarcotte.handlebars.psi.*
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.patterns.PsiElementPattern.Capture
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace

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

    val BLOCK_MUSTACHE_PARAM: Capture<PsiElement> = psiElement(HbTokenTypes.ID)
            .withParent(psiElement(HbTokenTypes.OPEN_BLOCK_STACHE))

    val MUSTACHE_ID_MISSING: Capture<PsiElement> = psiElement(HbTokenTypes.ID).withParent(psiElement(HbPsiElement::class.java)
            .afterSibling(psiElement(HbTokenTypes.SEP).afterSibling(psiElement(HbTokenTypes.ID))))

    val SUB_EXPR_NAME: Capture<HbMustacheName> = psiElement(HbMustacheName::class.java)
            .withParent(psiElement(HbTokenTypes.PARAM)
                    .afterLeaf(psiElement(HbTokenTypes.OPEN_SEXPR)))

    val SUB_EXPR_NAME_ID: Capture<PsiElement> = psiElement(HbTokenTypes.ID)
            .withSuperParent(3, SUB_EXPR_NAME)

    val MUSTACHE_ID: Capture<PsiElement> = psiElement(HbTokenTypes.ID)
    val IMPORT_PATH_AUTOCOMPLETE: Capture<PsiElement> = psiElement(HbTokenTypes.STRING).withSuperParent(3, (psiElement(HbTokenTypes.PARAM).afterSiblingSkipping(psiElement(HbTokenTypes.WHITE_SPACE), psiElement(HbTokenTypes.PARAM).withText("from"))))
    val IMPORT_PATH_REF: Capture<PsiElement> = psiElement(HbTokenTypes.STRING).withSuperParent(2, (psiElement(HbTokenTypes.PARAM).afterSiblingSkipping(psiElement(HbTokenTypes.WHITE_SPACE), psiElement(HbTokenTypes.PARAM).withText("from"))))

    val IMPORT_NAMES: Capture<PsiElement> = psiElement(HbTokenTypes.PARAM)
            .afterSiblingSkipping(psiElement(HbTokenTypes.WHITE_SPACE),
                    psiElement(HbTokenTypes.MUSTACHE_NAME).withText("import").afterSibling(psiElement(HbTokenTypes.OPEN))
            )


    val STRING_PARAM: Capture<HbParam> = psiElement(HbParam::class.java)
            .withChild(psiElement(HbMustacheName::class.java)
                    .withChild(psiElement(HbStringLiteral::class.java)))

    val LINK_TO_BLOCK_TARGET: Capture<HbParam> = STRING_PARAM
            .withParent(psiElement(HbOpenBlockMustache::class.java))
            .afterSiblingSkipping(psiElement(PsiWhiteSpace::class.java), psiElement(HbMustacheName::class.java)
                    .withText("link-to"))

    val LINK_TO_SIMPLE_TARGET: Capture<HbParam> = STRING_PARAM
            .withParent(psiElement(HbSimpleMustache::class.java))
            .afterSiblingSkipping(psiElement(PsiWhiteSpace::class.java), psiElement(HbParam::class.java)
                    .afterSiblingSkipping(psiElement(PsiWhiteSpace::class.java), psiElement(HbMustacheName::class.java)
                            .withText("link-to")))

    val TRANSLATION_KEY: Capture<HbParam> = STRING_PARAM
            .withParent(psiElement(HbSimpleMustache::class.java))
            .afterSiblingSkipping(psiElement(PsiWhiteSpace::class.java), psiElement(HbMustacheName::class.java).withText("t"))

    val TRANSLATION_KEY_IN_SEXPR: Capture<HbParam> = STRING_PARAM
            .withParent(psiElement(HbParam::class.java))
            .afterSiblingSkipping(psiElement(PsiWhiteSpace::class.java), psiElement(HbParam::class.java).withText("t"))
}
