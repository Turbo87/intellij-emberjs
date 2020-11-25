package com.emberjs

import com.intellij.mock.MockVirtualFile
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

object EmberTestFixtures {

    val FIXTURES_RESOURCE = ClassLoader.getSystemResource("com/emberjs/fixtures")
    val FIXTURES_PATH = Paths.get(FIXTURES_RESOURCE.toURI()).toAbsolutePath()

    val EXAMPLE = fromFixture("example")
    val CRATES_IO = fromFixture("crates.io")
    val APTIBLE = fromFixture("dashboard.aptible.com")

    private class MockVirtualDir(name: String) : MockVirtualFile(true, name)

    fun fromFixture(name: String) = from(FIXTURES_PATH.resolve(name))

    fun from(path: Path) = from(path.toFile())

    fun from(file: File): MockVirtualFile = when {
        file.isDirectory -> MockVirtualDir(file.name).apply {
            file.listFiles().forEach { child ->
                addChild(from(child))
            }
        }

        else -> MockVirtualFile(file.name, file.readText())
    }
}
