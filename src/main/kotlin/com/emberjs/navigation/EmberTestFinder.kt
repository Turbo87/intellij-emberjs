package com.emberjs.navigation

import com.emberjs.project.EmberModuleType
import com.emberjs.resolver.EmberName
import com.emberjs.resolver.EmberResolver
import com.intellij.openapi.project.ProjectLocator
import com.intellij.openapi.roots.ModuleRootManager
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
        val file = element.containingFile.virtualFile

        val project = ProjectLocator.getInstance().guessProjectForFile(file) ?: return emptyList()
        val module = EmberModuleType.findModuleForFile(file, project) ?: return emptyList()

        val psiManager = PsiManager.getInstance(project)

        return ModuleRootManager.getInstance(module).contentRoots
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
        val file = element.containingFile.virtualFile

        val project = ProjectLocator.getInstance().guessProjectForFile(file) ?: return emptyList()
        val module = EmberModuleType.findModuleForFile(file, project) ?: return emptyList()

        val psiManager = PsiManager.getInstance(project)

        return ModuleRootManager.getInstance(module).contentRoots
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
        val file = element.containingFile.virtualFile

        val project = ProjectLocator.getInstance().guessProjectForFile(file) ?: return false
        val module = EmberModuleType.findModuleForFile(file, project) ?: return false

        return ModuleRootManager.getInstance(module).contentRoots
                .map { EmberName.from(it, file) }
                .filterNotNull()
                .any { it.isTest }
    }
}
