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
            //Pair("app/initializers/google.js", "initializer:google"),
            //Pair("app/mixins/pagination.js", "mixin:pagination"),
            Pair("app/models/crate.js", "model:crate"),
            Pair("app/routes/crate/index.js", "route:crate/index"),
            Pair("app/serializers/crate.js", "serializer:crate"),
            Pair("app/services/session.js", "service:session"),
            Pair("app/templates/application.hbs", "template:application"),
            Pair("app/templates/components/crate-row.hbs", "template:components/crate-row")
    ))

    @Test fun testExample() = doTest(EXAMPLE, mapOf(
            Pair("app/application/adapter.js", "adapter:application"),
            Pair("app/pet/model.js", "model:pet"),
            Pair("app/pet/serializer.js", "serializer:pet"),
            Pair("app/session/service.js", "service:session"),
            Pair("app/application/template.hbs", "template:application"),
            Pair("app/user/adapter.js", "adapter:user"),
            Pair("app/user/model.js", "model:user")
    ))

    @Test fun testAptible() = doTest(APTIBLE, mapOf(
            Pair("app/app/controller.js", "controller:app"),
            Pair("app/app/route.js", "route:app"),
            Pair("app/app/template.hbs", "template:app"),
            //Pair("app/app/view.js", "view:app"),
            Pair("app/app/vhosts/route.js", "route:app/vhosts"),
            Pair("app/app/vhosts/template.hbs", "template:app/vhosts"),
            Pair("app/components/change-plan/component.js", "component:change-plan"),
            Pair("app/components/change-plan/template.hbs", "template:components/change-plan")
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
