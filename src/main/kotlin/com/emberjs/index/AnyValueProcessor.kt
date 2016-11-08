package com.emberjs.index

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.indexing.FileBasedIndex

class AnyValueProcessor<T> : FileBasedIndex.ValueProcessor<T> {
    var called = false
        private set

    override fun process(file: VirtualFile, value: T): Boolean {
        called = true
        return false
    }
}
