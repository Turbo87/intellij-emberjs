package com.emberjs.intl

import com.emberjs.utils.isEmberFolder
import com.emberjs.utils.parents
import com.intellij.openapi.project.Project
import com.intellij.psi.search.ProjectScope
import com.intellij.psi.util.PsiFilter
import com.intellij.util.CommonProcessors.CollectProcessor
import com.intellij.util.indexing.*
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import org.jetbrains.yaml.psi.YAMLDocument
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLScalar
import java.util.*

class EmberTranslationIndex() : FileBasedIndexExtension<String, String>() {

    override fun getName() = NAME
    override fun getVersion() = 1
    override fun dependsOnFileContent() = true

    override fun getKeyDescriptor(): KeyDescriptor<String> = EnumeratorStringDescriptor.INSTANCE
    override fun getValueExternalizer(): DataExternalizer<String> = EnumeratorStringDescriptor.INSTANCE

    override fun getInputFilter() = FileBasedIndex.InputFilter {
        it.extension == "yaml" && it.parent.name == "translations" && it.parent.parent.isEmberFolder
    }

    override fun getIndexer() = DataIndexer<String, String, FileContent> { index(it) }

    fun index(fileContent: FileContent): Map<String, String> {
        val psiFile = fileContent.psiFile as? YAMLFile ?: return emptyMap()

        return ArrayList<YAMLKeyValue>()
                .apply { psiFile.accept(YAML_ELEMENT_FILTER.createVisitor(this)) }
                .associate { it.keyPath to it.valueText }
    }

    companion object {
        val NAME: ID<String, String> = ID.create("ember.translations")

        val YAML_ELEMENT_FILTER = object : PsiFilter<YAMLKeyValue>(YAMLKeyValue::class.java) {
            override fun accept(element: YAMLKeyValue) = element.value is YAMLScalar
        }

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

        private val YAMLKeyValue.keyPath: String
            get() = this.parents.takeWhile { it !is YAMLDocument }
                    .filterIsInstance(YAMLKeyValue::class.java)
                    .let { listOf(this, *it.toTypedArray()) }
                    .map { it.keyText }
                    .reversed()
                    .joinToString(".")
    }
}

