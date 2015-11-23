package com.emberjs

import com.intellij.mock.MockVirtualFile
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

object EmberTestFixtures {

    val FIXTURES_RESOURCE = ClassLoader.getSystemResource("com/emberjs/fixtures")
    val FIXTURES_PATH = Paths.get(FIXTURES_RESOURCE.toURI()).toAbsolutePath()

    val EXAMPLE = from(FIXTURES_PATH.resolve("example"))
    val CRATES_IO = from(FIXTURES_PATH.resolve("crates.io"))
    val APTIBLE = from(FIXTURES_PATH.resolve("dashboard.aptible.com"))

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
