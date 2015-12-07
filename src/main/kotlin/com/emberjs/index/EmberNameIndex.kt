package com.emberjs.index

import com.emberjs.project.EmberProjectComponent
import com.emberjs.resolver.EmberName
import com.emberjs.utils.guessProject
import com.intellij.openapi.vfs.VfsUtil.isAncestor
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.indexing.*

class EmberNameIndex() :
        ScalarIndexExtension<EmberName>(),
        FileBasedIndex.InputFilter,
        DataIndexer<EmberName, Void?, FileContent> {

    override fun getName() = NAME
    override fun getVersion() = 2
    override fun getKeyDescriptor() = KEY_DESCRIPTIOR
    override fun dependsOnFileContent() = false

    override fun getInputFilter() = this
    override fun acceptInput(file: VirtualFile) =
            file.extension == "js" || file.extension == "hbs"

    override fun getIndexer() = this
    override fun map(inputData: FileContent): Map<EmberName, Void?> {
        val file = inputData.file
        val project = file.guessProject() ?: return mapOf()
        val roots = EmberProjectComponent.getInstance(project)?.roots ?: return mapOf()

        return roots.filter { isAncestor(it, file, true) }
                .map { EmberName.from(it, file) }
                .filterNotNull()
                .toMap({ it }, { null })
    }

    companion object {
        val NAME: ID<EmberName, Void> = ID.create("ember.names")
        val KEY_DESCRIPTIOR = EmberNameKeyDescriptor()
    }
}
