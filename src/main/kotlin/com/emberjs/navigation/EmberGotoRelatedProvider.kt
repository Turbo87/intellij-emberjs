package com.emberjs.navigation

import com.emberjs.project.EmberModuleType
import com.emberjs.resolver.EmberName
import com.emberjs.resolver.EmberResolver
import com.intellij.navigation.GotoRelatedItem
import com.intellij.navigation.GotoRelatedProvider
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager

class EmberGotoRelatedProvider : GotoRelatedProvider() {

    override fun getItems(context: DataContext): List<GotoRelatedItem> {
        val project = PlatformDataKeys.PROJECT.getData(context) ?: return listOf()
        val file = PlatformDataKeys.VIRTUAL_FILE.getData(context) ?: return listOf()

        val module = EmberModuleType.findModuleForFile(file, project) ?: return listOf()

        val psiManager = PsiManager.getInstance(project)

        return ModuleRootManager.getInstance(module).contentRoots
                .flatMap { root ->
                    getFiles(root, file).map {
                        EmberGotoRelatedItem.from(EmberName.from(root, it), psiManager.findFile(it))
                    }
                }
                .filterNotNull()
    }

    fun getFiles(root: VirtualFile, file: VirtualFile): List<VirtualFile> {
        val name = EmberName.from(root, file) ?: return listOf()

        val resolver = EmberResolver(root)

        when (name.type) {
            "template" -> if (name.name.startsWith("components/")) {
                return listOf(resolver.resolve("component", name.name.removePrefix("components/"))).filterNotNull()
            }
            "component" -> {
                return listOf(resolver.resolve("template", "components/${name.name}")).filterNotNull()
            }
        }

        return RELATED_TYPES[name.type].orEmpty().map { resolver.resolve(it, name.name) }.filterNotNull()
    }

    companion object {
        val RELATED_TYPES = mapOf(
                Pair("controller", listOf("route", "template")),
                Pair("route", listOf("controller", "template")),
                Pair("template", listOf("controller", "route")),
                Pair("model", listOf("adapter", "serializer")),
                Pair("adapter", listOf("model", "serializer")),
                Pair("serializer", listOf("adapter", "model"))
        )
    }
}
