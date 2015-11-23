package com.emberjs.navigation

import com.emberjs.EmberTestFixtures.APTIBLE
import com.emberjs.EmberTestFixtures.CRATES_IO
import com.emberjs.EmberTestFixtures.EXAMPLE
import com.emberjs.utils.find
import com.emberjs.utils.use
import com.intellij.openapi.vfs.VirtualFile
import org.assertj.core.api.SoftAssertions
import org.junit.Test

class EmberGotoRelatedProviderTest {

    val provider = EmberGotoRelatedProvider()

    @Test fun testCratesIo() = doTest(CRATES_IO, mapOf(
            Pair("app/adapters/dependency.js", listOf("app/models/dependency.js")),
            Pair("app/adapters/application.js", listOf()),
            Pair("app/components/crate-row.js", listOf("app/templates/components/crate-row.hbs")),

            Pair("app/controllers/crates.js", listOf(
                    "app/routes/crates.js",
                    "app/templates/crates.hbs")),

            Pair("app/controllers/crate/versions.js", listOf(
                    "app/routes/crate/versions.js",
                    "app/templates/crate/versions.hbs")),

            Pair("app/models/crate.js", listOf("app/serializers/crate.js")),
            Pair("app/models/dependency.js", listOf("app/adapters/dependency.js")),
            Pair("app/routes/login.js", listOf("app/templates/login.hbs")),

            Pair("app/routes/crate/index.js", listOf(
                    "app/controllers/crate/index.js",
                    "app/templates/crate/index.hbs")),

            Pair("app/routes/application.js", listOf(
                    "app/controllers/application.js",
                    "app/templates/application.hbs")),

            Pair("app/serializers/crate.js", listOf("app/models/crate.js")),
            Pair("app/services/session.js", listOf()),

            Pair("app/templates/crates.hbs", listOf(
                    "app/controllers/crates.js",
                    "app/routes/crates.js")),

            Pair("app/templates/application.hbs", listOf(
                    "app/controllers/application.js",
                    "app/routes/application.js")),

            Pair("app/templates/crate/versions.hbs", listOf(
                    "app/controllers/crate/versions.js",
                    "app/routes/crate/versions.js")),

            Pair("app/templates/components/crate-row.hbs", listOf("app/components/crate-row.js"))
    ));

    @Test fun testExample() = doTest(EXAMPLE, mapOf(
            Pair("app/user/adapter.js", listOf("app/user/model.js")),
            Pair("app/application/adapter.js", listOf()),
            Pair("app/pet/model.js", listOf("app/pet/serializer.js")),
            Pair("app/user/model.js", listOf("app/user/adapter.js")),
            Pair("app/pet/serializer.js", listOf("app/pet/model.js")),
            Pair("app/session/service.js", listOf())
    ));

    @Test fun testAptible() = doTest(APTIBLE, mapOf(
            Pair("app/components/billing-header/component.js", listOf("app/components/billing-header/template.hbs")),

            Pair("app/claim/controller.js", listOf(
                    "app/claim/route.js",
                    "app/claim/template.hbs")),

            Pair("app/password/reset/controller.js", listOf(
                    "app/password/reset/route.js",
                    "app/password/reset/template.hbs")),

            Pair("app/claim/route.js", listOf(
                    "app/claim/controller.js",
                    "app/claim/template.hbs")),

            Pair("app/password/reset/route.js", listOf(
                    "app/password/reset/controller.js",
                    "app/password/reset/template.hbs")),

            Pair("app/application/route.js", listOf("app/application/template.hbs")),

            Pair("app/claim/template.hbs", listOf(
                    "app/claim/controller.js",
                    "app/claim/route.js")),

            Pair("app/application/template.hbs", listOf("app/application/route.js")),

            Pair("app/password/reset/template.hbs", listOf(
                    "app/password/reset/controller.js",
                    "app/password/reset/route.js")),

            Pair("app/components/billing-header/template.hbs", listOf("app/components/billing-header/component.js"))
    ))

    private fun doTest(root: VirtualFile, tests: Map<String, List<String>>) {
        SoftAssertions().use {
            for ((path, related) in tests) {
                assertThat(provider.getFiles(root.find(path)))
                        .containsExactlyElementsOf(related.map { root.find(it) })
            }
        }
    }
}
