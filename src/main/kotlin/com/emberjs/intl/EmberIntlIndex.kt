package com.emberjs.intl

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.ProjectScope
import com.intellij.util.CommonProcessors.CollectProcessor
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.ID
import java.util.*

object EmberIntlIndex {
    val NAME: ID<String, String> = ID.create("ember.translations")

    private val index by lazy { FileBasedIndex.getInstance() }

    fun getTranslationKeys(project: Project): Set<String> {
        val processor = CollectProcessor<String>()
        index.processAllKeys(NAME, processor, project)
        return processor.results.toSet()
    }

    fun getTranslations(key: String, project: Project): Map<String, String> {
        val result = LinkedHashMap<String, String>()
        val processor = FileBasedIndex.ValueProcessor<String> { file, value ->
            result[file.nameWithoutExtension] = value
            true
        }

        index.processValues(NAME, key, null, processor, ProjectScope.getAllScope(project))
        return result
    }

    fun getFilesWithKey(key: String, project: Project): List<VirtualFile> {
        return ArrayList<VirtualFile>().apply {
            val processor = FileBasedIndex.ValueProcessor<String> { file, value -> add(file); true }
            index.processValues(NAME, key, null, processor, ProjectScope.getAllScope(project))
        }
    }
}

