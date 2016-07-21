package com.emberjs.resolver

import com.emberjs.EmberTestFixtures.APTIBLE
import com.emberjs.EmberTestFixtures.CRATES_IO
import com.emberjs.EmberTestFixtures.EXAMPLE
import com.emberjs.utils.find
import com.emberjs.utils.use
import com.intellij.openapi.vfs.VirtualFile
import org.assertj.core.api.SoftAssertions
import org.junit.Test

class EmberResolverTest {

    @Test fun testCratesIo() = doTest(CRATES_IO, mapOf(
            Pair("adapter:application", "app/adapters/application.js"),
            Pair("component:crate-row", "app/components/crate-row.js"),
            Pair("component:components/crate-row", null),
            Pair("controller:keyword/index", "app/controllers/keyword/index.js"),
            Pair("helper:format-num", "app/helpers/format-num.js"),
            Pair("initializer:google", "app/initializers/google.js"),
            Pair("mixin:pagination", "app/mixins/pagination.js"),
            Pair("model:crate", "app/models/crate.js"),
            Pair("route:crate/index", "app/routes/crate/index.js"),
            Pair("route:user", null),
            Pair("serializer:crate", "app/serializers/crate.js"),
            Pair("service:session", "app/services/session.js"),
            Pair("template:application", "app/templates/application.hbs"),
            Pair("template:crate-row", null),
            Pair("template:components/crate-row", "app/templates/components/crate-row.hbs"),
            Pair("helper-test:format-num", "tests/unit/helpers/format-num-test.js"),
            Pair("mixin-test:pagination", "tests/unit/mixins/pagination-test.js")
    ))

    @Test fun testExample() = doTest(EXAMPLE, mapOf(
            Pair("adapter:application", "app/application/adapter.js"),
            Pair("model:pet", "app/pet/model.js"),
            Pair("route:pet", null),
            Pair("serializer:pet", "app/pet/serializer.js"),
            Pair("service:session", "app/session/service.js"),
            Pair("template:application", "app/application/template.hbs"),
            Pair("adapter:user", "app/user/adapter.js"),
            Pair("model:user", "app/user/model.js"),
            Pair("model-test:pet", "tests/unit/pet/model-test.js"),
            Pair("serializer-test:pet", "tests/unit/pet/serializer-test.js"),
            Pair("service-test:session", "tests/unit/session/service-test.js"),
            Pair("adapter-test:user", "tests/unit/user/adapter-test.js"),
            Pair("model-test:user", "tests/unit/user/model-test.js"),
            Pair("template:components/blog-post", "app/templates/components/blog-post.handlebars")
    ))

    @Test fun testAptible() = doTest(APTIBLE, mapOf(
            Pair("controller:app", "app/app/controller.js"),
            Pair("route:app", "app/app/route.js"),
            Pair("template:app", "app/app/template.hbs"),
            Pair("view:app", "app/app/view.js"),
            Pair("route:app/vhosts", "app/app/vhosts/route.js"),
            Pair("template:app/vhosts", "app/app/vhosts/template.hbs"),
            Pair("component:change-plan", "app/components/change-plan/component.js"),
            Pair("component:components/change-plan", null),
            Pair("template:change-plan", null),
            Pair("template:components/change-plan", "app/components/change-plan/template.hbs"),
            Pair("component-integration-test:object-select", "tests/integration/components/object-select-test.js"),
            Pair("helper-integration-test:eq", "tests/integration/helpers/eq-test.js"),
            Pair("route-test:claim", "tests/unit/claim/route-test.js"),
            Pair("component-test:login-box", "tests/unit/components/login-box-test.js"),
            Pair("initializer-test:with-active-class", "tests/unit/initializers/with-active-class-test.js"),
            Pair("route-test:index", "tests/unit/routes/index-test.js"),
            Pair("route-test:databases/index", "tests/unit/routes/databases/index-test.js"),
            Pair("acceptance-test:login", "tests/acceptance/login-test.js"),
            Pair("acceptance-test:databases/create", "tests/acceptance/databases/create-test.js")
    ))

    private fun doTest(root: VirtualFile, tests: Map<String, String?>) {
        val resolver = EmberResolver(root)
        SoftAssertions().use {
            for ((name, expectedPath) in tests) {
                val result = resolver.resolve(name)

                val resultPath = result?.path?.removePrefix("MOCK_ROOT:/${root.name}/")
                val message = "'$resultPath' found, expected '$expectedPath'"

                assertThat(result).describedAs(name).overridingErrorMessage(message).apply {
                    when (expectedPath) {
                        null -> isNull()
                        else -> isEqualTo(root.find(expectedPath))
                    }
                }
            }
        }
    }
}

