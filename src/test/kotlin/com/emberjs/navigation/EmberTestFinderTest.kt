package com.emberjs.navigation

import com.emberjs.EmberTestFixtures.FIXTURES_PATH
import com.emberjs.index.EmberNameIndex
import com.emberjs.resolver.EmberName
import com.emberjs.utils.use
import com.intellij.psi.PsiManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.indexing.FileBasedIndex
import org.assertj.core.api.Assertions
import org.assertj.core.api.SoftAssertions
import org.junit.Test

class EmberTestFinderTest : BasePlatformTestCase() {

    val finder = EmberTestFinder()

    @Test fun testCratesIo() = doTest("crates.io", mapOf(
            "app/helpers/format-email.js" to listOf("tests/unit/helpers/format-email-test.js"),
            "app/helpers/format-num.js" to listOf("tests/unit/helpers/format-num-test.js"),
            "app/mixins/pagination.js" to listOf("tests/unit/mixins/pagination-test.js")
    ))

    @Test fun testExample() = doTest("example", mapOf(
            "app/pet/model.js" to listOf("tests/unit/pet/model-test.js"),
            "app/pet/serializer.js" to listOf("tests/unit/pet/serializer-test.js"),
            "app/session/service.js" to listOf("tests/unit/session/service-test.js"),
            "app/user/adapter.js" to listOf("tests/unit/user/adapter-test.js"),
            "app/user/model.js" to listOf("tests/unit/user/model-test.js")
    ))

    @Test fun testAptible() = doTest("dashboard.aptible.com", mapOf(
            "app/claim/route.js" to listOf("tests/unit/claim/route-test.js"),
            "app/components/object-select/component.js" to listOf("tests/integration/components/object-select-test.js"),
            "app/components/login-box/component.js" to listOf("tests/unit/components/login-box-test.js"),
            "app/helpers/eq.js" to listOf("tests/integration/helpers/eq-test.js"),
            "app/index/route.js" to listOf("tests/unit/routes/index-test.js"),
            "app/databases/index/route.js" to listOf("tests/unit/routes/databases/index-test.js")
    ))

    override fun getTestDataPath() = FIXTURES_PATH.toString()

    private fun doTest(fixtureName: String, tests: Map<String, List<String>>) {
        // Load fixture files into the project
        val root = myFixture.copyDirectoryToProject(fixtureName, "/")

        // Rebuild index now that the `package.json` file is copied over
        FileBasedIndex.getInstance().requestRebuild(EmberNameIndex.NAME)

        val project = myFixture.project
        val psiManager = PsiManager.getInstance(project)

        SoftAssertions().use {
            for ((path, relatedTests) in tests) {
                val file = root.findFileByRelativePath(path)!!
                val relatedFiles = relatedTests.map { root.findFileByRelativePath(it)!! }

                assertThat(finder.findTestsForClass(psiManager.findFile(file)!!))
                        .describedAs(path)
                        .containsOnly(*relatedFiles.map { psiManager.findFile(it)!! }.toTypedArray())

                relatedFiles.forEach {
                    assertThat(finder.findClassesForTest(psiManager.findFile(it)!!))
                            .describedAs(path)
                            .containsOnly(psiManager.findFile(file)!!)
                }
            }
        }
    }
}

