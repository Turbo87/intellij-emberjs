package com.emberjs.navigation

import com.emberjs.EmberTestFixtures.FIXTURES_PATH
import com.emberjs.index.EmberNameIndex
import com.emberjs.utils.use
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.indexing.FileBasedIndex
import org.assertj.core.api.SoftAssertions
import org.junit.Test

class EmberGotoRelatedProviderTest : BasePlatformTestCase() {

    val provider = EmberGotoRelatedProvider()

    @Test fun testCratesIo() = doTest("crates.io", mapOf(
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
    ))

    @Test fun testExample() = doTest("example", mapOf(
            "app/user/adapter.js" to listOf("app/user/model.js"),
            "app/application/adapter.js" to listOf(),
            "app/pet/model.js" to listOf("app/pet/serializer.js"),
            "app/user/model.js" to listOf("app/user/adapter.js"),
            "app/pet/serializer.js" to listOf("app/pet/model.js"),
            "app/session/service.js" to listOf(),
            "app/components/flat-structured-component.js" to listOf(
                    "app/components/flat-structured-component.scss",
                    "app/components/flat-structured-component.module.css",
                    "app/components/flat-structured-component.module.scss",
                    "app/components/flat-structured-component.css",
                    "app/components/flat-structured-component.hbs"
            ),
            "app/components/flat-structured-component.hbs" to listOf(
                    "app/components/flat-structured-component.scss",
                    "app/components/flat-structured-component.module.css",
                    "app/components/flat-structured-component.module.scss",
                    "app/components/flat-structured-component.css",
                    "app/components/flat-structured-component.js"
            ),
            "app/components/test-component-nested/index.js" to listOf("app/components/test-component-nested/index.hbs"),
            "app/components/test-component-nested/index.hbs" to listOf("app/components/test-component-nested/index.js")
    ))

    @Test fun testAptible() = doTest("dashboard.aptible.com", mapOf(
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

    override fun getTestDataPath() = FIXTURES_PATH.toString()

    private fun doTest(fixtureName: String, tests: Map<String, List<String>>) {
        // Load fixture files into the project
        val root = myFixture.copyDirectoryToProject(fixtureName, "/")

        val project = myFixture.project

        SoftAssertions().use {
            for ((path, related) in tests) {
                val file = root.findFileByRelativePath(path)!!

                assertThat(provider.getItems(file, project).map { it.second })
                        .describedAs(path)
                        .containsOnly(*related.map { root.findFileByRelativePath(it)!! }.toTypedArray())
            }
        }
    }
}
