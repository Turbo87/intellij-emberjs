package com.emberjs.index

import com.emberjs.project.EmberProjectComponent
import com.emberjs.resolver.EmberName
import com.emberjs.utils.guessProject
import com.intellij.openapi.vfs.VfsUtil.isAncestor
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor

class EmberClassIndex() : ScalarIndexExtension<String>() {

    override fun getName() = NAME
    override fun getVersion() = 3
    override fun getKeyDescriptor() = KEY_DESCRIPTIOR
    override fun dependsOnFileContent() = false

    override fun getInputFilter() = FileBasedIndex.InputFilter {
        it.extension == "js" || it.extension == "hbs" || it.extension == "handlebars"
    }

    override fun getIndexer() = DataIndexer<String, Void?, FileContent> { inputData ->
        val file = inputData.file
        val project = file.guessProject() ?: return@DataIndexer mapOf()
        val roots = EmberProjectComponent.getInstance(project)?.roots ?: return@DataIndexer mapOf()

        roots.filter { isAncestor(it, file, true) }
                .map { EmberName.from(it, file) }
                .filterNotNull()
                .associateBy({ it.displayName }, { null })
    }

    companion object {
        val NAME: ID<String, Void> = ID.create("ember.classes")
        val KEY_DESCRIPTIOR = EnumeratorStringDescriptor()
    }
}
