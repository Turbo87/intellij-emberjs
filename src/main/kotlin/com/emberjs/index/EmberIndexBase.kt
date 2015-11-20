package com.emberjs.index

import com.emberjs.utils.VoidHelper
import com.emberjs.utils.append
import com.emberjs.utils.classify
import com.emberjs.utils.parents
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor

abstract class EmberIndexBase(private val name: ID<String, Void>) :
        ScalarIndexExtension<String>(),
        FileBasedIndex.InputFilter,
        DataIndexer<String, Void, FileContent> {

    val appFolder: String = name.toString().removePrefix("ember.").append("s")
    val suffix: String = name.toString().removePrefix("ember.").capitalize()

    override fun getName() = name
    override fun getVersion() = 1
    override fun getKeyDescriptor() = KEY_DESCRIPTIOR
    override fun dependsOnFileContent() = false

    override fun getInputFilter() = this
    override fun acceptInput(file: VirtualFile) =
            file.extension == "js" && file.inAppFolder(appFolder)

    override fun getIndexer() = this
    override fun map(inputData: FileContent): Map<String, Void> {

        // Convert path to class name
        val name = inputData.file.toClassName(appFolder).append(suffix)

        return mapOf(Pair(name, VoidHelper.get()))
    }

    companion object {
        val KEY_DESCRIPTIOR = EnumeratorStringDescriptor()

        /**
         * Converts path to class name
         */
        fun VirtualFile.toClassName(appFolder: String): String =
                sequence(this, { it.parent })
                        .map { it.nameWithoutExtension }
                        .takeWhile { it != appFolder }
                        .toList()
                        .asReversed()
                        .map { it.classify() }
                        .joinToString("")

        private fun VirtualFile.getParentDirectory(dirName: String) =
                parents.firstOrNull { it.name == dirName }

        private fun VirtualFile.inAppFolder(dirName: String) =
                getParentDirectory(dirName)?.parent?.name == "app"
    }
}
