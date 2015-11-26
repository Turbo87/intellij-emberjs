package com.emberjs.index

import com.emberjs.project.EmberModuleType
import com.emberjs.resolver.EmberName
import com.emberjs.utils.VoidHelper
import com.intellij.openapi.project.ProjectLocator
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor

class EmberFileIndex() :
        ScalarIndexExtension<String>(),
        FileBasedIndex.InputFilter,
        DataIndexer<String, Void, FileContent> {

    override fun getName() = NAME
    override fun getVersion() = 2
    override fun getKeyDescriptor() = KEY_DESCRIPTIOR
    override fun dependsOnFileContent() = false

    override fun getInputFilter() = this
    override fun acceptInput(file: VirtualFile) =
            file.extension == "js" || file.extension == "hbs"

    override fun getIndexer() = this
    override fun map(inputData: FileContent): Map<String, Void> {
        val file = inputData.file
        val project = ProjectLocator.getInstance().guessProjectForFile(file) ?: return mapOf()
        val module = EmberModuleType.findModuleForFile(file, project) ?: return mapOf()

        return ModuleRootManager.getInstance(module).contentRoots
                .map { EmberName.from(it, file) }
                .filterNotNull()
                .toMap({ it.displayName }, { VoidHelper.get() })
    }

    companion object {
        val NAME: ID<String, Void> = ID.create("ember.files")
        val KEY_DESCRIPTIOR = EnumeratorStringDescriptor()
    }
}
