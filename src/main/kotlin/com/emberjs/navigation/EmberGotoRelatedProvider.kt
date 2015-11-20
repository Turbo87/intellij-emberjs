package com.emberjs.navigation

import com.emberjs.EmberFileInfo
import com.emberjs.EmberFileType
import com.emberjs.utils.findAppFolder
import com.emberjs.utils.parents
import com.intellij.navigation.GotoRelatedItem
import com.intellij.navigation.GotoRelatedProvider
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager

class EmberGotoRelatedProvider : GotoRelatedProvider() {

    override fun getItems(context: DataContext): List<GotoRelatedItem> {
        val project = PlatformDataKeys.PROJECT.getData(context) ?: return listOf()
        val psiManager = PsiManager.getInstance(project)

        val file = PlatformDataKeys.VIRTUAL_FILE.getData(context) ?: return listOf()
        val fileInfo = EmberFileInfo.from(file) ?: return listOf()
        if (fileInfo.isPod)
            return listOf()

        val relatedFiles = listOf(EMBER_MAIN_TYPES.findRelatedFiles(file, fileInfo),
                EMBER_DATA_TYPES.findRelatedFiles(file, fileInfo),
                findRelatedComponentFiles(file, fileInfo),
                findRelatedComponentTemplateFiles(file, fileInfo))

        return relatedFiles
                .flatten()
                .filterNotNull()
                .map { psiManager.findFile(it) }
                .filterNotNull()
                .map { GotoRelatedItem(it) }
    }

    /**
     * Find e.g. /app/routes/crate/index.js when called from /app/controllers/crate/index.js
     */
    private fun Iterable<EmberFileType>.findRelatedFiles(file: VirtualFile, fileInfo: EmberFileInfo):
            Iterable<VirtualFile?> {

        if (fileInfo.type !in this)
            return listOf()

        val appFolder = file.parents.findAppFolder() ?: return listOf()
        val pathParts = file.parents.map { it.name }.takeWhile { it != fileInfo.type.folderName }

        return this.filter { it != fileInfo.type }
                .map { type ->
                    // find e.g. "routes" child in "app" folder
                    var child = appFolder.findChild(type.folderName)
                    // find identical children folders
                    pathParts.forEach { child = child?.findChild(it) }
                    // find identical file with matching file extension
                    child?.findChild("${file.nameWithoutExtension}.${type.fileExtension}")
                }
    }

    /**
     * Find e.g. /app/components/x-select.js when called from /app/templates/components/x-select.hbs
     */
    private fun findRelatedComponentFiles(file: VirtualFile, fileInfo: EmberFileInfo): Iterable<VirtualFile?> {
        if (fileInfo.type != EmberFileType.COMPONENT_TEMPLATE)
            return listOf()

        val appFolder = file.parents.findAppFolder() ?: return listOf()
        val pathParts = file.parents.map { it.name }.takeWhile { it != EmberFileType.COMPONENT.folderName }

        // find e.g. "routes" child in "app" folder
        var child = appFolder.findChild(EmberFileType.COMPONENT.folderName)
        // find identical children folders
        pathParts.forEach { child = child?.findChild(it) }
        // find identical file with matching file extension
        child = child?.findChild("${file.nameWithoutExtension}.${EmberFileType.COMPONENT.fileExtension}")

        return listOf(child)
    }

    /**
     * Find e.g. /app/templates/components/x-select.hbs when called from /app/components/x-select.js
     */
    private fun findRelatedComponentTemplateFiles(file: VirtualFile, fileInfo: EmberFileInfo): Iterable<VirtualFile?> {
        if (fileInfo.type != EmberFileType.COMPONENT)
            return listOf()

        val appFolder = file.parents.findAppFolder() ?: return listOf()
        val pathParts = file.parents.map { it.name }.takeWhile { it != fileInfo.type.folderName }

        // find e.g. "routes" child in "app" folder
        var child = appFolder.findChild(EmberFileType.TEMPLATE.folderName)
        child = child?.findChild(EmberFileType.COMPONENT.folderName)
        // find identical children folders
        pathParts.forEach { child = child?.findChild(it) }
        // find identical file with matching file extension
        child = child?.findChild("${file.nameWithoutExtension}.${EmberFileType.TEMPLATE.fileExtension}")

        return listOf(child)
    }

    companion object {
        val EMBER_MAIN_TYPES = setOf(
                EmberFileType.CONTROLLER,
                EmberFileType.ROUTE,
                EmberFileType.TEMPLATE)

        val EMBER_DATA_TYPES = setOf(
                EmberFileType.ADAPTER,
                EmberFileType.MODEL,
                EmberFileType.SERIALIZER)
    }
}
