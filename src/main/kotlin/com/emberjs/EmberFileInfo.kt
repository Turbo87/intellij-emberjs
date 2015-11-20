package com.emberjs

import com.emberjs.EmberFileType.*
import com.emberjs.EmberFileType.Companion.FOLDER_NAMES
import com.emberjs.utils.parents
import com.intellij.openapi.vfs.VirtualFile

data class EmberFileInfo(val type: EmberFileType, val isPod: Boolean) {
    companion object {
        fun from(file: VirtualFile): EmberFileInfo? {
            // This is not an Ember.js file if it's not inside of an `app` folder
            file.parents.find { it.name == "app" && it.isDirectory } ?: return null

            val typeFromFileName = EmberFileType.values.find { it.fileName == file.name }
            if (typeFromFileName != null) {
                val type = when {
                    typeFromFileName != TEMPLATE -> typeFromFileName
                    file.parents.any { it.isComponentFolder } -> COMPONENT_TEMPLATE
                    else -> TEMPLATE
                }

                return EmberFileInfo(type!!, true)
            }

            val parent = file.parents.find { it.name in FOLDER_NAMES } ?: return null
            val typeFromFolderName = EmberFileType.values.find { it.folderName == parent.name } ?: return null

            val type = when {
                typeFromFolderName != COMPONENT -> typeFromFolderName
                file.parents.any { it.isTemplateFolder } -> COMPONENT_TEMPLATE
                else -> COMPONENT
            }

            return EmberFileInfo(type, false)
        }

        private val VirtualFile.isComponentFolder: Boolean
            get() = (isDirectory && name == EmberFileType.COMPONENT.folderName)

        private val VirtualFile.isTemplateFolder: Boolean
            get() = (isDirectory && name == EmberFileType.TEMPLATE.folderName)
    }
}
