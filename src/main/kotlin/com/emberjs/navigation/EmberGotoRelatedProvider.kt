package com.emberjs.navigation

import com.emberjs.index.EmberNameIndex
import com.emberjs.resolver.EmberName
import com.intellij.navigation.GotoRelatedItem
import com.intellij.navigation.GotoRelatedProvider
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.ProjectScope

class EmberGotoRelatedProvider : GotoRelatedProvider() {

    override fun getItems(context: DataContext): List<GotoRelatedItem> {
        val project = PlatformDataKeys.PROJECT.getData(context) ?: return listOf()
        val file = PlatformDataKeys.VIRTUAL_FILE.getData(context) ?: return listOf()

        val psiManager = PsiManager.getInstance(project)

        return getItems(file, project)
                .map { EmberGotoRelatedItem.from(it.first, psiManager.findFile(it.second)) }
                .filterNotNull()
    }

    fun getItems(file: VirtualFile, project: Project): List<Pair<EmberName, VirtualFile>> {
        val name = EmberName.from(file) ?: return listOf()

        val modulesToSearch = when {
            name.type == "template" && name.name.startsWith("components/") ->
                listOf(EmberName("component", name.name.removePrefix("components/")))

            name.type == "component" ->
                listOf(EmberName("template", "components/${name.name}"))

            else -> RELATED_TYPES[name.type].orEmpty().map { EmberName(it, name.name) }
        }

        val scope = ProjectScope.getAllScope(project)

        return EmberNameIndex.getFilteredKeys(scope) { it in modulesToSearch }
                .flatMap { module -> EmberNameIndex.getContainingFiles(module, scope).map { module to it } }
    }

    companion object {
        val RELATED_TYPES = mapOf(
                "controller" to listOf("route", "template"),
                "route" to listOf("controller", "template"),
                "template" to listOf("controller", "route"),
                "model" to listOf("adapter", "serializer"),
                "adapter" to listOf("model", "serializer"),
                "serializer" to listOf("adapter", "model")
        )
    }
}
