package com.emberjs.intl

import com.dmarcotte.handlebars.file.HbFileType
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import com.intellij.util.indexing.FileBasedIndex
import org.assertj.core.api.Assertions.assertThat
import java.nio.file.Paths

class EmberIntlHbsReferenceTest : LightPlatformCodeInsightFixtureTestCase() {

    override fun getTestDataPath(): String? {
        val resource = ClassLoader.getSystemResource("com/emberjs/intl/fixtures")
        return Paths.get(resource.toURI()).toAbsolutePath().toString()
    }

    override fun setUp() {
        super.setUp()

        // Load fixture files into the project
        myFixture.copyDirectoryToProject("ember-intl", "/")

        // Rebuild index now that the `package.json` file is copied over
        FileBasedIndex.getInstance().requestRebuild(EmberIntlIndex.NAME)
    }

    fun testCompletion1() {
        myFixture.configureByText(HbFileType.INSTANCE, "{{t \"quo<caret>\"}}")

        val result = myFixture.completeBasic().map { it.lookupString }

        assertThat(result).containsExactly("quote-test1", "quote-test2", "quote-test3", "quote-test4")
    }

    fun testCompletion2() {
        myFixture.configureByText(HbFileType.INSTANCE, "{{t \"<caret>\"}}")

        val result = myFixture.completeBasic().map { it.lookupString }

        assertThat(result).containsExactly("foo", "long-string", "nested.key.with-child", "parent.child",
                "quote-test1", "quote-test2", "quote-test3", "quote-test4")
    }

    fun testReference1() {
        myFixture.configureByText(HbFileType.INSTANCE, "{{t \"long-st<caret>ring\"}}")

        val reference = myFixture.getReferenceAtCaretPosition()!!
        val result = reference.resolve()

        assertThat(result).isNotNull()
    }

    fun testNestedReference() {
        myFixture.configureByText(HbFileType.INSTANCE, "{{t \"nested.key.<caret>with-child\"}}")

        val reference = myFixture.getReferenceAtCaretPosition()!!
        val result = reference.resolve()

        assertThat(result).isNotNull()
    }

    fun testReference2() {
        myFixture.configureByText(HbFileType.INSTANCE, "{{t \"unkn<caret>own\"}}")

        val reference = myFixture.getReferenceAtCaretPosition()!!
        val result = reference.resolve()

        assertThat(result).isNull()
    }
}
