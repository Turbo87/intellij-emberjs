package com.emberjs

import com.dmarcotte.handlebars.psi.impl.HbDataImpl
import com.dmarcotte.handlebars.psi.impl.HbPathImpl
import com.emberjs.index.EmberNameIndex
import com.emberjs.resolver.ClassOrFileReference
import com.intellij.codeInsight.documentation.DocumentationManager.ORIGINAL_ELEMENT_KEY
import com.intellij.lang.javascript.psi.ecma6.impl.TypeScriptPropertySignatureImpl
import com.intellij.lang.javascript.psi.ecma6.impl.TypeScriptTypeAliasImpl
import com.intellij.lang.javascript.psi.jsdoc.JSDocComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.file.PsiDirectoryImpl
import com.intellij.psi.impl.source.html.dtd.HtmlNSDescriptorImpl
import com.intellij.psi.impl.source.xml.XmlDescriptorUtil
import com.intellij.psi.search.ProjectScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.XmlAttributeDescriptor
import com.intellij.xml.XmlElementDescriptor
import com.intellij.xml.XmlElementsGroup
import com.intellij.xml.XmlNSDescriptor
import com.intellij.xml.impl.schema.AnyXmlAttributeDescriptor
import com.intellij.psi.PsiReferenceBase as PsiReferenceBase1

class EmberXmlElementDescriptor(private val tag: XmlTag, private val declaration: PsiElement) : XmlElementDescriptor {
    val project = tag.project

    companion object {
        fun forTag(tag: XmlTag?): EmberXmlElementDescriptor? {
            if (tag == null) return null

            val project = tag.project
            val scope = ProjectScope.getAllScope(project)
            val psiManager: PsiManager by lazy { PsiManager.getInstance(project) }

            val componentTemplate = // Filter out components that are not related to this project
                    EmberNameIndex.getFilteredKeys(scope) { it.isComponentTemplate && it.tagName == tag.name }
                            // Filter out components that are not related to this project
                            .flatMap { EmberNameIndex.getContainingFiles(it, scope) }
                            .mapNotNull { psiManager.findFile(it) }
                            .firstOrNull()

            if (componentTemplate != null) return EmberXmlElementDescriptor(tag, componentTemplate)

            val component = EmberNameIndex.getFilteredKeys(scope) { it.type == "component" && it.tagName == tag.name }
                    .flatMap { EmberNameIndex.getContainingFiles(it, scope) }
                    .mapNotNull { psiManager.findFile(it) }
                    .map { ClassOrFileReference(it, null).resolve() }
                    .firstOrNull()

            if (component != null) return EmberXmlElementDescriptor(tag, component)

            return null
        }
    }

    override fun getDeclaration(): PsiElement = declaration
    override fun getName(context: PsiElement?): String = (context as? XmlTag)?.name ?: name
    override fun getName(): String = tag.localName
    override fun init(element: PsiElement?) {
        element?.putUserData(ORIGINAL_ELEMENT_KEY, null)
    }
    override fun getQualifiedName(): String = name
    override fun getDefaultName(): String = name

    override fun getElementsDescriptors(context: XmlTag): Array<XmlElementDescriptor> {
        return XmlDescriptorUtil.getElementsDescriptors(context)
    }

    override fun getElementDescriptor(childTag: XmlTag, contextTag: XmlTag): XmlElementDescriptor? {
        return XmlDescriptorUtil.getElementDescriptor(childTag, contextTag)
    }

    class YieldReference(element: PsiElement): PsiReferenceBase1<PsiElement>(element) {
        override fun resolve(): PsiElement? {
            return element
        }

    }

    override fun getAttributesDescriptors(context: XmlTag?): Array<out XmlAttributeDescriptor> {
        val result = mutableListOf<XmlAttributeDescriptor>()
        val commonHtmlAttributes = HtmlNSDescriptorImpl.getCommonAttributeDescriptors(context)
        val name = this.declaration.containingFile.name.split(".").first()
        val dir = this.declaration.containingFile.parent as PsiDirectoryImpl
        // co-located
        var template: PsiFile? = null
        if (name == "component") {
            template = dir.findFile("template.hbs")
        } else {
            template = dir.findFile("$name.hbs")
        }
        val tplArgs = emptyArray<HashMap<String, Any>>().toMutableList()
        val tplYields = emptyArray<HashMap<String, Any>>().toMutableList()
        if (template?.node?.psi != null) {
            val args = PsiTreeUtil.collectElementsOfType(template.node.psi, HbDataImpl::class.java)
            for (arg in args) {
                val argName = arg.text.split(".").first()
                if (tplArgs.find { it["value"] == argName } == null) {
                    tplArgs.add(hashMapOf( "value" to argName as Any, "reference" to AttrPsiReference(arg)))
                }
            }

            val yields = PsiTreeUtil.collectElements(template.node.psi, { it is HbPathImpl && it.text == "yield"})
            for (y in yields) {
                val argName = y.text
                tplYields.add(hashMapOf( "value" to argName as Any, "reference" to YieldReference(y)))
            }
        }

        val hasSplattributes = template?.text?.contains("...attributes") ?: false
        val tsFile =  dir.findFile("$name.ts") ?: dir.findFile("$name.d.ts")
        if (tsFile != null) {
            val argsElem = PsiTreeUtil.collectElements(tsFile.node.psi) { it is TypeScriptTypeAliasImpl && it.toString().endsWith(":Args") }
            val signatures = PsiTreeUtil.collectElements(argsElem[0]) { it is TypeScriptPropertySignatureImpl }
            for (sign in signatures) {
                val comment = sign.children.find { it is JSDocComment }
//                val s: TypeScriptSingleTypeImpl? = sign.children.find { it is TypeScriptSingleTypeImpl } as TypeScriptSingleTypeImpl?
                val attr = sign.toString().split(":").last()
                val data = tplArgs.find { it["value"] as String == attr } ?: HashMap()
                data["value"] = attr
                data["reference"] = AttrPsiReference(sign)
                data["description"] = comment?.text ?: ""
                if (tplArgs.find { it["value"] as String == attr } == null) {
                    tplArgs.add(data)
                }
            }
        }

        val attributes = tplArgs.map { EmberAttributeDescriptor(it)  }
        result.addAll(attributes)
        if (hasSplattributes) {
            result.addAll(commonHtmlAttributes)
        }
        return result.toTypedArray()
    }

    override fun getAttributeDescriptor(attributeName: String?, context: XmlTag?): XmlAttributeDescriptor? {
        if (attributeName == null) {
            return null
        }
        if (attributeName == "as" || attributeName.matches(Regex("^\\|.*\\|$"))) {
            return AnyXmlAttributeDescriptor(attributeName)
        }
        return this.getAttributesDescriptors(context).find { it.name == attributeName }
    }
    override fun getAttributeDescriptor(attribute: XmlAttribute?): XmlAttributeDescriptor?
            = getAttributeDescriptor(attribute?.name, attribute?.parent)

    override fun getNSDescriptor(): XmlNSDescriptor? = null
    override fun getTopGroup(): XmlElementsGroup? = null
    override fun getContentType(): Int = XmlElementDescriptor.CONTENT_TYPE_ANY
    override fun getDefaultValue(): String? = null
}
