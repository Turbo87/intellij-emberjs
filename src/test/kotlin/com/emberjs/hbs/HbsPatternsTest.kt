package com.emberjs.hbs

import com.dmarcotte.handlebars.file.HbFileType
import com.emberjs.hbs.HbsPatterns.BLOCK_MUSTACHE_NAME_ID
import com.emberjs.hbs.HbsPatterns.SIMPLE_MUSTACHE_NAME_ID
import com.emberjs.hbs.HbsPatterns.SUB_EXPR_NAME_ID
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiElement
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import org.assertj.core.api.Assertions.assertThat

class HbsPatternsTest : LightPlatformCodeInsightFixtureTestCase() {
    fun testSimpleMustache() = with(SIMPLE_MUSTACHE_NAME_ID) {
        test("{{foo<caret>}}")
        test("{{fo<caret>o}}")
        test("{{#fo<caret>}}", false)
        test("{{foo (bar<caret>)}}", false)
        test("{{foo bar=(baz<caret>)}}", false)
    }

    fun testBlockMustache() = with(BLOCK_MUSTACHE_NAME_ID) {
        test("{{foo<caret>}}", false)
        test("{{fo<caret>o}}", false)
        test("{{#fo<caret>}}")
        test("{{/fo<caret>}}", false)
        test("{{foo (bar<caret>)}}", false)
        test("{{foo bar=(baz<caret>)}}", false)
    }

    fun testSubExpression() = with(SUB_EXPR_NAME_ID) {
        test("{{foo<caret>}}", false)
        test("{{fo<caret>o}}", false)
        test("{{#fo<caret>}}", false)
        test("{{/fo<caret>}}", false)
        test("{{foo (ba<caret>r baz)}}")
        test("{{foo (bar ba<caret>z)}}", false)
        test("{{foo bar=(ba<caret>z)}}")
        test("{{foo bar=(baz ab<caret>c)}}", false)
    }

    private fun <T : PsiElement> PsiElementPattern.Capture<T>.test(text: String, matches: Boolean = true) {
        // Load text into the Editor
        myFixture.configureByText(HbFileType.INSTANCE, text)

        // Retrieve `PsiElement` in front of the caret
        // TODO figure out this off-by-one thing here
        val offset = myFixture.editor.caretModel.offset - 1
        val element = myFixture.file.findElementAt(offset)

        // Check if the pattern accepts the element
        assertThat(element).isNotNull()
        assertThat(accepts(element)).describedAs(text).isEqualTo(matches)
    }
}
