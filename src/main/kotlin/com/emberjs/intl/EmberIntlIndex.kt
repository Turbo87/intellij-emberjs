package com.emberjs.intl

import com.emberjs.intellij.isEnabled
import com.emberjs.json.keyPath
import com.emberjs.utils.findMainPackageJson
import com.emberjs.utils.isEmberFolder
import com.emberjs.yaml.keyPath
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.search.ProjectScope
import com.intellij.util.CommonProcessors
import com.intellij.util.indexing.*
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import org.jetbrains.yaml.psi.YAMLFile
import java.util.*

class EmberIntlIndex() : FileBasedIndexExtension<String, String>() {

    override fun getName() = NAME
    override fun getVersion() = 1
    override fun dependsOnFileContent() = true

    override fun getKeyDescriptor(): KeyDescriptor<String> = EnumeratorStringDescriptor.INSTANCE
    override fun getValueExternalizer(): DataExternalizer<String> = EnumeratorStringDescriptor.INSTANCE

    override fun getInputFilter() = FileBasedIndex.InputFilter { acceptFile(it) }

    private fun acceptFile(file: VirtualFile): Boolean {
        return (file.extension == "json" || (YAML_PLUGIN_ENABLED && file.extension == "yaml")) &&
                file.parent.name == "translations" && file.parent.parent.isEmberFolder &&
                findMainPackageJson(file)?.isDependencyOfAnyType("ember-intl") == true
    }

    override fun getIndexer() = DataIndexer<String, String, FileContent> { index(it.psiFile) }

    fun index(file: PsiFile) = when (file) {
        is JsonFile -> JsonStringPropertyCollector().collectFrom(file)
                .associate { it.keyPath to (it.value as JsonStringLiteral).value }

        is YAMLFile -> YAMLScalarKeyValueCollector().collectFrom(file)
                .associate { it.keyPath to it.valueText }

        else -> emptyMap()
    }

    companion object {
        val NAME: ID<String, String> = ID.create("ember.translations")

        private val YAML_PLUGIN_ENABLED by lazy { PluginId.findId("org.jetbrains.plugins.yaml").isEnabled }

        private val index by lazy { FileBasedIndex.getInstance() }

        fun getTranslationKeys(project: Project): Set<String> {
            val processor = CommonProcessors.CollectProcessor<String>()
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
}
