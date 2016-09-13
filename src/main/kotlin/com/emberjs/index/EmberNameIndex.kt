package com.emberjs.index

import com.emberjs.project.EmberProjectComponent
import com.emberjs.resolver.EmberName
import com.emberjs.utils.guessProject
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil.isAncestor
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import com.intellij.util.indexing.*

class EmberNameIndex() : ScalarIndexExtension<EmberName>() {

    override fun getName() = NAME
    override fun getVersion() = 3
    override fun getKeyDescriptor() = EmberNameKeyDescriptor()
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

        private val index by lazy { FileBasedIndex.getInstance() }

        fun getAllKeys(project: Project): Collection<EmberName>
                = index.getAllKeys(NAME, project)

        fun processAllKeys(processor: Processor<EmberName>, scope: GlobalSearchScope, idFilter: IdFilter? = null)
                = index.processAllKeys(NAME, processor, scope, idFilter)

        fun getContainingFiles(module: EmberName, scope: GlobalSearchScope): Collection<VirtualFile>
                = index.getContainingFiles(NAME, module, scope)
    }
}
