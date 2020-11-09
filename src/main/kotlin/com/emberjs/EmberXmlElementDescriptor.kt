package com.emberjs

import com.dmarcotte.handlebars.parsing.HbTokenTypes
import com.dmarcotte.handlebars.psi.impl.HbDataImpl
import com.dmarcotte.handlebars.psi.impl.HbPathImpl
import com.emberjs.utils.*
import com.intellij.codeInsight.documentation.DocumentationManager.ORIGINAL_ELEMENT_KEY
import com.intellij.lang.javascript.psi.JSElement
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

class ArgData(
        var value: String = "",
        var description: String? = null,
        var reference: AttrPsiReference? = null) {}

class ComponentReferenceData(
        public val hasSplattributes: Boolean = false,
        public val yields: MutableList<EmberXmlElementDescriptor.YieldReference> = mutableListOf(),
        public val args: MutableList<ArgData> = mutableListOf()
) {

}

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

    class YieldReference(element: PsiElement): PsiReferenceBase<PsiElement>(element) {

        val yieldBlock by lazy {
            element.parent.parent
        }

        override fun resolve(): PsiElement? {
            return yieldBlock.children.find { it.elementType == HbTokenTypes.PARAM }
        }
    }

    fun getFileByPath(directory: PsiDirectory?, path: String): PsiFile? {
        if (directory == null) return null
        var dir: PsiDirectory = directory
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
    fun getReferenceData(): ComponentReferenceData {
        var f: PsiFile? = null
        // if it references a block param
        if (this.declaration.containingFile == this.tag.containingFile) {
            f = EmberUtils.followReferences((this.declaration as XmlAttributeImpl).descriptor?.declaration?.reference?.resolve())?.containingFile
        }
        val file = f ?: this.declaration.containingFile
        var name = file.name.split(".").first()
        val dir = file.parent as PsiDirectoryImpl?
        var template: PsiFile? = null
        var path = ""
        var parentModule: PsiDirectory? = null
        val tplArgs = emptyArray<ArgData>().toMutableList()
        var tplYields = mutableListOf<YieldReference>()

        if (dir != null) {
            // co-located
            if (name == "component") {
                name = "template"
            }
            template = dir.findFile("$name.hbs")
            parentModule = file.parents.find { it is PsiDirectory && it.virtualFile == file.originalVirtualFile?.parentEmberModule} as PsiDirectory?
            path = file.parents
                    .takeWhile { it != parentModule }
                    .reversed()
                    .map { (it as PsiFileSystemItem).name }
                    .joinToString("/")

            val fullPathToHbs = path.replace("app/", "addon/") + "/$name.hbs"
            template = template ?: getFileByPath(parentModule, fullPathToHbs)

            if (template?.node?.psi != null) {
                val args = PsiTreeUtil.collectElementsOfType(template.node.psi, HbDataImpl::class.java)
                for (arg in args) {
                    val argName = arg.text.split(".").first()
                    if (tplArgs.find { it.value == argName } == null) {
                        tplArgs.add(ArgData())
                    }
                }

                val yields = PsiTreeUtil.collectElements(template.node.psi, { it is HbPathImpl && it.text == "yield" })
                for (y in yields) {
                    tplYields.add(YieldReference(y))
                }
            }
        }


        if (name == "template") {
            name = "component"
        }
        val hasSplattributes = template?.text?.contains("...attributes") ?: false
        val fullPathToTs = path.replace("app/", "addon/") + "/$name.ts"
        val fullPathToDts = path.replace("app/", "addon/") + "/$name.d.ts"
        val tsFile = getFileByPath(parentModule, fullPathToTs) ?: getFileByPath(parentModule, fullPathToDts)
        val cls = tsFile?.let {EmberUtils.findDefaultExportClass(tsFile)} ?: this.declaration
        if (cls is JSElement) {
            val argsElem = EmberUtils.findComponentArgsType(cls)
            val signatures = argsElem?.properties ?: emptyList()
            for (sign in signatures) {
                val comment = sign.memberSource.singleElement?.children?.find { it is JSDocComment }
//                val s: TypeScriptSingleTypeImpl? = sign.children.find { it is TypeScriptSingleTypeImpl } as TypeScriptSingleTypeImpl?
                val attr = sign.toString().split(":").last()
                val data = tplArgs.find { it.value == attr } ?: ArgData()
                data.value = attr
                data.reference = AttrPsiReference(sign.memberSource.singleElement!!)
                data.description = comment?.text ?: ""
                if (tplArgs.find { it.value == attr } == null) {
                    tplArgs.add(data)
                }
            }
        }
        return ComponentReferenceData(hasSplattributes, tplYields, tplArgs)
    }

    override fun getAttributesDescriptors(context: XmlTag?): Array<out XmlAttributeDescriptor> {
        val result = mutableListOf<XmlAttributeDescriptor>()
        val commonHtmlAttributes = HtmlNSDescriptorImpl.getCommonAttributeDescriptors(context)
        val data = getReferenceData()
        val attributes = data.args.map { EmberAttributeDescriptor(it.value, false, it.description, it.reference, null)  }
        result.addAll(attributes)
        if (data.hasSplattributes) {
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
            return EmberAttributeDescriptor(attributeName, true, "yield", null, data.yields.toTypedArray())
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
