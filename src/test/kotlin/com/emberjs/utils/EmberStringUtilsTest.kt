package com.emberjs.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EmberStringUtilsTest {

    @Test fun `classify normal string`() = doTest("my favorite items", "MyFavoriteItems")
    @Test fun `classify dasherized string`() = doTest("css-class-name", "CssClassName")
    @Test fun `classify underscored string`() = doTest("action_name", "ActionName")
    @Test fun `classify namespaced camelized string`() = doTest("privateDocs/ownerInvoice", "PrivateDocs/OwnerInvoice")
    @Test fun `classify namespaced underscored string`() = doTest("private_docs/owner_invoice", "PrivateDocs/OwnerInvoice")
    @Test fun `classify namespaced dasherized string`() = doTest("private-docs/owner-invoice", "PrivateDocs/OwnerInvoice")
    @Test fun `classify prefixed dasherized string`() = doTest("-view-registry", "_ViewRegistry")
    @Test fun `classify namespaced prefixed dasherized string`() = doTest("components/-text-field", "Components/_TextField")
    @Test fun `classify underscore-prefixed underscored string`() = doTest("_Foo_Bar", "_FooBar")
    @Test fun `classify underscore-prefixed dasherized string`() = doTest("_Foo-Bar", "_FooBar")
    @Test fun `classify underscore-prefixed-namespaced underscore-prefixed string`() = doTest("_foo/_bar", "_Foo/_Bar")
    @Test fun `classify dash-prefixed-namespaced underscore-prefixed string`() = doTest("-foo/_bar", "_Foo/_Bar")
    @Test fun `classify dash-prefixed-namespaced dash-prefixed string`() = doTest("-foo/-bar", "_Foo/_Bar")
    @Test fun `does nothing with classified string`() = doTest("InnerHTML", "InnerHTML")
    @Test fun `does nothing with classified prefixed string`() = doTest("_FooBar", "_FooBar")

    private fun doTest(input: String, expected: String) {
        assertThat(input.classify()).isEqualTo(expected)
    }
}
