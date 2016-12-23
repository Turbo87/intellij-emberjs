package com.emberjs.utils

import com.intellij.lang.javascript.library.JSLibraryUtil
import com.intellij.patterns.PatternCondition
import com.intellij.util.ProcessingContext
import com.intellij.util.indexing.FileContent

/** Exclude files in `node_modules`, `bower_components`, etc. from framework detection */
object NotLibrary : PatternCondition<FileContent>("notExcluded") {
    override fun accepts(content: FileContent, context: ProcessingContext?): Boolean {
        return !JSLibraryUtil.isProbableLibraryFile(content.file)
    }
}