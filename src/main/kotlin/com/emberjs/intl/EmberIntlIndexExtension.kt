package com.emberjs.intl

import com.emberjs.intl.EmberIntlIndex.NAME
import com.emberjs.utils.findMainPackageJson
import com.emberjs.utils.isEmberFolder
import com.emberjs.yaml.keyPath
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.util.PsiFilter
import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FileBasedIndexExtension
import com.intellij.util.indexing.FileContent
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLScalar
import java.util.*

class EmberIntlIndexExtension() : FileBasedIndexExtension<String, String>() {

    override fun getName() = NAME
    override fun getVersion() = 1
    override fun dependsOnFileContent() = true

    override fun getKeyDescriptor(): KeyDescriptor<String> = EnumeratorStringDescriptor.INSTANCE
    override fun getValueExternalizer(): DataExternalizer<String> = EnumeratorStringDescriptor.INSTANCE

    override fun getInputFilter() = FileBasedIndex.InputFilter { acceptFile(it) }

    private fun acceptFile(file: VirtualFile): Boolean {
        return file.extension == "yaml" && file.parent.name == "translations" && file.parent.parent.isEmberFolder &&
                findMainPackageJson(file)?.isDependencyOfAnyType("ember-intl") == true
    }

    override fun getIndexer() = DataIndexer<String, String, FileContent> { index(it) }

    fun index(fileContent: FileContent): Map<String, String> {
        val psiFile = fileContent.psiFile as? YAMLFile ?: return emptyMap()

        return ArrayList<YAMLKeyValue>()
                .apply { psiFile.accept(YAML_ELEMENT_FILTER.createVisitor(this)) }
                .associate { it.keyPath to it.valueText }
    }

    companion object {
        val YAML_ELEMENT_FILTER = object : PsiFilter<YAMLKeyValue>(YAMLKeyValue::class.java) {
            override fun accept(element: YAMLKeyValue) = element.value is YAMLScalar
        }

        fun findKeyInFile(key: String, file: YAMLFile) = ArrayList<YAMLKeyValue>()
                .apply { file.accept(YAML_ELEMENT_FILTER.createVisitor(this)) }
                .find { it.keyPath == key }
    }
}

