package com.emberjs.refactoring

import com.dmarcotte.handlebars.parsing.HbLexer
import com.dmarcotte.handlebars.parsing.HbTokenTypes
import com.emberjs.EmberAttributeDescriptor
import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import com.intellij.lang.cacheBuilder.WordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.elementType
import com.intellij.psi.xml.XmlAttribute


class HbsWordScanner : DefaultWordsScanner(HbLexer(), TokenSet.create(HbTokenTypes.ID), HbTokenTypes.COMMENTS, TokenSet.create(HbTokenTypes.NUMBER, HbTokenTypes.BOOLEAN, HbTokenTypes.STRING)) {

}


class HbsUsageProvider : FindUsagesProvider {

    override fun getWordsScanner(): WordsScanner? {
        return null
    }

    override fun canFindUsagesFor(psiElement: PsiElement): Boolean {
        return psiElement.elementType == HbTokenTypes.ID ||
                (psiElement is XmlAttribute
                        && psiElement.descriptor is EmberAttributeDescriptor
                        && (psiElement.descriptor as EmberAttributeDescriptor).isYield)
    }

    override fun getHelpId(psiElement: PsiElement): String? {
        return "find usages"
    }

    override fun getType(element: PsiElement): String {
        return "variable"
    }

    override fun getDescriptiveName(element: PsiElement): String {
        return "variable"
    }

    override fun getNodeText(element: PsiElement, useFullName: Boolean): String {
        if (element is XmlAttribute) {
            val range = element.references.last().rangeInElement
            return element.text.substring(range.startOffset, range.endOffset)
        }
        return element.text
    }

}
