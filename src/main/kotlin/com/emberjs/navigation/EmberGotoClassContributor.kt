package com.emberjs.navigation

import com.emberjs.index.EmberRouteIndex
import com.intellij.icons.AllIcons
import com.intellij.navigation.ChooseByNameContributor
import com.intellij.navigation.ItemPresentation
import com.intellij.navigation.NavigationItem
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.ProjectScope
import com.intellij.util.indexing.FileBasedIndex

class EmberGotoClassContributor : ChooseByNameContributor {

    override fun getNames(project: Project, includeNonProjectItems: Boolean) =
            FileBasedIndex.getInstance().getAllKeys(EmberRouteIndex.NAME, project).toTypedArray()

    override fun getItemsByName(name: String, pattern: String, project: Project, includeNonProjectItems: Boolean) =
            getItemsByName(name, project, project.getScope(includeNonProjectItems))

    fun getItemsByName(name: String, project: Project, scope: GlobalSearchScope): Array<NavigationItem> {
        // Query file index for the VirtualFile containing the indexed item
        return FileBasedIndex.getInstance().getContainingFiles(EmberRouteIndex.NAME, name, scope)
                // Convert VirtualFile to PsiFile
                .map { PsiManager.getInstance(project).findFile(it) }
                .filterNotNull()
                // Create delegating NavigationItem with custom Presentation
                .map { it.cloneWithPresentation(EmberItemPresentation(name, it.presentation)) }
                .toTypedArray()
    }

    private class EmberItemPresentation(
            private val name: String, private val presentation: ItemPresentation?) : ItemPresentation {

        override fun getPresentableText() = name
        override fun getLocationString() = presentation?.locationString
        override fun getIcon(unused: Boolean) = AllIcons.Nodes.Class
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
}
