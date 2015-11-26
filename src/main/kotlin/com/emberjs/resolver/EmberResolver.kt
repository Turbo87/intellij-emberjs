package com.emberjs.resolver

import com.intellij.openapi.vfs.VirtualFile

class EmberResolver(val root: VirtualFile) {

    fun resolve(type: String, name: String) = resolve(EmberName(type, name))

    fun resolve(fullName: String) = resolve(EmberName.from(fullName))

    fun resolve(name: EmberName?): VirtualFile? {
        name ?: return null

        return when {
            name.type.endsWith("-integration-test") -> resolveTest("integration", name)
            name.type.endsWith("-test") -> resolveTest("unit", name)
            else -> resolveNonTest(name)
        }
    }

    private fun resolveNonTest(name: EmberName): VirtualFile? {
        val fileExtension = when (name.type) {
            "template" -> "hbs"
            else -> "js"
        }

        val isComponent = (name.type == "component")

        return findIf(isComponent, "app/components/${name.name}/${name.type}.$fileExtension") ?:
                findIf(!isComponent, "app/${name.name}/${name.type}.$fileExtension") ?:
                find("app/${name.type}s/${name.name}.$fileExtension")
    }

    private fun resolveTest(testType: String, name: EmberName): VirtualFile? {
        val type = name.type.removeSuffix("-test").removeSuffix("-$testType")

        return find("tests/$testType/${name.name}/${type}-test.js") ?:
                find("tests/$testType/${type}s/${name.name}-test.js")
    }

    private fun find(path: String) = root.findFileByRelativePath(path)
    private fun findIf(condition: Boolean, path: String) =
            if (condition) find(path) else null
}

