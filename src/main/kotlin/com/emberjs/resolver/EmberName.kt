package com.emberjs.resolver

import com.emberjs.EmberFileType
import com.emberjs.utils.parents
import com.intellij.openapi.vfs.VirtualFile

data class EmberName(val type: String, val name: String) {

    val fullName: String
        get() = "$type:$name"

    val displayName: String
        get() = "${name.replace('/', '.')} $type"

    companion object {
        private val KNOWN_TYPES = EmberFileType.values.filter { it != EmberFileType.COMPONENT_TEMPLATE }

        fun from(fullName: String): EmberName? {
            val parts = fullName.split(":")
            return when {
                parts.count() == 2 -> EmberName(parts[0], parts[1])
                else -> null
            }
        }

        fun from(root: VirtualFile, file: VirtualFile): EmberName? {
            val appFolder = root.findChild("app") ?: return null

            return fromPod(root, appFolder, file) ?:
                    fromClassic(appFolder, file)
        }

        fun fromClassic(appFolder: VirtualFile, file: VirtualFile): EmberName? {
            val typeFolder = file.parents.find { it.parent == appFolder } ?: return null

            return KNOWN_TYPES.find { it.folderName == typeFolder.name }?.let { type ->

                var path = file.parents
                        .takeWhile { it != typeFolder }
                        .map { it.name }
                        .reversed()
                        .joinToString("/")

                val name = "$path/${file.nameWithoutExtension}".removePrefix("/")

                EmberName(type.name.toLowerCase(), name)
            }
        }

        fun fromPod(root: VirtualFile, appFolder: VirtualFile, file: VirtualFile): EmberName? {
            return KNOWN_TYPES.find { it.fileName == file.name }?.let { type ->

                var name = file.parents
                        .takeWhile { it != appFolder && it != root }
                        .map { it.name }
                        .reversed()
                        .joinToString("/")
                        .let {
                            when (type) {
                                EmberFileType.COMPONENT -> it.removePrefix("components/")
                                else -> it
                            }
                        }

                EmberName(type.name.toLowerCase(), name)
            }
        }
    }
}
