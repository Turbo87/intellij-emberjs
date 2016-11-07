package com.emberjs.translations

import com.emberjs.js.keyPath
import com.emberjs.utils.findMainPackageJson
import com.emberjs.utils.isEmberFolder
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.search.ProjectScope
import com.intellij.util.CommonProcessors
import com.intellij.util.indexing.*
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import filterNotNullValues
import java.util.*

class EmberI18nIndex() : FileBasedIndexExtension<String, String>() {

    override fun getName() = NAME
    override fun getVersion() = 1
    override fun dependsOnFileContent() = true

    override fun getKeyDescriptor(): KeyDescriptor<String> = EnumeratorStringDescriptor.INSTANCE
    override fun getValueExternalizer(): DataExternalizer<String> = EnumeratorStringDescriptor.INSTANCE

    override fun getInputFilter() = FileBasedIndex.InputFilter { acceptFile(it) }

    private fun acceptFile(file: VirtualFile): Boolean {
        return file.name == "translations.js" &&
                file.parent?.parent?.name == "locales" &&
                file.parent?.parent?.parent?.name == "app" &&
                file.parent?.parent?.parent?.parent?.isEmberFolder == true &&
                findMainPackageJson(file)?.isDependencyOfAnyType("ember-i18n") == true
    }

    override fun getIndexer() = DataIndexer<String, String, FileContent> { index(it.psiFile) }

    fun index(file: PsiFile): Map<String, String> {
        return JSStringPropertyCollector().collectFrom(file)
                .associate { it.keyPath to (it.value as? JSLiteralExpression)?.value as? String }
                .filterNotNullValues()
    }

    companion object {
        val NAME: ID<String, String> = ID.create("ember.i18n")

        private val index by lazy { FileBasedIndex.getInstance() }

        fun getTranslationKeys(project: Project): Set<String> {
            val processor = CommonProcessors.CollectProcessor<String>()
            index.processAllKeys(NAME, processor, project)
            return processor.results.toSet()
        }

        fun getTranslations(key: String, project: Project): Map<String, String> {
            val result = LinkedHashMap<String, String>()
            val processor = FileBasedIndex.ValueProcessor<String> { file, value ->
                result[file.parent.name] = value
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
