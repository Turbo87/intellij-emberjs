package com.emberjs.resolver

import com.intellij.openapi.vfs.VirtualFile

class EmberResolver(val root: VirtualFile) {

    fun resolve(type: String, name: String) = resolve(EmberName(type, name))

    fun resolve(fullName: String) = resolve(EmberName.from(fullName))

    fun resolve(name: EmberName?): VirtualFile? {
        name ?: return null

        val fileExtension = when (name.type) {
            "template" -> "hbs"
            else -> "js"
        }

        val isComponent = (name.type == "component")

        return findIf(isComponent, "app/components/${name.name}/${name.type}.$fileExtension") ?:
                findIf(!isComponent, "app/${name.name}/${name.type}.$fileExtension") ?:
                find("app/${name.type}s/${name.name}.$fileExtension")
    }

    private fun find(path: String) = root.findFileByRelativePath(path)
    private fun findIf(condition: Boolean, path: String) =
            if (condition) find(path) else null
}

