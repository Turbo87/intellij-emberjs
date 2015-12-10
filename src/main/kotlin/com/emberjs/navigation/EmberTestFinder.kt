package com.emberjs.navigation

import com.emberjs.project.EmberProjectComponent
import com.emberjs.resolver.EmberName
import com.emberjs.resolver.EmberResolver
import com.emberjs.utils.guessProject
import com.emberjs.utils.originalVirtualFile
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.testIntegration.TestFinder

class EmberTestFinder : TestFinder {
    override fun findSourceElement(from: PsiElement): PsiFile? {
        return from.containingFile
    }

    override fun findTestsForClass(element: PsiElement): Collection<PsiElement> {
        val file = element.originalVirtualFile

        val project = file.guessProject() ?: return emptyList()
        val roots = EmberProjectComponent.getInstance(project)?.roots ?: return listOf()

        val psiManager = PsiManager.getInstance(project)

        return roots.filter { VfsUtil.isAncestor(it, file, true) }
                .flatMap { findTestsForClass(file, it) }
                .map { psiManager.findFile(it) }
                .filterNotNull()
    }

    fun findTestsForClass(file: VirtualFile, root: VirtualFile): Collection<VirtualFile> {
        val name = EmberName.from(root, file) ?: return emptyList()

        val resolver = EmberResolver(root)
        return listOf("-test", "-integration-test")
                .map { resolver.resolve("${name.type}$it", name.name) }
                .filterNotNull()
    }

    override fun findClassesForTest(element: PsiElement): Collection<PsiElement> {
        val file = element.originalVirtualFile

        val project = file.guessProject() ?: return emptyList()
        val roots = EmberProjectComponent.getInstance(project)?.roots ?: return listOf()

        val psiManager = PsiManager.getInstance(project)

        return roots.filter { VfsUtil.isAncestor(it, file, true) }
                .map { findClassesForTest(file, it) }
                .filterNotNull()
                .map { psiManager.findFile(it) }
                .filterNotNull()
    }

    fun findClassesForTest(file: VirtualFile, root: VirtualFile): VirtualFile? {
        val name = EmberName.from(root, file) ?: return null

        return EmberResolver(root)
                .resolve(name.type.removeSuffix("-test").removeSuffix("-integration"), name.name)
    }

    override fun isTest(element: PsiElement): Boolean {
        val file = element.originalVirtualFile

        val project = file.guessProject() ?: return false
        val roots = EmberProjectComponent.getInstance(project)?.roots ?: return false

        return roots.filter { VfsUtil.isAncestor(it, file, true) }
                .map { EmberName.from(it, file) }
                .filterNotNull()
                .any { it.isTest }
    }
}
