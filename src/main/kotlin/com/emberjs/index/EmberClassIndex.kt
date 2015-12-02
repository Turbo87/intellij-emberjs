package com.emberjs.index

import com.emberjs.resolver.EmberName
import com.emberjs.utils.getEmberModule
import com.emberjs.utils.guessProject
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor

class EmberClassIndex() :
        ScalarIndexExtension<String>(),
        FileBasedIndex.InputFilter,
        DataIndexer<String, Void?, FileContent> {

    override fun getName() = NAME
    override fun getVersion() = 1
    override fun getKeyDescriptor() = KEY_DESCRIPTIOR
    override fun dependsOnFileContent() = false

    override fun getInputFilter() = this
    override fun acceptInput(file: VirtualFile) =
            file.extension == "js" || file.extension == "hbs"

    override fun getIndexer() = this
    override fun map(inputData: FileContent): Map<String, Void?> {
        val file = inputData.file
        val project = file.guessProject() ?: return mapOf()
        val module = file.getEmberModule(project) ?: return mapOf()

        return ModuleRootManager.getInstance(module).contentRoots
                .map { EmberName.from(it, file) }
                .filterNotNull()
                .toMap({ it.displayName }, { null })
    }

    companion object {
        val NAME: ID<String, Void> = ID.create("ember.classes")
        val KEY_DESCRIPTIOR = EnumeratorStringDescriptor()
    }
}
