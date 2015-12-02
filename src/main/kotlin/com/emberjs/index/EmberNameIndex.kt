package com.emberjs.index

import com.emberjs.resolver.EmberName
import com.emberjs.utils.getEmberModule
import com.emberjs.utils.guessProject
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.indexing.*
import com.intellij.util.io.IOUtil
import com.intellij.util.io.KeyDescriptor
import java.io.DataInput
import java.io.DataOutput

class EmberNameIndex() :
        ScalarIndexExtension<EmberName>(),
        FileBasedIndex.InputFilter,
        DataIndexer<EmberName, Void?, FileContent> {

    override fun getName() = NAME
    override fun getVersion() = 1
    override fun getKeyDescriptor() = KEY_DESCRIPTIOR
    override fun dependsOnFileContent() = false

    override fun getInputFilter() = this
    override fun acceptInput(file: VirtualFile) =
            file.extension == "js" || file.extension == "hbs"

    override fun getIndexer() = this
    override fun map(inputData: FileContent): Map<EmberName, Void?> {
        val file = inputData.file
        val project = file.guessProject() ?: return mapOf()
        val module = file.getEmberModule(project) ?: return mapOf()

        return ModuleRootManager.getInstance(module).contentRoots
                .map { EmberName.from(it, file) }
                .filterNotNull()
                .toMap({ it }, { null })
    }

    companion object {
        val NAME: ID<EmberName, Void> = ID.create("ember.names")
        val KEY_DESCRIPTIOR = EmberNameKeyDescriptor()
    }
}
