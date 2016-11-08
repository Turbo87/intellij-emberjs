package com.emberjs.cli

import com.intellij.execution.filters.AbstractFileHyperlinkFilter
import com.intellij.execution.filters.FileHyperlinkRawData
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project

class EmberCliFilter(project: Project, baseDir: String?) : AbstractFileHyperlinkFilter(project, baseDir), DumbAware {

    override fun parse(line: String): MutableList<FileHyperlinkRawData> {
        val index = line.indexOf(CREATE)
        if (index < 0) return mutableListOf()

        val start = index + CREATE.length
        val fileName = line.substring(start).trim { it <= ' ' }
        return mutableListOf(FileHyperlinkRawData(fileName, -1, -1, start, start + fileName.length))
    }

    override fun supportVfsRefresh() = true

    companion object {
        private val CREATE = "create "
    }
}
