package com.emberjs.utils

import org.junit.Test
import kotlin.test.assertEquals

public class EmberStringUtilsTest {

    @Test public fun `classify normal string`() = doTest("my favorite items", "MyFavoriteItems")
    @Test public fun `classify dasherized string`() = doTest("css-class-name", "CssClassName")
    @Test public fun `classify underscored string`() = doTest("action_name", "ActionName")
    @Test public fun `classify namespaced camelized string`() = doTest("privateDocs/ownerInvoice", "PrivateDocs/OwnerInvoice")
    @Test public fun `classify namespaced underscored string`() = doTest("private_docs/owner_invoice", "PrivateDocs/OwnerInvoice")
    @Test public fun `classify namespaced dasherized string`() = doTest("private-docs/owner-invoice", "PrivateDocs/OwnerInvoice")
    @Test public fun `classify prefixed dasherized string`() = doTest("-view-registry", "_ViewRegistry")
    @Test public fun `classify namespaced prefixed dasherized string`() = doTest("components/-text-field", "Components/_TextField")
    @Test public fun `classify underscore-prefixed underscored string`() = doTest("_Foo_Bar", "_FooBar")
    @Test public fun `classify underscore-prefixed dasherized string`() = doTest("_Foo-Bar", "_FooBar")
    @Test public fun `classify underscore-prefixed-namespaced underscore-prefixed string`() = doTest("_foo/_bar", "_Foo/_Bar")
    @Test public fun `classify dash-prefixed-namespaced underscore-prefixed string`() = doTest("-foo/_bar", "_Foo/_Bar")
    @Test public fun `classify dash-prefixed-namespaced dash-prefixed string`() = doTest("-foo/-bar", "_Foo/_Bar")
    @Test public fun `does nothing with classified string`() = doTest("InnerHTML", "InnerHTML")
    @Test public fun `does nothing with classified prefixed string`() = doTest("_FooBar", "_FooBar")

    private fun doTest(input: String, expected: String) {
        assertEquals(expected, input.classify())
    }
}
