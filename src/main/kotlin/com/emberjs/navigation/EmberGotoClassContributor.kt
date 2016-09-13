package com.emberjs.navigation

import com.emberjs.icons.EmberIconProvider
import com.emberjs.icons.EmberIcons
import com.emberjs.index.EmberNameIndex
import com.emberjs.resolver.EmberName
import com.intellij.navigation.ChooseByNameContributor
import com.intellij.navigation.DelegatingItemPresentation
import com.intellij.navigation.NavigationItem
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.util.indexing.FindSymbolParameters.searchScopeFor

class EmberGotoClassContributor() : ChooseByNameContributor {

    private val iconProvider by lazy { EmberIconProvider() }

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
        return EmberNameIndex.getFilteredKeys(scope) { it.displayName == name }

                // Find the corresponding PsiFiles
                .flatMap { module ->
                    EmberNameIndex.getContainingFiles(module, scope)
                            .map { psiManager.findFile(it)?.let { Pair(module, it) } }
                            .filterNotNull()
                }

                // Convert search results for LookupElements
                .map { convert(it.first, it.second) }
                .toTypedArray()
    }

    private fun convert(module: EmberName, file: PsiFile): DelegatingNavigationItem {
        val presentation = DelegatingItemPresentation(file.presentation)
                .withPresentableText(module.displayName)
                .withLocationString(null)
                .withIcon(iconProvider.getIcon(module) ?: DEFAULT_ICON)

        return DelegatingNavigationItem(file)
                .withPresentation(presentation)
    }

    companion object {
        val DEFAULT_ICON = EmberIcons.EMPTY_16
    }
}

