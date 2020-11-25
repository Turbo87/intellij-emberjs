package com.emberjs

import com.intellij.lang.javascript.psi.ecma6.TypeScriptUnionOrIntersectionType
import com.intellij.lang.javascript.psi.ecma6.impl.TypeScriptPropertySignatureImpl
import com.intellij.lang.javascript.psi.ecma6.impl.TypeScriptStringLiteralTypeImpl
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlElement
import com.intellij.xml.XmlAttributeDescriptor

class EmberAttributeDescriptor(private val data: HashMap<String, Any?>) : XmlAttributeDescriptor {
    private val attrName: String
    private val description: String
    private val declaration: PsiElement?
    private val isRequired: Boolean
    private val values: List<String>
    val isParam: Boolean
    init {
        this.isParam = this.data.getOrDefault("isParam", false) as Boolean
        if (!this.isParam) {
            this.attrName = "@" + (this.data["value"] as String)
        } else {
            this.attrName = this.data["value"] as String
        }
        this.description = this.data.getOrDefault("description", "") as String
        val reference = this.data.getOrDefault("reference", null) as PsiReference?
        val references: Array<PsiReference>? = (this.data.getOrDefault("references", null) as ArrayList<PsiReference>?)?.toTypedArray()
        if (reference != null || (references != null && references.isNotEmpty())) {
            this.declaration = EmberAttrDec(
                    this.attrName,
                    this.description,
                    reference,
                    references
            )
        } else {
            this.declaration = null
        }

        val ref = this.data.getOrDefault("reference", null) as PsiReference?
        if (ref != null) {
            val type = PsiTreeUtil.collectElementsOfType(ref.element, TypeScriptPropertySignatureImpl::class.java).firstOrNull()
            val types = type?.children?.find { it is TypeScriptUnionOrIntersectionType }?.children
            val typesStr = types?.map { it.text } ?: arrayListOf<String>()
            val isOptional = (type?.isOptional ?: true) || typesStr.isEmpty() || (typesStr.contains("undefined") || typesStr.contains("null") || typesStr.contains("*"))
            this.isRequired = !isOptional
            if (types != null && types.all { it is TypeScriptStringLiteralTypeImpl }) {
                this.values = typesStr
            } else {
                this.values = arrayListOf<String>()
            }
        } else {
            this.isRequired = false
            this.values = arrayListOf<String>()
        }
    }

    override fun getDeclaration(): PsiElement? {
        return declaration
    }

    override fun getName(context: PsiElement?): String {
        return this.attrName
    }

    override fun getName(): String {
        return this.attrName
    }

    override fun init(element: PsiElement?) {
        TODO("Not yet implemented")
    }

    override fun isRequired(): Boolean {
        return this.isRequired
    }

    override fun isFixed(): Boolean {
        return false;
    }

    override fun hasIdType(): Boolean {
        return false
    }

    override fun hasIdRefType(): Boolean {
        return false
    }

    override fun getDefaultValue(): String? {
        return null
    }

    override fun isEnumerated(): Boolean {
        return this.values.isNotEmpty()
    }

    override fun getEnumeratedValues(): Array<String> {
        return this.values.toTypedArray()
    }

    override fun validateValue(context: XmlElement?, value: String?): String? {
        if (this.values.isNotEmpty() && this.values.contains(value)) {
            return value
        }
        return null
    }

}
