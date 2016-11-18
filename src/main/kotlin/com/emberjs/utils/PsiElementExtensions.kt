package com.emberjs.utils

import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtilCore.findModuleForFile
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiFilter
import java.util.*


val PsiElement.originalVirtualFile: VirtualFile
    get() = containingFile.originalFile.virtualFile

val PsiElement.module: Module?
    get() = originalVirtualFile.let { findModuleForFile(it, project) }

val PsiElement.emberRoot: VirtualFile?
    get() = module?.emberRoot

val PsiElement.parents: Iterable<PsiElement>
    get() = object : Iterable<PsiElement> {
        override fun iterator(): Iterator<PsiElement> {
            var file = this@parents

            return object : Iterator<PsiElement> {
                override fun hasNext() = file.parent != null
                override fun next(): PsiElement {
                    file = file.parent
                    return file
                }
            }
        }
    }

fun <T : PsiElement> PsiElement.collect(filter: PsiFilter<T>) =
        ArrayList<T>().apply { accept(filter.createVisitor(this)) }

inline fun <reified T : PsiElement> PsiElementPattern.Capture<T>.toFilter() = object : PsiFilter<T>(T::class.java) {
    override fun accept(element: T) = this@toFilter.accepts(element)
}
