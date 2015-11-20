package com.emberjs

import com.intellij.mock.MockVirtualFile
import com.intellij.openapi.vfs.VirtualFile
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull


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

    private fun checkType(path: String, type: EmberFileType, isPod: Boolean) {
        getFileInfo(path).apply {
            assertNotNull(this)
            assertEquals(type, this?.type)
            assertEquals(isPod, this?.isPod)
        }
    }

    private fun checkNoType(path: String) {
        getFileInfo(path).apply {
            assertNull(this)
        }
    }

    private fun getFileInfo(path: String): EmberFileInfo? {
        val file = path.split("/").fold(null as VirtualFile?, { parent, name ->
            MockVirtualFile("." !in name, name).apply { this.parent = parent }
        })!!

        return EmberFileInfo.from(file)
    }
}
