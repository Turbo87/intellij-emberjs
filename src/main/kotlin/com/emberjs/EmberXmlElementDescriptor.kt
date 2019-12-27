package com.emberjs

import com.emberjs.index.EmberNameIndex
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.source.html.dtd.HtmlNSDescriptorImpl
import com.intellij.psi.impl.source.xml.XmlDescriptorUtil
import com.intellij.psi.search.ProjectScope
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.XmlAttributeDescriptor
import com.intellij.xml.XmlElementDescriptor
import com.intellij.xml.XmlElementsGroup
import com.intellij.xml.XmlNSDescriptor

class EmberXmlElementDescriptor(private val tag: XmlTag) : XmlElementDescriptor {
    val project = tag.project
    private val scope = ProjectScope.getAllScope(project)

    private val psiManager: PsiManager by lazy { PsiManager.getInstance(project) }

    override fun getDeclaration(): PsiElement {
        val componentTemplate = EmberNameIndex.getFilteredKeys(scope) { it.isComponentTemplate && it.angleBracketsName == tag.name }
                // Filter out components that are not related to this project
                .flatMap { EmberNameIndex.getContainingFiles(it, scope) }
                .map { psiManager.findFile(it) }
                .filterNotNull()
                .firstOrNull()

        if (componentTemplate != null) return componentTemplate

        val component = EmberNameIndex.getFilteredKeys(scope) { it.type == "component" && it.angleBracketsName == tag.name }
                .flatMap { EmberNameIndex.getContainingFiles(it, scope) }
                .map { psiManager.findFile(it) }
                .filterNotNull()
                .firstOrNull()

        if (component != null) return component

        return tag
    }

    override fun getName(context: PsiElement?): String = (context as? XmlTag)?.name ?: name
    override fun getName(): String = tag.localName
    override fun init(element: PsiElement?) {}
    override fun getQualifiedName(): String = name
    override fun getDefaultName(): String = name

    override fun getElementsDescriptors(context: XmlTag): Array<XmlElementDescriptor> {
        return XmlDescriptorUtil.getElementsDescriptors(context)
    }

    override fun getElementDescriptor(childTag: XmlTag, contextTag: XmlTag): XmlElementDescriptor? {
        return XmlDescriptorUtil.getElementDescriptor(childTag, contextTag)
    }

    override fun getAttributesDescriptors(context: XmlTag?): Array<out XmlAttributeDescriptor> {
        val result = mutableListOf<XmlAttributeDescriptor>()
        val commonHtmlAttributes = HtmlNSDescriptorImpl.getCommonAttributeDescriptors(context)
        result.addAll(commonHtmlAttributes)
        return result.toTypedArray()
    }

    override fun getAttributeDescriptor(attributeName: String?, context: XmlTag?): XmlAttributeDescriptor? = null
    override fun getAttributeDescriptor(attribute: XmlAttribute?): XmlAttributeDescriptor?
            = getAttributeDescriptor(attribute?.name, attribute?.parent)

    override fun getNSDescriptor(): XmlNSDescriptor? = null
    override fun getTopGroup(): XmlElementsGroup? = null
    override fun getContentType(): Int = XmlElementDescriptor.CONTENT_TYPE_ANY
    override fun getDefaultValue(): String? = null
}
