package com.emberjs.hbs

import com.intellij.openapi.util.TextRange
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.MapEntry.entry
import org.junit.Test

class HbsLinkToReferenceProviderTest {
    @Test fun empty() = doTest("")
    @Test fun oneQuoteOnly() = doTest("\"")
    @Test fun quotesOnly() = doTest("\"\"")
    @Test fun foo() = doTest("\"foo\"", "foo" to 1..4)
    @Test fun foo2() = doTest("'foo'", "foo" to 1..4)
    @Test fun fooBar() = doTest("\"foo.bar\"", "foo" to 1..4, "foo.bar" to 5..8)
    @Test fun fooBar2() = doTest("'foo.bar'", "foo" to 1..4, "foo.bar" to 5..8)
    @Test fun fooBarBaz() = doTest("\"foo.bar.baz\"", "foo" to 1..4, "foo.bar" to 5..8, "foo.bar.baz" to 9..12)
    @Test fun fooBarBaz2() = doTest("'foo.bar.baz'", "foo" to 1..4, "foo.bar" to 5..8, "foo.bar.baz" to 9..12)

    private fun doTest(moduleName: String, vararg ranges: Pair<String, IntRange>) {
        val moduleNames = HbsLinkToReferenceProvider.extractModuleNames(moduleName)
        val expected = ranges.map { entry(it.first, it.second.toTextRange()) }.toTypedArray()
        assertThat(moduleNames).containsOnly(*expected)
    }

    private fun IntRange.toTextRange() = TextRange(this.start, this.last)
}
