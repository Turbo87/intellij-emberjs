package com.emberjs

import com.emberjs.EmberXmlElementDescriptor.Companion.forTag
import com.intellij.psi.impl.source.html.HtmlFileImpl
import com.intellij.psi.impl.source.xml.XmlElementDescriptorProvider
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.XmlElementDescriptor

class EmberXmlElementDescriptorProvider : XmlElementDescriptorProvider {
    override fun getDescriptor(tag: XmlTag?): XmlElementDescriptor? {
        if (tag == null) return null

        val containingFile = tag.containingFile as? HtmlFileImpl ?: return null
        val language = containingFile.contentElementType?.language ?: return null
        if (language.id !== "Handlebars") return null

        return forTag(tag)
    }
}
