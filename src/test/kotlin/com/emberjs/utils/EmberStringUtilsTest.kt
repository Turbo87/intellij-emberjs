package com.emberjs.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EmberStringUtilsTest {

    @Test fun `classify normal string`() = classify("my favorite items", "MyFavoriteItems")
    @Test fun `classify dasherized string`() = classify("css-class-name", "CssClassName")
    @Test fun `classify underscored string`() = classify("action_name", "ActionName")
    @Test fun `classify namespaced camelized string`() = classify("privateDocs/ownerInvoice", "PrivateDocs/OwnerInvoice")
    @Test fun `classify namespaced underscored string`() = classify("private_docs/owner_invoice", "PrivateDocs/OwnerInvoice")
    @Test fun `classify namespaced dasherized string`() = classify("private-docs/owner-invoice", "PrivateDocs/OwnerInvoice")
    @Test fun `classify prefixed dasherized string`() = classify("-view-registry", "_ViewRegistry")
    @Test fun `classify namespaced prefixed dasherized string`() = classify("components/-text-field", "Components/_TextField")
    @Test fun `classify underscore-prefixed underscored string`() = classify("_Foo_Bar", "_FooBar")
    @Test fun `classify underscore-prefixed dasherized string`() = classify("_Foo-Bar", "_FooBar")
    @Test fun `classify underscore-prefixed-namespaced underscore-prefixed string`() = classify("_foo/_bar", "_Foo/_Bar")
    @Test fun `classify dash-prefixed-namespaced underscore-prefixed string`() = classify("-foo/_bar", "_Foo/_Bar")
    @Test fun `classify dash-prefixed-namespaced dash-prefixed string`() = classify("-foo/-bar", "_Foo/_Bar")
    @Test fun `does nothing with classified string`() = classify("InnerHTML", "InnerHTML")
    @Test fun `does nothing with classified prefixed string`() = classify("_FooBar", "_FooBar")

    private fun classify(input: String, expected: String) {
        assertThat(input.classify()).isEqualTo(expected)
    }
}
