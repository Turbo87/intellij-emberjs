package com.emberjs.resolver

import com.emberjs.EmberTestFixtures.APTIBLE
import com.emberjs.EmberTestFixtures.CRATES_IO
import com.emberjs.EmberTestFixtures.EXAMPLE
import com.emberjs.utils.find
import com.emberjs.utils.parentEmberModule
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

    @Test fun testAngleBracketsName() {
        assertThat(EmberName.from("component:table")?.angleBracketsName)
                .isEqualTo("Table")
        assertThat(EmberName.from("component:-table")?.angleBracketsName)
                .isEqualTo("-Table")
        assertThat(EmberName.from("component:table-row")?.angleBracketsName)
                .isEqualTo("TableRow")
        assertThat(EmberName.from("component:table/row")?.angleBracketsName)
                .isEqualTo("Table::Row")

        assertThat(EmberName.from("template:components/table")?.angleBracketsName)
                .isEqualTo("Table")
        assertThat(EmberName.from("template:components/-table")?.angleBracketsName)
                .isEqualTo("-Table")
        assertThat(EmberName.from("template:components/table-row")?.angleBracketsName)
                .isEqualTo("TableRow")
        assertThat(EmberName.from("template:components/table/row")?.angleBracketsName)
                .isEqualTo("Table::Row")
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
            "app/adapters/application.js" to "adapter:application",
            "app/components/crate-row.js" to "component:crate-row",
            "app/controllers/keyword/index.js" to "controller:keyword/index",
            "app/helpers/format-num.js" to "helper:format-num",
            "app/initializers/google.js" to "initializer:google",
            "app/mixins/pagination.js" to "mixin:pagination",
            "app/models/crate.js" to "model:crate",
            "app/routes/crate/index.js" to "route:crate/index",
            "app/serializers/crate.js" to "serializer:crate",
            "app/services/session.js" to "service:session",
            "app/templates/application.hbs" to "template:application",
            "app/templates/components/crate-row.hbs" to "template:components/crate-row",
            "tests/unit/helpers/format-num-test.js" to "helper-test:format-num",
            "tests/unit/mixins/pagination-test.js" to "mixin-test:pagination"
    ))

    @Test fun testExample() = doTest(EXAMPLE, mapOf(
            "app/app.js" to null,
            "app/application/adapter.js" to "adapter:application",
            "app/pet/model.js" to "model:pet",
            "app/pet/serializer.js" to "serializer:pet",
            "app/session/service.js" to "service:session",
            "app/application/template.hbs" to "template:application",
            "app/user/adapter.js" to "adapter:user",
            "app/user/model.js" to "model:user",
            "app/user/styles.css" to "styles:user",
            "app/user/styles.scss" to "styles:user",
            "app/user/styles.module.css" to "styles:user",
            "app/user/styles.module.scss" to "styles:user",
            "foo/controller.js" to null,
            "tests/unit/pet/model-test.js" to "model-test:pet",
            "tests/unit/pet/serializer-test.js" to "serializer-test:pet",
            "tests/unit/session/service-test.js" to "service-test:session",
            "tests/unit/user/adapter-test.js" to "adapter-test:user",
            "tests/unit/user/model-test.js" to "model-test:user",
            "app/templates/components/blog-post.handlebars" to "template:components/blog-post",
            "app/components/flat-structured-component.js" to "component:flat-structured-component",
            "app/components/flat-structured-component.hbs" to "template:components/flat-structured-component",
            "app/components/flat-structured-component.scss" to "styles:components/flat-structured-component",
            "app/components/flat-structured-component.css" to "styles:components/flat-structured-component",
            "app/components/flat-structured-component.module.scss" to "styles:components/flat-structured-component",
            "app/components/flat-structured-component.module.css" to "styles:components/flat-structured-component",
            "app/styles/some-route.css" to "styles:some-route",
            "app/styles/components/some-component.css" to "styles:components/some-component",
            "app/components/test-component-nested/index.js" to "component:test-component-nested/index",
            "app/components/test-component-nested/index.hbs" to "template:components/test-component-nested/index",
            "node_modules/my-components/addon/components/button/component.js" to "component:button"
    ))

    @Test fun testAptible() = doTest(APTIBLE, mapOf(
            "app/app/controller.js" to "controller:app",
            "app/app/route.js" to "route:app",
            "app/app/template.hbs" to "template:app",
            "app/app/view.js" to "view:app",
            "app/app/vhosts/route.js" to "route:app/vhosts",
            "app/app/vhosts/template.hbs" to "template:app/vhosts",
            "app/components/change-plan/component.js" to "component:change-plan",
            "app/components/change-plan/template.hbs" to "template:components/change-plan",
            "config/environment.js" to null,
            "tests/integration/components/object-select-test.js" to "component-integration-test:object-select",
            "tests/integration/helpers/eq-test.js" to "helper-integration-test:eq",
            "tests/unit/claim/route-test.js" to "route-test:claim",
            "tests/unit/components/login-box-test.js" to "component-test:login-box",
            "tests/unit/initializers/with-active-class-test.js" to "initializer-test:with-active-class",
            "tests/unit/routes/index-test.js" to "route-test:index",
            "tests/unit/routes/databases/index-test.js" to "route-test:databases/index",
            "tests/acceptance/login-test.js" to "acceptance-test:login",
            "tests/acceptance/databases/create-test.js" to "acceptance-test:databases/create",
            "tests/helpers/start-app.js" to null
    ))

    private fun doTest(root: VirtualFile, tests: Map<String, String?>) {
        SoftAssertions().use {
            for ((path, expectedName) in tests) {
                assertThat(EmberName.from(root.find(path).parentEmberModule!!, root.find(path))?.fullName)
                        .describedAs(path)
                        .apply { if (expectedName == null) isNull() else isEqualTo(expectedName) }

                assertThat(EmberName.from(root.find(path))?.fullName)
                        .describedAs(path)
                        .apply { if (expectedName == null) isNull() else isEqualTo(expectedName) }
            }
        }
    }
}
