package com.emberjs.index

import com.emberjs.project.EmberProjectComponent
import com.emberjs.resolver.EmberName
import com.emberjs.utils.guessProject
import com.intellij.openapi.vfs.VfsUtil.isAncestor
import com.intellij.util.indexing.*

class EmberNameIndex() : ScalarIndexExtension<EmberName>() {

    override fun getName() = NAME
    override fun getVersion() = 3
    override fun getKeyDescriptor() = KEY_DESCRIPTIOR
    override fun dependsOnFileContent() = false

    override fun getInputFilter() = FileBasedIndex.InputFilter {
        it.extension == "js" || it.extension == "hbs" || it.extension == "handlebars"
    }

    override fun getIndexer() = DataIndexer<EmberName, Void?, FileContent> { inputData ->
        val file = inputData.file
        val project = file.guessProject() ?: return@DataIndexer mapOf()
        val roots = EmberProjectComponent.getInstance(project)?.roots ?: return@DataIndexer mapOf()

        roots.filter { isAncestor(it, file, true) }
                .map { EmberName.from(it, file) }
                .filterNotNull()
                .associateBy({ it }, { null })
    }

    companion object {
        val NAME: ID<EmberName, Void> = ID.create("ember.names")
        val KEY_DESCRIPTIOR = EmberNameKeyDescriptor()
    }
}
