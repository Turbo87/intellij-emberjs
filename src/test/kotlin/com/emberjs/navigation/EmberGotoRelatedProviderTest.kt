package com.emberjs.navigation

import com.emberjs.EmberTestFixtures.APTIBLE
import com.emberjs.EmberTestFixtures.CRATES_IO
import com.intellij.openapi.vfs.VirtualFile
import org.assertj.core.api.Assertions.assertThat
import org.junit.Ignore
import org.junit.Test

class EmberGotoRelatedProviderTest {

    val provider = EmberGotoRelatedProvider()

    // Non-POD tests

    @Test public fun testAdapter() =
            doTest(CRATES_IO, "app/adapters/dependency.js",
                    "app/models/dependency.js")

    @Test public fun testApplicationAdapter() =
            doTest(CRATES_IO, "app/adapters/application.js")

    @Test public fun testComponent() =
            doTest(CRATES_IO, "app/components/crate-row.js",
                    "app/templates/components/crate-row.hbs")

    @Test public fun testController() =
            doTest(CRATES_IO, "app/controllers/crates.js",
                    "app/routes/crates.js",
                    "app/templates/crates.hbs")

    @Test public fun testNestedController() =
            doTest(CRATES_IO, "app/controllers/crate/versions.js",
                    "app/routes/crate/versions.js",
                    "app/templates/crate/versions.hbs")

    @Test public fun testModel() =
            doTest(CRATES_IO, "app/models/crate.js",
                    "app/serializers/crate.js")

    @Test public fun testModel2() =
            doTest(CRATES_IO, "app/models/dependency.js",
                    "app/adapters/dependency.js")

    @Test public fun testRoute() =
            doTest(CRATES_IO, "app/routes/login.js",
                    "app/templates/login.hbs")

    @Test public fun testNestedRoute() =
            doTest(CRATES_IO, "app/routes/crate/index.js",
                    "app/controllers/crate/index.js",
                    "app/templates/crate/index.hbs")

    @Test public fun testApplicationRoute() =
            doTest(CRATES_IO, "app/routes/application.js",
                    "app/controllers/application.js",
                    "app/templates/application.hbs")

    @Test public fun testSerializer() =
            doTest(CRATES_IO, "app/serializers/crate.js",
                    "app/models/crate.js")

    @Test public fun testService() =
            doTest(CRATES_IO, "app/services/session.js")

    @Test public fun testTemplate() =
            doTest(CRATES_IO, "app/templates/crates.hbs",
                    "app/controllers/crates.js",
                    "app/routes/crates.js")

    @Test public fun testApplicationTemplate() =
            doTest(CRATES_IO, "app/templates/application.hbs",
                    "app/controllers/application.js",
                    "app/routes/application.js")

    @Test public fun testNestedTemplate() =
            doTest(CRATES_IO, "app/templates/crate/versions.hbs",
                    "app/controllers/crate/versions.js",
                    "app/routes/crate/versions.js")

    @Test public fun testComponentTemplate() =
            doTest(CRATES_IO, "app/templates/components/crate-row.hbs",
                    "app/components/crate-row.js")

    // PODs

    @Ignore @Test public fun testPodAdapter() =
            doTest(APTIBLE, "app/dependency/adapter.js",
                    "app/dependency/model.js")

    @Ignore @Test public fun testPodApplicationAdapter() =
            doTest(APTIBLE, "app/application/adapter.js")

    @Test public fun testPodComponent() =
            doTest(APTIBLE, "app/components/billing-header/component.js",
                    "app/components/billing-header/template.hbs")

    @Test public fun testPodController() =
            doTest(APTIBLE, "app/claim/controller.js",
                    "app/claim/route.js",
                    "app/claim/template.hbs")

    @Test public fun testPodNestedController() =
            doTest(APTIBLE, "app/password/reset/controller.js",
                    "app/password/reset/route.js",
                    "app/password/reset/template.hbs")

    @Ignore @Test public fun testPodModel() =
            doTest(APTIBLE, "app/crate/model.js",
                    "app/crate/serializer.js")

    @Ignore @Test public fun testPodModel2() =
            doTest(APTIBLE, "app/dependency/model.js",
                    "app/dependency/adapter.js")

    @Test public fun testPodRoute() =
            doTest(APTIBLE, "app/claim/route.js",
                    "app/claim/controller.js",
                    "app/claim/template.hbs")

    @Test public fun testPodNestedRoute() =
            doTest(APTIBLE, "app/password/reset/route.js",
                    "app/password/reset/controller.js",
                    "app/password/reset/template.hbs")

    @Test public fun testPodApplicationRoute() =
            doTest(APTIBLE, "app/application/route.js",
                    "app/application/template.hbs")

    @Ignore @Test public fun testPodSerializer() =
            doTest(APTIBLE, "app/crate/serializer.js",
                    "app/crate/model.js")

    @Ignore @Test public fun testPodService() =
            doTest(APTIBLE, "app/session/service.js")

    @Test public fun testPodTemplate() =
            doTest(APTIBLE, "app/claim/template.hbs",
                    "app/claim/controller.js",
                    "app/claim/route.js")

    @Test public fun testPodApplicationTemplate() =
            doTest(APTIBLE, "app/application/template.hbs",
                    "app/application/route.js")

    @Test public fun testPodNestedTemplate() =
            doTest(APTIBLE, "app/password/reset/template.hbs",
                    "app/password/reset/controller.js",
                    "app/password/reset/route.js")

    @Test public fun testPodComponentTemplate() =
            doTest(APTIBLE, "app/components/billing-header/template.hbs",
                    "app/components/billing-header/component.js")

    private fun doTest(root: VirtualFile, path: String, vararg related: String) {
        assertThat(provider.getFiles(root.find(path)))
                .containsExactlyElementsOf(listOf(*related).map { root.find(it) })
    }

    private fun VirtualFile.find(path: String): VirtualFile {
        val file = findFileByRelativePath(path)
        assertThat(file).describedAs(path).withFailMessage("File not found").isNotNull()
        return file!!
    }
}
