package com.emberjs.utils

import com.intellij.openapi.vfs.VirtualFile
import org.assertj.core.api.Assertions
import org.assertj.core.api.SoftAssertions

fun SoftAssertions.use(function: SoftAssertions.() -> Unit) {
    apply {
        function()
        assertAll()
    }
}

fun VirtualFile.find(path: String): VirtualFile {
    val file = findFileByRelativePath(path)
    Assertions.assertThat(file).describedAs(path).withFailMessage("File not found in '$name'").isNotNull()
    return file!!
}
