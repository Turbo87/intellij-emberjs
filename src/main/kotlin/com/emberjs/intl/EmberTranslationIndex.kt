package com.emberjs.intl

import com.intellij.ide.plugins.PluginManager
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.psi.search.ProjectScope
import com.intellij.util.CommonProcessors.CollectProcessor
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.ID
import java.util.*

object EmberTranslationIndex {
    val NAME: ID<String, String> = ID.create("ember.translations")

    val YAML_PLUGIN_ENABLED by lazy {
        PluginManager.getPlugin(PluginId.findId("org.jetbrains.plugins.yaml"))?.isEnabled ?: false
    }

    private val index by lazy { FileBasedIndex.getInstance() }

    fun getTranslationKeys(project: Project): Set<String> {
        if (!YAML_PLUGIN_ENABLED) return emptySet()

        val processor = CollectProcessor<String>()
        index.processAllKeys(NAME, processor, project)
        return processor.results.toSet()
    }

    fun getTranslations(key: String, project: Project): Map<String, String> {
        if (!YAML_PLUGIN_ENABLED) return emptyMap()

        val result = LinkedHashMap<String, String>()
        val processor = FileBasedIndex.ValueProcessor<String> { file, value ->
            result[file.nameWithoutExtension] = value
            true
        }

        index.processValues(NAME, key, null, processor, ProjectScope.getAllScope(project))
        return result
    }
}

