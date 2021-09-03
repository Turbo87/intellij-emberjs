package com.emberjs.navigation

import com.emberjs.index.EmberNameIndex
import com.emberjs.resolver.EmberName
import com.emberjs.utils.originalVirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.ProjectScope
import com.intellij.testIntegration.TestFinder

class EmberTestFinder : TestFinder {
    override fun findSourceElement(from: PsiElement): PsiFile? {
        return from.containingFile
    }

    override fun findTestsForClass(element: PsiElement): Collection<PsiElement> {
        val project = element.project
        val file = element.originalVirtualFile ?: return emptyList()

        val name = EmberName.from(file) ?: return emptyList()

        val search = listOf("-test", "-integration-test")
                .map { EmberName("${name.type}$it", name.name) }

        val psiManager = PsiManager.getInstance(project)
        val scope = ProjectScope.getAllScope(project)

        return EmberNameIndex.getFilteredFiles(scope) { it in search }.mapNotNull { psiManager.findFile(it) }
    }

    override fun findClassesForTest(element: PsiElement): Collection<PsiElement> {
        val project = element.project
        val file = element.originalVirtualFile ?: return emptyList()

        val name = EmberName.from(file) ?: return emptyList()

        val search = EmberName(name.type.removeSuffix("-test").removeSuffix("-integration"), name.name)

        val psiManager = PsiManager.getInstance(project)
        val scope = ProjectScope.getAllScope(project)

        return EmberNameIndex.getFilteredFiles(scope) { it == search }.mapNotNull { psiManager.findFile(it) }
    }

    override fun isTest(element: PsiElement): Boolean {
        val file = element.originalVirtualFile ?: return false
        return EmberName.from(file)?.let { it.isTest } ?: false
    }
}
