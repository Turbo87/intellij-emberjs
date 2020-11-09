package com.emberjs

import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.ProjectScope
import com.intellij.psi.search.SearchScope
import javax.swing.Icon

class EmberAttrDec(private val name: String, private val description: String, ref: PsiReference?, private val references: Array<PsiReference>?) : PsiElement {
    private val userDataMap = HashMap<Any, Any>()
    private val reference: PsiReference
    override fun <T> getUserData(key: Key<T>): T? {
        return this.userDataMap[key] as T?
    }

    init {
        this.reference = ref ?: references!!.first()
    }

    override fun <T> putUserData(key: Key<T>, value: T?) {
        this.userDataMap[key] = value as Any
    }

    override fun getIcon(flags: Int): Icon {
        return this.reference.element.getIcon(flags)
    }

    override fun getProject(): Project {
        return this.reference.element.project
    }

    override fun getLanguage(): Language {
        return this.reference.element.language
    }

    override fun getManager(): PsiManager {
        return this.reference.element.manager
    }

    override fun getChildren(): Array<PsiElement> {
        return emptyArray<PsiElement>()
    }

    override fun getParent(): PsiElement? {
        return this.reference.element.parent
    }

    override fun getFirstChild(): PsiElement? {
        return null
    }

    override fun getLastChild(): PsiElement? {
        return null
    }

    override fun getNextSibling(): PsiElement? {
        return null
    }

    override fun getPrevSibling(): PsiElement? {
        return null
    }

    override fun getContainingFile(): PsiFile? {
        return this.reference.element.containingFile;
    }

    override fun getTextRange(): TextRange? {
        return null
    }

    override fun getStartOffsetInParent(): Int {
        return 0
    }

    override fun getTextLength(): Int {
        return name.length
    }

    override fun findElementAt(offset: Int): PsiElement? {
        return null
    }

    override fun findReferenceAt(offset: Int): PsiReference? {
        return null
    }

    override fun getTextOffset(): Int {
        return 0
    }

    override fun getText(): String {
        return this.description
    }

    override fun textToCharArray(): CharArray {
        return this.description.toCharArray()
    }

    override fun getNavigationElement(): PsiElement? {
        return this.reference.element
    }

    override fun getOriginalElement(): PsiElement? {
        return null
    }

    override fun textMatches(text: CharSequence): Boolean {
        return false
    }

    override fun textMatches(element: PsiElement): Boolean {
        return false
    }

    override fun textContains(c: Char): Boolean {
        return false
    }

    override fun accept(visitor: PsiElementVisitor) {
        return
    }

    override fun acceptChildren(visitor: PsiElementVisitor) {
        TODO("Not yet implemented")
    }

    override fun copy(): PsiElement {
        TODO("Not yet implemented")
    }

    override fun add(element: PsiElement): PsiElement {
        TODO("Not yet implemented")
    }

    override fun addBefore(element: PsiElement, anchor: PsiElement?): PsiElement {
        TODO("Not yet implemented")
    }

    override fun addAfter(element: PsiElement, anchor: PsiElement?): PsiElement {
        TODO("Not yet implemented")
    }

    override fun checkAdd(element: PsiElement) {
        TODO("Not yet implemented")
    }

    override fun addRange(first: PsiElement?, last: PsiElement?): PsiElement {
        TODO("Not yet implemented")
    }

    override fun addRangeBefore(first: PsiElement, last: PsiElement, anchor: PsiElement?): PsiElement {
        TODO("Not yet implemented")
    }

    override fun addRangeAfter(first: PsiElement?, last: PsiElement?, anchor: PsiElement?): PsiElement {
        TODO("Not yet implemented")
    }

    override fun delete() {
        TODO("Not yet implemented")
    }

    override fun checkDelete() {
        TODO("Not yet implemented")
    }

    override fun deleteChildRange(first: PsiElement?, last: PsiElement?) {
        TODO("Not yet implemented")
    }

    override fun replace(newElement: PsiElement): PsiElement {
        TODO("Not yet implemented")
    }

    override fun isValid(): Boolean {
        return true;
    }

    override fun isWritable(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getReference(): PsiReference {
        return this.reference
    }

    override fun getReferences(): Array<PsiReference> {
        return this.references ?: emptyArray()
    }

    override fun <T : Any?> getCopyableUserData(key: Key<T>?): T? {
        TODO("Not yet implemented")
    }

    override fun <T : Any?> putCopyableUserData(key: Key<T>?, value: T?) {
        TODO("Not yet implemented")
    }

    override fun processDeclarations(processor: PsiScopeProcessor, state: ResolveState, lastParent: PsiElement?, place: PsiElement): Boolean {
        TODO("Not yet implemented")
    }

    override fun getContext(): PsiElement? {
        return this.reference.element.context
    }

    override fun isPhysical(): Boolean {
        return true
    }

    override fun getResolveScope(): GlobalSearchScope {
        TODO("Not yet implemented")
    }

    override fun getUseScope(): SearchScope {
        return ProjectScope.getProjectScope(this.project)
    }

    override fun getNode(): ASTNode? {
        return null
    }

    override fun isEquivalentTo(another: PsiElement?): Boolean {
        return false
    }

}
