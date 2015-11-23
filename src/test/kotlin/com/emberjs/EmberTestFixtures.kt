package com.emberjs

import com.intellij.mock.MockVirtualFile
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

object EmberTestFixtures {

    val RESOURCE_PATH = Paths.get("src/test/resources/com/emberjs").toAbsolutePath()

    val CRATES_IO = from(RESOURCE_PATH.resolve("fixtures/crates.io"))
    val APTIBLE = from(RESOURCE_PATH.resolve("fixtures/dashboard.aptible.com"))

    private class MockVirtualDir(name: String) : MockVirtualFile(true, name)

    fun from(path: Path) = from(path.toFile())

    fun from(file: File): MockVirtualFile {
        return when {
            file.isDirectory -> MockVirtualDir(file.name).apply {
                file.listFiles().forEach { child ->
                    addChild(from(child))
                }
            }

            else -> MockVirtualFile(file.name)
        }
    }
}
