package com.emberjs.resolver

import com.emberjs.EmberTestFixtures.APTIBLE
import com.emberjs.EmberTestFixtures.CRATES_IO
import com.emberjs.EmberTestFixtures.EXAMPLE
import com.emberjs.utils.find
import com.emberjs.utils.use
import com.intellij.openapi.vfs.VirtualFile
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.Test

class EmberNameTest {

    @Test fun test() {
        val name = EmberName.from("route:crate/index")!!
        assertThat(name.type).isEqualTo("route")
        assertThat(name.name).isEqualTo("crate/index")
        assertThat(name.fullName).isEqualTo("route:crate/index")
        assertThat(name.displayName).isEqualTo("crate.index route")
        assertThat(name.isTest).isFalse()
    }

    @Test fun testComponentTemplate() {
        val name = EmberName.from("template:components/table-row")!!
        assertThat(name.type).isEqualTo("template")
        assertThat(name.name).isEqualTo("components/table-row")
        assertThat(name.fullName).isEqualTo("template:components/table-row")
        assertThat(name.displayName).isEqualTo("table-row component-template")
        assertThat(name.isTest).isFalse()
    }

    @Test fun testContollerTest() {
        val name = EmberName.from("controller-test:application")!!
        assertThat(name.type).isEqualTo("controller-test")
        assertThat(name.name).isEqualTo("application")
        assertThat(name.fullName).isEqualTo("controller-test:application")
        assertThat(name.displayName).isEqualTo("application controller-test")
        assertThat(name.isTest).isTrue()
    }

    @Test fun testInvalidName() {
        val name = EmberName.from("foobar")
        assertThat(name).isNull()
    }

    @Test fun testCratesIo() = doTest(CRATES_IO, mapOf(
            Pair("app/adapters/application.js", "adapter:application"),
            Pair("app/components/crate-row.js", "component:crate-row"),
            Pair("app/controllers/keyword/index.js", "controller:keyword/index"),
            Pair("app/helpers/format-num.js", "helper:format-num"),
            Pair("app/initializers/google.js", "initializer:google"),
            Pair("app/mixins/pagination.js", "mixin:pagination"),
            Pair("app/models/crate.js", "model:crate"),
            Pair("app/routes/crate/index.js", "route:crate/index"),
            Pair("app/serializers/crate.js", "serializer:crate"),
            Pair("app/services/session.js", "service:session"),
            Pair("app/templates/application.hbs", "template:application"),
            Pair("app/templates/components/crate-row.hbs", "template:components/crate-row"),
            Pair("tests/unit/helpers/format-num-test.js", "helper-test:format-num"),
            Pair("tests/unit/mixins/pagination-test.js", "mixin-test:pagination")
    ))

    @Test fun testExample() = doTest(EXAMPLE, mapOf(
            Pair("app/app.js", null),
            Pair("app/application/adapter.js", "adapter:application"),
            Pair("app/pet/model.js", "model:pet"),
            Pair("app/pet/serializer.js", "serializer:pet"),
            Pair("app/session/service.js", "service:session"),
            Pair("app/application/template.hbs", "template:application"),
            Pair("app/user/adapter.js", "adapter:user"),
            Pair("app/user/model.js", "model:user"),
            Pair("foo/controller.js", null),
            Pair("tests/unit/pet/model-test.js", "model-test:pet"),
            Pair("tests/unit/pet/serializer-test.js", "serializer-test:pet"),
            Pair("tests/unit/session/service-test.js", "service-test:session"),
            Pair("tests/unit/user/adapter-test.js", "adapter-test:user"),
            Pair("tests/unit/user/model-test.js", "model-test:user"),
            Pair("app/templates/components/blog-post.handlebars", "template:components/blog-post")
    ))

    @Test fun testAptible() = doTest(APTIBLE, mapOf(
            Pair("app/app/controller.js", "controller:app"),
            Pair("app/app/route.js", "route:app"),
            Pair("app/app/template.hbs", "template:app"),
            Pair("app/app/view.js", "view:app"),
            Pair("app/app/vhosts/route.js", "route:app/vhosts"),
            Pair("app/app/vhosts/template.hbs", "template:app/vhosts"),
            Pair("app/components/change-plan/component.js", "component:change-plan"),
            Pair("app/components/change-plan/template.hbs", "template:components/change-plan"),
            Pair("config/environment.js", null),
            Pair("tests/integration/components/object-select-test.js", "component-integration-test:object-select"),
            Pair("tests/integration/helpers/eq-test.js", "helper-integration-test:eq"),
            Pair("tests/unit/claim/route-test.js", "route-test:claim"),
            Pair("tests/unit/components/login-box-test.js", "component-test:login-box"),
            Pair("tests/unit/initializers/with-active-class-test.js", "initializer-test:with-active-class"),
            Pair("tests/unit/routes/index-test.js", "route-test:index"),
            Pair("tests/unit/routes/databases/index-test.js", "route-test:databases/index"),
            Pair("tests/acceptance/login-test.js", "acceptance-test:login"),
            Pair("tests/acceptance/databases/create-test.js", "acceptance-test:databases/create"),
            Pair("tests/helpers/start-app.js", null)
    ))

    private fun doTest(root: VirtualFile, tests: Map<String, String?>) {
        SoftAssertions().use {
            for ((path, expectedName) in tests) {
                assertThat(EmberName.from(root, root.find(path))?.fullName)
                        .describedAs(path)
                        .isEqualTo(expectedName)
            }
        }
    }
}
