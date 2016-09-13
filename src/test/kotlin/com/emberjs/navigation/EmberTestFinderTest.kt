package com.emberjs.navigation

import com.emberjs.EmberTestFixtures.APTIBLE
import com.emberjs.EmberTestFixtures.CRATES_IO
import com.emberjs.EmberTestFixtures.EXAMPLE
import com.emberjs.utils.find
import com.emberjs.utils.use
import com.intellij.openapi.vfs.VirtualFile
import org.assertj.core.api.SoftAssertions
import org.junit.Test

class EmberTestFinderTest {

    val finder = EmberTestFinder()

    @Test fun testCratesIo() = doTest(CRATES_IO, mapOf(
            "app/helpers/format-email.js" to listOf("tests/unit/helpers/format-email-test.js"),
            "app/helpers/format-num.js" to listOf("tests/unit/helpers/format-num-test.js"),
            "app/mixins/pagination.js" to listOf("tests/unit/mixins/pagination-test.js")
    ))

    @Test fun testExample() = doTest(EXAMPLE, mapOf(
            "app/pet/model.js" to listOf("tests/unit/pet/model-test.js"),
            "app/pet/serializer.js" to listOf("tests/unit/pet/serializer-test.js"),
            "app/session/service.js" to listOf("tests/unit/session/service-test.js"),
            "app/user/adapter.js" to listOf("tests/unit/user/adapter-test.js"),
            "app/user/model.js" to listOf("tests/unit/user/model-test.js")
    ))

    @Test fun testAptible() = doTest(APTIBLE, mapOf(
            "app/claim/route.js" to listOf("tests/unit/claim/route-test.js"),
            "app/components/object-select/component.js" to listOf("tests/integration/components/object-select-test.js"),
            "app/components/login-box/component.js" to listOf("tests/unit/components/login-box-test.js"),
            "app/helpers/eq.js" to listOf("tests/integration/helpers/eq-test.js"),
            "app/index/route.js" to listOf("tests/unit/routes/index-test.js"),
            "app/databases/index/route.js" to listOf("tests/unit/routes/databases/index-test.js")
    ))

    private fun doTest(root: VirtualFile, tests: Map<String, List<String>>) {
        SoftAssertions().use {
            for ((path, relatedTests) in tests) {
                assertThat(finder.findTestsForClass(root.find(path), root))
                        .describedAs(path)
                        .containsExactlyElementsOf(relatedTests.map { root.find(it) })

                relatedTests.forEach {
                    assertThat(finder.findClassesForTest(root.find(it), root))
                            .describedAs(path)
                            .isEqualTo(root.find(path))
                }
            }
        }
    }
}

