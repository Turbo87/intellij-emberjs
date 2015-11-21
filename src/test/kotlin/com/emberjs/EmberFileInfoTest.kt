package com.emberjs

import com.intellij.mock.MockVirtualFile
import com.intellij.openapi.vfs.VirtualFile
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


public class EmberFileInfoTest {

    @Test public fun testMissingAppFolder() =
            checkNoType("crates.io/crate/route.js")

    @Test public fun testMissingType() =
            checkNoType("crates.io/app/crate/index.js")

    @Test public fun testRoute() =
            checkType("crates.io/app/routes/application.js", EmberFileType.ROUTE, false)

    @Test public fun testPodRoute() =
            checkType("crates.io/app/application/route.js", EmberFileType.ROUTE, true)

    @Test public fun testNestedController() =
            checkType("crates.io/app/controllers/crate/version.js", EmberFileType.CONTROLLER, false)

    @Test public fun testNestedPodController() =
            checkType("crates.io/app/crate/version/controller.js", EmberFileType.CONTROLLER, true)

    @Test public fun testHelper() =
            checkType("crates.io/app/helpers/from-now.js", EmberFileType.HELPER, false)

    @Test public fun testComponent() =
            checkType("crates.io/app/components/x-select.js", EmberFileType.COMPONENT, false)

    @Test public fun testPodComponent() =
            checkType("crates.io/app/components/x-select/component.js", EmberFileType.COMPONENT, true)

    @Test public fun testComponentTemplate() =
            checkType("crates.io/app/templates/components/x-select.hbs", EmberFileType.COMPONENT_TEMPLATE, false)

    @Test public fun testPodComponentTemplate() =
            checkType("crates.io/app/components/x-select/template.hbs", EmberFileType.COMPONENT_TEMPLATE, true)

    private fun checkType(path: String, type: EmberFileType, isPod: Boolean) {
        getFileInfo(path).apply {
            assertThat(this).isNotNull()
            assertThat(this?.type).isEqualTo(type)
            assertThat(this?.isPod).isEqualTo(isPod)
        }
    }

    private fun checkNoType(path: String) {
        getFileInfo(path).apply {
            assertThat(this).isNull()
        }
    }

    private fun getFileInfo(path: String): EmberFileInfo? {
        val file = path.split("/").fold(null as VirtualFile?, { parent, name ->
            MockVirtualFile("." !in name, name).apply { this.parent = parent }
        })!!

        return EmberFileInfo.from(file)
    }
}
