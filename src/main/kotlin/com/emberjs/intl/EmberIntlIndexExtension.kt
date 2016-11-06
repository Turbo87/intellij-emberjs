package com.emberjs.intl

import com.emberjs.intl.EmberIntlIndex.NAME
import com.emberjs.utils.findMainPackageJson
import com.emberjs.utils.isEmberFolder
import com.emberjs.yaml.keyPath
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FileBasedIndexExtension
import com.intellij.util.indexing.FileContent
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import org.jetbrains.yaml.psi.YAMLFile

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

        return YAMLScalarKeyValueCollector().collectFrom(psiFile)
                .associate { it.keyPath to it.valueText }
    }

    companion object {
        fun findKeyInFile(key: String, file: YAMLFile) = YAMLKeyValueFinder(key).findIn(file)
    }
}
