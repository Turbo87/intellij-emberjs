package com.emberjs.index

import com.emberjs.resolver.EmberName
import com.emberjs.utils.VoidHelper
import com.emberjs.utils.parents
import com.intellij.openapi.project.ProjectLocator
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor

abstract class EmberIndexBase(private val name: ID<String, Void>) :
        ScalarIndexExtension<String>(),
        FileBasedIndex.InputFilter,
        DataIndexer<String, Void, FileContent> {

    override fun getName() = name
    override fun getVersion() = 2
    override fun getKeyDescriptor() = KEY_DESCRIPTIOR
    override fun dependsOnFileContent() = false

    override fun getInputFilter() = this
    override fun acceptInput(file: VirtualFile): Boolean {
        val project = ProjectLocator.getInstance().guessProjectForFile(file) ?: return false
        val appFolder = project.baseDir.findChild("app") ?: return false

        return (file.extension == "js" || file.extension == "hbs") && file.parents.any { it == appFolder }
    }

    override fun getIndexer() = this
    override fun map(inputData: FileContent): Map<String, Void> {

        val file = inputData.file
        val project = ProjectLocator.getInstance().guessProjectForFile(file) ?: return mapOf()
        val name = EmberName.from(project.baseDir, file) ?: return mapOf()

        if (name.type != this.name.toString().removePrefix("ember."))
            return mapOf()

        return mapOf(Pair(name.displayName, VoidHelper.get()))
    }

    companion object {
        val KEY_DESCRIPTIOR = EnumeratorStringDescriptor()
    }
}
