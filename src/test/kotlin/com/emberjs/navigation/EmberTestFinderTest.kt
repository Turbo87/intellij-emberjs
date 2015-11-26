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
            Pair("app/helpers/format-email.js", listOf("tests/unit/helpers/format-email-test.js")),
            Pair("app/helpers/format-num.js", listOf("tests/unit/helpers/format-num-test.js")),
            Pair("app/mixins/pagination.js", listOf("tests/unit/mixins/pagination-test.js"))
    ))

    @Test fun testExample() = doTest(EXAMPLE, mapOf(
            Pair("app/pet/model.js", listOf("tests/unit/pet/model-test.js")),
            Pair("app/pet/serializer.js", listOf("tests/unit/pet/serializer-test.js")),
            Pair("app/session/service.js", listOf("tests/unit/session/service-test.js")),
            Pair("app/user/adapter.js", listOf("tests/unit/user/adapter-test.js")),
            Pair("app/user/model.js", listOf("tests/unit/user/model-test.js"))
    ))

    @Test fun testAptible() = doTest(APTIBLE, mapOf(
            Pair("app/claim/route.js", listOf("tests/unit/claim/route-test.js")),
            Pair("app/components/object-select/component.js", listOf("tests/integration/components/object-select-test.js")),
            Pair("app/components/login-box/component.js", listOf("tests/unit/components/login-box-test.js")),
            Pair("app/helpers/eq.js", listOf("tests/integration/helpers/eq-test.js")),
            Pair("app/index/route.js", listOf("tests/unit/routes/index-test.js")),
            Pair("app/databases/index/route.js", listOf("tests/unit/routes/databases/index-test.js"))
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

