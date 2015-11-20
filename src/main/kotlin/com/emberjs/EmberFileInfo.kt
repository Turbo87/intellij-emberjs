package com.emberjs

import com.emberjs.utils.parents
import com.intellij.openapi.vfs.VirtualFile

data class EmberFileInfo(val type: EmberFileType, val isPod: Boolean) {
    companion object {
        fun from(file: VirtualFile): EmberFileInfo? {
            // This is not an Ember.js file if it's not inside of an `app` folder
            file.parents.find { it.name == "app" && it.isDirectory } ?: return null

            val typeFromFileName = EmberFileType.values.find { it.fileName == file.name }
            if (typeFromFileName != null) {
                return EmberFileInfo(typeFromFileName, true)
            }

            val parent = file.parents.find { it.name in EmberFileType.FOLDER_NAMES } ?: return null
            val typeFromFolderName = EmberFileType.values.find { it.folderName == parent.name } ?: return null

            return EmberFileInfo(typeFromFolderName, false)
        }
    }
}
