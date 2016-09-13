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
            "app/adapters/dependency.js" to listOf("app/models/dependency.js"),
            "app/adapters/application.js" to listOf(),
            "app/components/crate-row.js" to listOf("app/templates/components/crate-row.hbs"),

            "app/controllers/crates.js" to listOf(
                    "app/routes/crates.js",
                    "app/templates/crates.hbs"),

            "app/controllers/crate/versions.js" to listOf(
                    "app/routes/crate/versions.js",
                    "app/templates/crate/versions.hbs"),

            "app/models/crate.js" to listOf("app/serializers/crate.js"),
            "app/models/dependency.js" to listOf("app/adapters/dependency.js"),
            "app/routes/login.js" to listOf("app/templates/login.hbs"),

            "app/routes/crate/index.js" to listOf(
                    "app/controllers/crate/index.js",
                    "app/templates/crate/index.hbs"),

            "app/routes/application.js" to listOf(
                    "app/controllers/application.js",
                    "app/templates/application.hbs"),

            "app/serializers/crate.js" to listOf("app/models/crate.js"),
            "app/services/session.js" to listOf(),

            "app/templates/crates.hbs" to listOf(
                    "app/controllers/crates.js",
                    "app/routes/crates.js"),

            "app/templates/application.hbs" to listOf(
                    "app/controllers/application.js",
                    "app/routes/application.js"),

            "app/templates/crate/versions.hbs" to listOf(
                    "app/controllers/crate/versions.js",
                    "app/routes/crate/versions.js"),

            "app/templates/components/crate-row.hbs" to listOf("app/components/crate-row.js")
    ));

    @Test fun testExample() = doTest(EXAMPLE, mapOf(
            "app/user/adapter.js" to listOf("app/user/model.js"),
            "app/application/adapter.js" to listOf(),
            "app/pet/model.js" to listOf("app/pet/serializer.js"),
            "app/user/model.js" to listOf("app/user/adapter.js"),
            "app/pet/serializer.js" to listOf("app/pet/model.js"),
            "app/session/service.js" to listOf()
    ));

    @Test fun testAptible() = doTest(APTIBLE, mapOf(
            "app/components/billing-header/component.js" to listOf("app/components/billing-header/template.hbs"),

            "app/claim/controller.js" to listOf(
                    "app/claim/route.js",
                    "app/claim/template.hbs"),

            "app/password/reset/controller.js" to listOf(
                    "app/password/reset/route.js",
                    "app/password/reset/template.hbs"),

            "app/claim/route.js" to listOf(
                    "app/claim/controller.js",
                    "app/claim/template.hbs"),

            "app/password/reset/route.js" to listOf(
                    "app/password/reset/controller.js",
                    "app/password/reset/template.hbs"),

            "app/application/route.js" to listOf("app/application/template.hbs"),

            "app/claim/template.hbs" to listOf(
                    "app/claim/controller.js",
                    "app/claim/route.js"),

            "app/application/template.hbs" to listOf("app/application/route.js"),

            "app/password/reset/template.hbs" to listOf(
                    "app/password/reset/controller.js",
                    "app/password/reset/route.js"),

            "app/components/billing-header/template.hbs" to listOf("app/components/billing-header/component.js")
    ))

    private fun doTest(root: VirtualFile, tests: Map<String, List<String>>) {
        SoftAssertions().use {
            for ((path, related) in tests) {
                assertThat(provider.getFiles(root, root.find(path)))
                        .describedAs(path)
                        .containsExactlyElementsOf(related.map { root.find(it) })
            }
        }
    }
}
