package com.emberjs.navigation

import com.emberjs.EmberTestFixtures.CRATES_IO
import com.intellij.openapi.vfs.VirtualFile
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EmberGotoRelatedProviderTest {

    val provider = EmberGotoRelatedProvider()

    @Test public fun testAdapter() =
            doTest("app/adapters/dependency.js",
                    "app/models/dependency.js")

    @Test public fun testApplicationAdapter() =
            doTest("app/adapters/application.js")

    @Test public fun testComponent() =
            doTest("app/components/crate-row.js",
                    "app/templates/components/crate-row.hbs")

    @Test public fun testController() =
            doTest("app/controllers/crates.js",
                    "app/routes/crates.js",
                    "app/templates/crates.hbs")

    @Test public fun testNestedController() =
            doTest("app/controllers/crate/versions.js",
                    "app/routes/crate/versions.js",
                    "app/templates/crate/versions.hbs")

    @Test public fun testModel() =
            doTest("app/models/crate.js",
                    "app/serializers/crate.js")

    @Test public fun testModel2() =
            doTest("app/models/dependency.js",
                    "app/adapters/dependency.js")

    @Test public fun testRoute() =
            doTest("app/routes/github-login.js",
                    "app/templates/github-login.hbs")

    @Test public fun testNestedRoute() =
            doTest("app/routes/crate/index.js",
                    "app/controllers/crate/index.js",
                    "app/templates/crate/index.hbs")

    @Test public fun testApplicationRoute() =
            doTest("app/routes/application.js",
                    "app/templates/application.hbs")

    @Test public fun testSerializer() =
            doTest("app/serializers/crate.js",
                    "app/models/crate.js")

    @Test public fun testService() =
            doTest("app/services/session.js")

    @Test public fun testTemplate() =
            doTest("app/templates/crates.hbs",
                    "app/controllers/crates.js",
                    "app/routes/crates.js")

    @Test public fun testApplicationTemplate() =
            doTest("app/templates/application.hbs",
                    "app/routes/application.js")

    @Test public fun testNestedTemplate() =
            doTest("app/templates/crate/versions.hbs",
                    "app/controllers/crate/versions.js",
                    "app/routes/crate/versions.js")

    @Test public fun testComponentTemplate() =
            doTest("app/templates/components/crate-row.hbs",
                    "app/components/crate-row.js")


    private fun doTest(path: String, vararg related: String) {
        assertThat(relatedTo(path)).containsExactlyElementsOf(listOf(*related).map { file(it) })
    }

    private fun relatedTo(path: String) = provider.getFiles(file(path))
    private fun file(path: String): VirtualFile {
        val file = CRATES_IO.findFileByRelativePath(path)
        assertThat(file).describedAs(path).withFailMessage("File not found").isNotNull()
        return file!!
    }
}
