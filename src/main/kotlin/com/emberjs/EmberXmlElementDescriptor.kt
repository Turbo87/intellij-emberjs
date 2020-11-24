package com.emberjs

import com.dmarcotte.handlebars.parsing.HbTokenTypes
import com.dmarcotte.handlebars.psi.impl.HbDataImpl
import com.dmarcotte.handlebars.psi.impl.HbPathImpl
import com.emberjs.utils.*
import com.intellij.codeInsight.documentation.DocumentationManager.ORIGINAL_ELEMENT_KEY
import com.intellij.lang.javascript.psi.ecma6.impl.TypeScriptPropertySignatureImpl
import com.intellij.lang.javascript.psi.jsdoc.JSDocComment
import com.intellij.psi.*
import com.intellij.psi.impl.file.PsiDirectoryImpl
import com.intellij.psi.impl.source.html.dtd.HtmlNSDescriptorImpl
import com.intellij.psi.impl.source.xml.XmlAttributeImpl
import com.intellij.psi.impl.source.xml.XmlDescriptorUtil
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
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

        fun forTag(tag: XmlTag): EmberXmlElementDescriptor? {
            val res = tag.references.last().resolve()
            if (res == null) {
                return null
            }
            return EmberXmlElementDescriptor(tag, res)
        }
    }

    override fun getDeclaration(): PsiElement? = declaration
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
            return PsiTreeUtil.collectElements(element.parent.parent) { it.elementType == HbTokenTypes.PARAM }.first()
        }
    }

    fun getFileByPath(directory: PsiDirectory, path: String): PsiFile? {
        var dir = directory
        val parts = path.split("/").toMutableList()
        while (parts.isNotEmpty()) {
            val p = parts.removeAt(0)
            val d: Any? = dir.findSubdirectory(p) ?: dir.findFile(p)
            if (d is PsiFile) {
                return d
            }
            if (d is PsiDirectory) {
                dir = d
                continue
            }
            return null
        }
        return null
    }

    /**
     * finds yields and data mustache `@xxx`
     * also check .ts/d.ts files for Component<Args>
     */
    fun getReferenceData(): HashMap<String, Any> {
        var f: PsiFile? = null
        // if it references a block param
        if (this.declaration.containingFile == this.tag.containingFile) {
            f = followReferences((this.declaration as XmlAttributeImpl).descriptor?.declaration?.reference?.resolve())?.containingFile
        }
        val file = f ?: this.declaration.containingFile
        var name = file.name.split(".").first()
        val dir = file.parent as PsiDirectoryImpl
        // co-located
        var template: PsiFile?
        if (name == "component") {
            name = "template"
        }
        template = dir.findFile("$name.hbs")
        val parentModule = file.parents.find { it is PsiDirectory && it.virtualFile == file.originalVirtualFile?.parentEmberModule} as PsiDirectory
        val path = file.parents
                .takeWhile { it != parentModule }
                .reversed()
                .map { (it as PsiFileSystemItem).name }
                .joinToString("/")

        val fullPathToHbs = path.replace("app/", "addon/") + "/$name.hbs"
        template = template ?: getFileByPath(parentModule, fullPathToHbs)

        val tplArgs = emptyArray<HashMap<String, Any>>().toMutableList()
        val tplYields = emptyArray<YieldReference>().toMutableList()
        if (template?.node?.psi != null) {
            val args = PsiTreeUtil.collectElementsOfType(template.node.psi, HbDataImpl::class.java)
            for (arg in args) {
                val argName = arg.text.split(".").first()
                if (tplArgs.find { it["value"] == argName } == null) {
                    tplArgs.add(hashMapOf("value" to argName as Any, "reference" to AttrPsiReference(arg)))
                }
            }

            val yields = PsiTreeUtil.collectElements(template.node.psi, { it is HbPathImpl && it.text == "yield" })
            for (y in yields) {
                tplYields.add(YieldReference(y))
            }
        }

        if (name == "template") {
            name = "component"
        }
        val hasSplattributes = template?.text?.contains("...attributes") ?: false
        val fullPathToTs = path.replace("app/", "addon/") + "/$name.ts"
        val fullPathToDts = path.replace("app/", "addon/") + "/$name.d.ts"
        val tsFile = getFileByPath(parentModule, fullPathToTs) ?: getFileByPath(parentModule, fullPathToDts)
        if (tsFile != null) {
            val argsElem = findComponentArgsType(tsFile)
            val signatures = PsiTreeUtil.collectElements(argsElem) { it is TypeScriptPropertySignatureImpl }
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
        return hashMapOf("hasSplattributes" to hasSplattributes, "tplArgs" to tplArgs, "tplYields" to tplYields)
    }

    override fun getAttributesDescriptors(context: XmlTag?): Array<out XmlAttributeDescriptor> {
        val result = mutableListOf<XmlAttributeDescriptor>()
        val commonHtmlAttributes = HtmlNSDescriptorImpl.getCommonAttributeDescriptors(context)
        val data = getReferenceData()
        val attributes = (data["tplArgs"] as MutableList<HashMap<String, Any?>>).map { EmberAttributeDescriptor(it)  }
        result.addAll(attributes)
        if (data["hasSplattributes"]!! as Boolean) {
            result.addAll(commonHtmlAttributes)
        }
        return result.toTypedArray()
    }

    override fun getAttributeDescriptor(attributeName: String?, context: XmlTag?): XmlAttributeDescriptor? {
        if (attributeName == null || context == null) {
            return null
        }
        if (attributeName == "as") {
            return AnyXmlAttributeDescriptor(attributeName)
        }
        val asIndex = context.attributes.indexOfFirst { it.text == "as" }
        if (asIndex >= 0) {
            // handle |param| or |param1 param2| or | param | or | param1 param2 | or | param1 param2|
            // for referencing && renaming the pattern | x y | would be the best
            // there is also a possiblity that this can be improved with value & valueTextRange
            // attributes are always separated by spaces
            val blockParams = context.attributes.toList().subList(asIndex + 1, context.attributes.size)
            val index = blockParams.indexOfFirst { it.text == attributeName }
            if (index == -1) {
                return this.getAttributesDescriptors(context).find { it.name == attributeName }
            }
            val data = getReferenceData()
            val hash = hashMapOf(
                    "value" to attributeName,
                    "isParam" to true,
                    "description" to (data["tplYields"] as MutableList<YieldReference>).map {
                        it.resolve()?.children?.filter { it.elementType == HbTokenTypes.PARAM }?.map { "yield" } ?: ""
                    }.joinToString(" or "),
                    "references" to data["tplYields"]
            )
            return EmberAttributeDescriptor(hash)
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
