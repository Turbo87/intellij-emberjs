package com.emberjs

import com.intellij.lang.javascript.psi.ecma6.TypeScriptUnionOrIntersectionType
import com.intellij.lang.javascript.psi.ecma6.impl.TypeScriptPropertySignatureImpl
import com.intellij.lang.javascript.psi.ecma6.impl.TypeScriptStringLiteralTypeImpl
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlElement
import com.intellij.xml.XmlAttributeDescriptor

class EmberAttributeDescriptor(value: String, isYield: Boolean = false, description: String?, reference: PsiReference?, references: Array<PsiReference>?) : XmlAttributeDescriptor {
    private val attrName: String
    private val description: String
    private val declaration: PsiElement?
    private val isRequired: Boolean
    private val values: List<String>
    val isYield: Boolean
    init {
        this.isYield = isYield
        if (!this.isYield) {
            this.attrName = "@" + value
        } else {
            this.attrName = value
        }
        this.description = description ?: ""
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

        val ref = reference
        if (ref != null) {
            val type = PsiTreeUtil.collectElementsOfType(ref.element, TypeScriptPropertySignatureImpl::class.java).firstOrNull()
            val types = type?.children?.find { it is TypeScriptUnionOrIntersectionType }?.children
            val typesStr = types?.map { it.text } ?: arrayListOf<String>()
            val isOptional = (type?.isOptional ?: true) || typesStr.isEmpty() || (typesStr.contains("undefined") || typesStr.contains("null") || typesStr.contains("*"))
            this.isRequired = !isOptional
            if (types != null && types.all { it is TypeScriptStringLiteralTypeImpl }) {
                this.values = typesStr
            } else {
                this.values = arrayListOf()
            }
        } else {
            this.isRequired = false
            this.values = arrayListOf()
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
