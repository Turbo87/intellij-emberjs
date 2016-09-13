package com.emberjs.navigation

import com.emberjs.project.EmberProjectComponent
import com.emberjs.resolver.EmberName
import com.emberjs.resolver.EmberResolver
import com.intellij.navigation.GotoRelatedItem
import com.intellij.navigation.GotoRelatedProvider
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager

class EmberGotoRelatedProvider : GotoRelatedProvider() {

    override fun getItems(context: DataContext): List<GotoRelatedItem> {
        val project = PlatformDataKeys.PROJECT.getData(context) ?: return listOf()
        val file = PlatformDataKeys.VIRTUAL_FILE.getData(context) ?: return listOf()
        val roots = EmberProjectComponent.getInstance(project)?.roots ?: return listOf()

        val psiManager = PsiManager.getInstance(project)

        return roots.filter { VfsUtil.isAncestor(it, file, true) }
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
                "controller" to listOf("route", "template"),
                "route" to listOf("controller", "template"),
                "template" to listOf("controller", "route"),
                "model" to listOf("adapter", "serializer"),
                "adapter" to listOf("model", "serializer"),
                "serializer" to listOf("adapter", "model")
        )
    }
}
