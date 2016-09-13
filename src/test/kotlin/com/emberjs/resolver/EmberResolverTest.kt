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
            "adapter:application" to "app/adapters/application.js",
            "component:crate-row" to "app/components/crate-row.js",
            "component:components/crate-row" to null,
            "controller:keyword/index" to "app/controllers/keyword/index.js",
            "helper:format-num" to "app/helpers/format-num.js",
            "initializer:google" to "app/initializers/google.js",
            "mixin:pagination" to "app/mixins/pagination.js",
            "model:crate" to "app/models/crate.js",
            "route:crate/index" to "app/routes/crate/index.js",
            "route:user" to null,
            "serializer:crate" to "app/serializers/crate.js",
            "service:session" to "app/services/session.js",
            "template:application" to "app/templates/application.hbs",
            "template:crate-row" to null,
            "template:components/crate-row" to "app/templates/components/crate-row.hbs",
            "helper-test:format-num" to "tests/unit/helpers/format-num-test.js",
            "mixin-test:pagination" to "tests/unit/mixins/pagination-test.js"
    ))

    @Test fun testExample() = doTest(EXAMPLE, mapOf(
            "adapter:application" to "app/application/adapter.js",
            "model:pet" to "app/pet/model.js",
            "route:pet" to null,
            "serializer:pet" to "app/pet/serializer.js",
            "service:session" to "app/session/service.js",
            "template:application" to "app/application/template.hbs",
            "adapter:user" to "app/user/adapter.js",
            "model:user" to "app/user/model.js",
            "model-test:pet" to "tests/unit/pet/model-test.js",
            "serializer-test:pet" to "tests/unit/pet/serializer-test.js",
            "service-test:session" to "tests/unit/session/service-test.js",
            "adapter-test:user" to "tests/unit/user/adapter-test.js",
            "model-test:user" to "tests/unit/user/model-test.js",
            "template:components/blog-post" to "app/templates/components/blog-post.handlebars"
    ))

    @Test fun testAptible() = doTest(APTIBLE, mapOf(
            "controller:app" to "app/app/controller.js",
            "route:app" to "app/app/route.js",
            "template:app" to "app/app/template.hbs",
            "view:app" to "app/app/view.js",
            "route:app/vhosts" to "app/app/vhosts/route.js",
            "template:app/vhosts" to "app/app/vhosts/template.hbs",
            "component:change-plan" to "app/components/change-plan/component.js",
            "component:components/change-plan" to null,
            "template:change-plan" to null,
            "template:components/change-plan" to "app/components/change-plan/template.hbs",
            "component-integration-test:object-select" to "tests/integration/components/object-select-test.js",
            "helper-integration-test:eq" to "tests/integration/helpers/eq-test.js",
            "route-test:claim" to "tests/unit/claim/route-test.js",
            "component-test:login-box" to "tests/unit/components/login-box-test.js",
            "initializer-test:with-active-class" to "tests/unit/initializers/with-active-class-test.js",
            "route-test:index" to "tests/unit/routes/index-test.js",
            "route-test:databases/index" to "tests/unit/routes/databases/index-test.js",
            "acceptance-test:login" to "tests/acceptance/login-test.js",
            "acceptance-test:databases/create" to "tests/acceptance/databases/create-test.js"
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

