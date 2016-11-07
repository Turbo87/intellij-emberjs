package com.emberjs.translations

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import com.intellij.util.indexing.FileBasedIndex
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import java.nio.file.Paths

class EmberI18nIndexTest : LightPlatformCodeInsightFixtureTestCase() {

    fun testSimpleKey() = doTest("foo", mapOf("en" to "bar baz", "de" to "Bar Baz"))
    fun testDottedKey() = doTest("user.edit.title", mapOf("en" to "Edit User", "de" to "Benutzer editieren"))
    fun testNestedKey() = doTest("button.add_user.text", mapOf("en" to "Add", "de" to "Hinzufügen"))
    fun testCombinedKey() = doTest("nested.and.dotted", mapOf("en" to "foobar"))
    fun testNotDefaultExport() = doTest("bar", emptyMap())
    fun testWithoutDependency() = doTest("foo", emptyMap(), "no-dependencies")

    fun testAllKeys() {
        loadFixture("ember-i18n")

        val keys = EmberI18nIndex.getTranslationKeys(myFixture.project)
        assertThat(keys).contains("foo", "user.edit.title", "button.add_user.title")
    }

    fun testGetFilesWithKey() {
        loadFixture("ember-i18n")

        val result = EmberI18nIndex.getFilesWithKey("user.edit.title", myFixture.project).map { it.parent.name }
        assertThat(result).containsOnly("de", "en")
    }

    override fun getTestDataPath(): String? {
        val resource = ClassLoader.getSystemResource("com/emberjs/translations/fixtures")
        return Paths.get(resource.toURI()).toAbsolutePath().toString()
    }

    private fun loadFixture(fixtureName: String) {
        // Load fixture files into the project
        myFixture.copyDirectoryToProject(fixtureName, "/")

        // Rebuild index now that the `package.json` file is copied over
        FileBasedIndex.getInstance().requestRebuild(EmberI18nIndex.NAME)
    }

    private fun doTest(key: String, expected: Map<String, String>, fixtureName: String = "ember-i18n") {
        loadFixture(fixtureName)

        val translations = EmberI18nIndex.getTranslations(key, myFixture.project)
        if (expected.isEmpty()) {
            assertThat(translations).isEmpty()
        } else {
            val _expected = expected.entries.map { entry(it.key, it.value) }.toTypedArray()
            assertThat(translations).containsOnly(*_expected)
        }
    }
}
