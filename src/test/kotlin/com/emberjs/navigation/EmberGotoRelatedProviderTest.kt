package com.emberjs.navigation

import com.emberjs.EmberTestFixtures.CRATES_IO
import com.emberjs.EmberTestFixtures.CRATES_IO_POD
import com.intellij.openapi.vfs.VirtualFile
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EmberGotoRelatedProviderTest {

    val provider = EmberGotoRelatedProvider()

    // Non-POD tests

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
            doTest("app/routes/login.js",
                    "app/templates/login.hbs")

    @Test public fun testNestedRoute() =
            doTest("app/routes/crate/index.js",
                    "app/controllers/crate/index.js",
                    "app/templates/crate/index.hbs")

    @Test public fun testApplicationRoute() =
            doTest("app/routes/application.js",
                    "app/controllers/application.js",
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
                    "app/controllers/application.js",
                    "app/routes/application.js")

    @Test public fun testNestedTemplate() =
            doTest("app/templates/crate/versions.hbs",
                    "app/controllers/crate/versions.js",
                    "app/routes/crate/versions.js")

    @Test public fun testComponentTemplate() =
            doTest("app/templates/components/crate-row.hbs",
                    "app/components/crate-row.js")

    // PODs

    @Test public fun testPodAdapter() =
            doPodTest("app/dependency/adapter.js",
                    "app/dependency/model.js")

    @Test public fun testPodApplicationAdapter() =
            doPodTest("app/application/adapter.js")

    @Test public fun testPodComponent() =
            doPodTest("app/components/crate-row/component.js",
                    "app/components/crate-row/template.hbs")

    @Test public fun testPodController() =
            doPodTest("app/crates/controller.js",
                    "app/crates/route.js",
                    "app/crates/template.hbs")

    @Test public fun testPodNestedController() =
            doPodTest("app/crate/versions/controller.js",
                    "app/crate/versions/route.js",
                    "app/crate/versions/template.hbs")

    @Test public fun testPodModel() =
            doPodTest("app/crate/model.js",
                    "app/crate/serializer.js")

    @Test public fun testPodModel2() =
            doPodTest("app/dependency/model.js",
                    "app/dependency/adapter.js")

    @Test public fun testPodRoute() =
            doPodTest("app/github-login/route.js",
                    "app/github-login/template.hbs")

    @Test public fun testPodNestedRoute() =
            doPodTest("app/crate/index/route.js",
                    "app/crate/index/controller.js",
                    "app/crate/index/template.hbs")

    @Test public fun testPodApplicationRoute() =
            doPodTest("app/application/route.js",
                    "app/application/template.hbs")

    @Test public fun testPodSerializer() =
            doPodTest("app/crate/serializer.js",
                    "app/crate/model.js")

    @Test public fun testPodService() =
            doPodTest("app/session/service.js")

    @Test public fun testPodTemplate() =
            doPodTest("app/crates/template.hbs",
                    "app/crates/controller.js",
                    "app/crates/route.js")

    @Test public fun testPodApplicationTemplate() =
            doPodTest("app/application/template.hbs",
                    "app/application/route.js")

    @Test public fun testPodNestedTemplate() =
            doPodTest("app/crate/versions/template.hbs",
                    "app/crate/versions/controller.js",
                    "app/crate/versions/route.js")

    @Test public fun testPodComponentTemplate() =
            doPodTest("app/components/crate-row/template.hbs",
                    "app/components/crate-row/component.js")

    private fun doTest(path: String, vararg related: String) {
        assertThat(relatedTo(path)).containsExactlyElementsOf(listOf(*related).map { file(it) })
    }
    private fun doPodTest(path: String, vararg related: String) {
        assertThat(relatedToPod(path)).containsExactlyElementsOf(listOf(*related).map { podFile(it) })
    }

    private fun relatedTo(path: String) = provider.getFiles(file(path))
    private fun relatedToPod(path: String) = provider.getFiles(podFile(path))
    private fun file(path: String): VirtualFile {
        val file = CRATES_IO.findFileByRelativePath(path)
        assertThat(file).describedAs(path).withFailMessage("File not found").isNotNull()
        return file!!
    }
    private fun podFile(path: String): VirtualFile {
        val file = CRATES_IO_POD.findFileByRelativePath(path)
        assertThat(file).describedAs(path).withFailMessage("File not found").isNotNull()
        return file!!
    }
}
