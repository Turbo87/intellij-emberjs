package com.emberjs.index

import com.emberjs.utils.VoidHelper
import com.emberjs.utils.append
import com.emberjs.utils.classify
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor

class EmberRouteIndex :
        ScalarIndexExtension<String>(),
        FileBasedIndex.InputFilter,
        DataIndexer<String, Void, FileContent> {

    override fun getName() = NAME
    override fun getVersion() = 7
    override fun getKeyDescriptor() = KEY_DESCRIPTIOR
    override fun dependsOnFileContent() = true

    override fun getInputFilter() = this
    override fun acceptInput(file: VirtualFile) =
            file.extension == "js" && file.inAppFolder(APP_FOLDER)

    override fun getIndexer() = this
    override fun map(inputData: FileContent): Map<String, Void> {

        // Convert path to class name
        val name = sequence(inputData.file, { it.parent })
                .map { it.nameWithoutExtension }
                .takeWhile { it != APP_FOLDER }
                .toList()
                .asReversed()
                .map { it.classify() }
                .joinToString("")
                .append(SUFFIX)

        return mapOf(Pair(name, VoidHelper.get()))
    }

    companion object {
        val NAME: ID<String, Void> = ID.create("ember.route")
        val KEY_DESCRIPTIOR = EnumeratorStringDescriptor()
        val APP_FOLDER = "routes"
        val SUFFIX = "Route"

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
