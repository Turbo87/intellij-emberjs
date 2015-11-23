package com.emberjs.resolver

import com.intellij.openapi.vfs.VirtualFile

class EmberResolver(val root: VirtualFile) {

    fun resolve(fullName: String) = resolve(EmberName.from(fullName))

    fun resolve(name: EmberName?): VirtualFile? {
        name ?: return null

        val fileExtension = when (name.type) {
            "template" -> "hbs"
            else -> "js"
        }

        return findIf(name.type == "component", "app/components/${name.name}/${name.type}.$fileExtension") ?:
                findIf(name.type != "component", "app/${name.name}/${name.type}.$fileExtension") ?:
                find("app/${name.type}s/${name.name}.$fileExtension")
    }

    private fun find(path: String) = root.findFileByRelativePath(path)
    private fun findIf(condition: Boolean, path: String) =
            if (condition) find(path) else null
}

