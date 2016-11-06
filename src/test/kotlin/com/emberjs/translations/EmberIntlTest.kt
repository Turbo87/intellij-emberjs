package com.emberjs.translations

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import com.intellij.util.indexing.FileBasedIndex
import org.assertj.core.api.Assertions.assertThat
import java.nio.file.Paths

class EmberIntlTest : LightPlatformCodeInsightFixtureTestCase() {

    override fun getTestDataPath(): String? {
        val resource = ClassLoader.getSystemResource("com/emberjs/translations/fixtures")
        return Paths.get(resource.toURI()).toAbsolutePath().toString()
    }

    fun testFindBaseLocale() {
        // Load fixture files into the project
        myFixture.copyDirectoryToProject("ember-intl-with-base-locale", "/")

        // Rebuild index now that the `package.json` file is copied over
        FileBasedIndex.getInstance().requestRebuild(EmberIntlIndex.NAME)

        val psiFile = myFixture.configureByFile("app/templates/base-locale-test.hbs")

        assertThat(EmberIntl.findBaseLocale(psiFile)).isEqualTo("de")
    }
}
