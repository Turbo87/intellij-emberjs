package com.emberjs

import com.intellij.mock.MockVirtualFile
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

object EmberTestFixtures {

    val RESOURCE_PATH = Paths.get("src/test/resources/com/emberjs").toAbsolutePath()

    val CRATES_IO = from(RESOURCE_PATH.resolve("fixtures/crates.io"))

    val CRATES_IO_POD = dir("crates.io").with(
            dir("app").with(
                    dir("application").with(
                            file("adapter.js"),
                            file("route.js"),
                            file("template.hbs")),
                    dir("dependency").with(
                            file("adapter.js"),
                            file("model.js")),
                    dir("crate").with(
                            dir("index").with(
                                    file("controller.js"),
                                    file("route.js"),
                                    file("template.hbs")),
                            dir("versions").with(
                                    file("controller.js"),
                                    file("route.js"),
                                    file("template.hbs")),
                            file("model.js"),
                            file("serializer.js")),
                    dir("crates").with(
                            file("controller.js"),
                            file("route.js"),
                            file("template.hbs")),
                    dir("github-login").with(
                            file("route.js"),
                            file("template.hbs")),
                    dir("index").with(
                            file("controller.js"),
                            file("route.js"),
                            file("template.hbs")),
                    dir("session").with(
                            file("service.js")),
                    dir("dependency").with(
                            file("adapter.js")),
                    dir("components").with(
                            dir("crate-row").with(
                                    file("component.js"),
                                    file("template.hbs"))),
                    file("app.js"),
                    file("router.js")))

    private fun file(name: String) = MockVirtualFile(name)
    private fun dir(name: String) = MockVirtualDir(name)

    private class MockVirtualDir(name: String) : MockVirtualFile(true, name) {
        fun with(vararg children: MockVirtualFile): MockVirtualFile {
            for (child in children) {
                addChild(child)
            }

            return this
        }
    }

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
