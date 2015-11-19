package com.emberjs.index

import com.emberjs.utils.VoidHelper
import com.emberjs.utils.append
import com.emberjs.utils.classify
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor

abstract class EmberIndexBase(val appFolder: String, val suffix: String) :
        ScalarIndexExtension<String>(),
        FileBasedIndex.InputFilter,
        DataIndexer<String, Void, FileContent> {

    override fun getVersion() = 1
    override fun getKeyDescriptor() = KEY_DESCRIPTIOR
    override fun dependsOnFileContent() = false

    override fun getInputFilter() = this
    override fun acceptInput(file: VirtualFile) =
            file.extension == "js" && file.inAppFolder(appFolder)

    override fun getIndexer() = this
    override fun map(inputData: FileContent): Map<String, Void> {

        // Convert path to class name
        val name = sequence(inputData.file, { it.parent })
                .map { it.nameWithoutExtension }
                .takeWhile { it != appFolder }
                .toList()
                .asReversed()
                .map { it.classify() }
                .joinToString("")
                .append(suffix)

        return mapOf(Pair(name, VoidHelper.get()))
    }

    companion object {
        val KEY_DESCRIPTIOR = EnumeratorStringDescriptor()

        private val VirtualFile.parents: Iterable<VirtualFile>
            get() = object : Iterable<VirtualFile> {
                override fun iterator(): Iterator<VirtualFile> {
                    var file = this@parents

                    return object : Iterator<VirtualFile> {
                        override fun hasNext() = file.parent != null
                        override fun next(): VirtualFile {
                            file = file.parent
                            return file
                        }
                    }
                }
            }

        private fun VirtualFile.getParentDirectory(dirName: String) =
                parents.firstOrNull { it.name == dirName }

        private fun VirtualFile.inAppFolder(dirName: String) =
                getParentDirectory(dirName)?.parent?.name == "app"
    }
}
