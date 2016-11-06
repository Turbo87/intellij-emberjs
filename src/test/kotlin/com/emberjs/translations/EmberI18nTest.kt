package com.emberjs.translations

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import com.intellij.util.indexing.FileBasedIndex
import org.assertj.core.api.Assertions.assertThat
import java.nio.file.Paths

class EmberI18nTest : LightPlatformCodeInsightFixtureTestCase() {

    override fun getTestDataPath(): String? {
        val resource = ClassLoader.getSystemResource("com/emberjs/translations/fixtures")
        return Paths.get(resource.toURI()).toAbsolutePath().toString()
    }

    fun testFindDefaultLocale() {
        // Load fixture files into the project
        myFixture.copyDirectoryToProject("ember-i18n", "/")

        // Rebuild index now that the `package.json` file is copied over
        FileBasedIndex.getInstance().requestRebuild(EmberIntlIndex.NAME)

        val psiFile = myFixture.configureByFile("app/templates/application.hbs")

        assertThat(EmberI18n.findDefaultLocale(psiFile)).isEqualTo("zh")
    }
}
