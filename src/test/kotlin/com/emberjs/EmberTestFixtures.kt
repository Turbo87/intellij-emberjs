package com.emberjs

import com.intellij.mock.MockVirtualFile

object EmberTestFixtures {

    val CRATES_IO = dir("crates.io").with(
            dir("app").with(
                    dir("adapters").with(
                            file("application.js"),
                            file("dependency.js")),
                    dir("components").with(
                            file("crate-row.js")),
                    dir("controllers").with(
                            dir("crate").with(
                                    file("index.js"),
                                    file("versions.js")),
                            file("crates.js"),
                            file("index.js")),
                    dir("models").with(
                            file("crate.js"),
                            file("dependency.js")),
                    dir("routes").with(
                            dir("crate").with(
                                    file("index.js"),
                                    file("versions.js")),
                            file("application.js"),
                            file("crates.js"),
                            file("github-login.js"),
                            file("index.js")),
                    dir("serializers").with(
                            file("crate.js")),
                    dir("services").with(
                            file("session.js")),
                    dir("templates").with(
                            dir("components").with(
                                    file("crate-row.hbs")),
                            dir("crate").with(
                                    file("index.hbs"),
                                    file("versions.hbs")),
                            file("application.hbs"),
                            file("crates.hbs"),
                            file("github-login.hbs"),
                            file("index.hbs")),
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
}
