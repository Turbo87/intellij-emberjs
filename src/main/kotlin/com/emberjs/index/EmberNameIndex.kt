package com.emberjs.index

import com.emberjs.resolver.EmberName
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.util.CommonProcessors
import com.intellij.util.FilteringProcessor
import com.intellij.util.Processor
import com.intellij.util.SlowOperations
import com.intellij.util.containers.addIfNotNull
import com.intellij.util.indexing.*
import com.intellij.util.io.BooleanDataDescriptor

class EmberNameIndex : ScalarIndexExtension<Boolean>() {

    override fun getName() = NAME
    override fun getVersion() = 5
    override fun getKeyDescriptor() = BooleanDataDescriptor.INSTANCE
    override fun dependsOnFileContent() = false

    override fun getInputFilter() = FileBasedIndex.InputFilter { it.extension in FILE_EXTENSIONS }

    override fun getIndexer() = DataIndexer<Boolean, Void?, FileContent> { mapOf(true to null) }

    companion object {
        val NAME: ID<Boolean, Void> = ID.create("ember.names")
        private val FILE_EXTENSIONS = setOf("css", "scss", "js", "ts", "hbs", "handlebars")

        private val index: FileBasedIndex get() = FileBasedIndex.getInstance()

        fun getAllKeys(project: Project): Collection<EmberName> {
            return getAllPairs(project).map { it.first }
        }

        private fun getAllPairs(project: Project): Collection<Pair<EmberName, VirtualFile>> {
            return SlowOperations.allowSlowOperations<Collection<Pair<EmberName, VirtualFile>>, Throwable> {
                 CachedValuesManager.getManager(project).getCachedValue(project) {
                    val results = mutableListOf<Pair<EmberName, VirtualFile>>()
                    for (file in index.getContainingFiles(NAME, true, GlobalSearchScope.projectScope(project))) {
                        ProgressManager.checkCanceled()
                        results.addIfNotNull(EmberName.from(file)?.let { it to file })
                    }
                    CachedValueProvider.Result.create(results, VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS)
                }
            }
        }

        private fun getScopePairs(scope: GlobalSearchScope): Collection<Pair<EmberName, VirtualFile>> {
            val project = scope.project ?: return emptyList()
            return getAllPairs(project).filter { scope.accept(it.second) }
        }

        // getFilteredKeys
        fun getFilteredProjectKeys(scope: GlobalSearchScope, filterFn: (EmberName) -> Boolean): Collection<EmberName> {
            return getScopePairs(scope).filter { filterFn(it.first) }.map { it.first }
        }

        fun getFilteredFiles(scope: GlobalSearchScope, filterFn: (EmberName) -> Boolean): Collection<VirtualFile> {
            return getScopePairs(scope).filter { filterFn(it.first) }.map { it.second }
        }

        fun getFilteredPairs(scope: GlobalSearchScope, filterFn: (EmberName) -> Boolean): Collection<Pair<EmberName, VirtualFile>> {
            return getScopePairs(scope).filter { filterFn(it.first) }
        }
    }
}
