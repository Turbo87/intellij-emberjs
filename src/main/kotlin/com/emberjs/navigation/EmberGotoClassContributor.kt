package com.emberjs.navigation

import com.emberjs.icons.EmberIcons
import com.emberjs.index.EmberFileIndex
import com.emberjs.resolver.EmberName
import com.intellij.navigation.ChooseByNameContributor
import com.intellij.navigation.ItemPresentation
import com.intellij.navigation.NavigationItem
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.ProjectScope
import com.intellij.util.indexing.FileBasedIndex
import javax.swing.Icon

class EmberGotoClassContributor() : ChooseByNameContributor {

    override fun getNames(project: Project, includeNonProjectItems: Boolean) =
            FileBasedIndex.getInstance().getAllKeys(EmberFileIndex.NAME, project).toTypedArray()

    override fun getItemsByName(name: String, pattern: String, project: Project, includeNonProjectItems: Boolean) =
            getItemsByName(name, project, project.getScope(includeNonProjectItems))

    fun getItemsByName(name: String, project: Project, scope: GlobalSearchScope): Array<NavigationItem> {
        // Query file index for the VirtualFile containing the indexed item
        return FileBasedIndex.getInstance().getContainingFiles(EmberFileIndex.NAME, name, scope)
                // Convert VirtualFile to PsiFile
                .map {
                    val psiFile = PsiManager.getInstance(project).findFile(it)
                    Pair(EmberName.from(project.baseDir, it), psiFile)
                }
                .filter { it.first != null && it.second != null }
                // Create delegating NavigationItem with custom Presentation
                .map {
                    val emberName = it.first!!
                    val psiFile = it.second!!
                    val icon = EmberIcons.FILE_TYPE_ICONS[emberName.type] ?: DEFAULT_ICON
                    psiFile.cloneWithPresentation(EmberItemPresentation(name, psiFile.presentation, icon)) }
                .toTypedArray()
    }

    private class EmberItemPresentation(
            private val name: String,
            private val presentation: ItemPresentation?,
            private val icon: Icon?
    ) : ItemPresentation {

        override fun getPresentableText() = name
        override fun getLocationString() = presentation?.locationString
        override fun getIcon(unused: Boolean) = icon
    }

    private fun NavigationItem.cloneWithPresentation(presentation: ItemPresentation): NavigationItem {
        return object : NavigationItem {
            override fun getPresentation() = presentation

            override fun getName() = this@cloneWithPresentation.name
            override fun canNavigate() = this@cloneWithPresentation.canNavigate()
            override fun canNavigateToSource() = this@cloneWithPresentation.canNavigateToSource()
            override fun navigate(requestFocus: Boolean) = this@cloneWithPresentation.navigate(requestFocus)
        }
    }

    private fun Project.getScope(includeNonProjectItems: Boolean): GlobalSearchScope {
        return when {
            includeNonProjectItems -> ProjectScope.getAllScope(this)
            else -> ProjectScope.getProjectScope(this)
        }
    }

    companion object {
        val DEFAULT_ICON = EmberIcons.EMPTY_16
    }
}
