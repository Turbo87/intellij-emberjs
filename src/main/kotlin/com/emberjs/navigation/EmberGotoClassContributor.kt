package com.emberjs.navigation

import com.emberjs.icons.EmberIconProvider
import com.emberjs.icons.EmberIcons
import com.emberjs.index.EmberNameIndex
import com.emberjs.resolver.EmberName
import com.emberjs.utils.isInRepoAddon
import com.emberjs.utils.parentEmberModule
import com.intellij.navigation.DelegatingItemPresentation
import com.intellij.navigation.GotoClassContributor
import com.intellij.navigation.NavigationItem
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.util.indexing.FindSymbolParameters.searchScopeFor

class EmberGotoClassContributor : GotoClassContributor {
    private val iconProvider by lazy { EmberIconProvider() }

    override fun getQualifiedName(item: NavigationItem?): String? {
        if (item is DelegatingNavigationItem) {
            return item.presentation?.presentableText?.replace("-", qualifiedNameSeparator)
        }
        return null
    }

    override fun getQualifiedNameSeparator() = " "

    /**
     * Get all entries from the module index and extract the `displayName` property.
     */
    override fun getNames(project: Project, includeNonProjectItems: Boolean): Array<String> {
        return EmberNameIndex.getAllKeys(project)
                .map { it.displayName }
                .toTypedArray()
    }

    override fun getItemsByName(name: String, pattern: String, project: Project, includeNonProjectItems: Boolean): Array<NavigationItem> {
        val scope = searchScopeFor(project, includeNonProjectItems)

        val psiManager = PsiManager.getInstance(project)

        // Collect all matching modules from the index
        return EmberNameIndex.getFilteredPairs(scope) { it.displayName == name }
                // Find the corresponding PsiFiles
                .mapNotNull { (module, file) -> psiManager.findFile(file)?.let { Pair(module, it) } }
                // Convert search results for LookupElements
                .map { convert(it.first, it.second) }
                .toTypedArray()
    }

    private fun  convert(module: EmberName, file: PsiFile): DelegatingNavigationItem {
        val presentation = DelegatingItemPresentation(file.presentation)
                .withPresentableText(module.displayName)
                .withLocationString(getLocation(file))
                .withIcon(iconProvider.getIcon(module) ?: DEFAULT_ICON)

        return DelegatingNavigationItem(file)
                .withPresentation(presentation)
    }

    private fun getLocation(file: PsiFile): String? {
        val root = file.virtualFile.parentEmberModule ?: return null
        val prefix = file.virtualFile.path.removePrefix(root.path)

        return when {
            root.isInRepoAddon -> "(${root.name} addon)"
            prefix.startsWith("/app/") -> null
            prefix.startsWith("/addon/") -> "(addon)"
            prefix.startsWith("/tests/dummy/app/") -> "(dummy app)"
            prefix.startsWith("/tests/") -> "(test)"
            else -> null
        }
    }

    companion object {
        val DEFAULT_ICON = EmberIcons.EMPTY_16
    }
}

