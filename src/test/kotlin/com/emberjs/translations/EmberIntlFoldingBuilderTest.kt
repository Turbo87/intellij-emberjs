package com.emberjs.translations

import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.indexing.FileBasedIndex
import java.nio.file.Paths

class EmberIntlFoldingBuilderTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String? {
        val resource = ClassLoader.getSystemResource("com/emberjs/translations/fixtures")
        return Paths.get(resource.toURI()).toAbsolutePath().toString()
    }

    fun doTest(templateName: String, fixtureName: String = "ember-intl") {
        // Load fixture files into the project
        myFixture.copyDirectoryToProject(fixtureName, "/")

        // Rebuild index now that the `package.json` file is copied over
        FileBasedIndex.getInstance().requestRebuild(EmberIntlIndex.NAME)

        PlatformTestUtil.dispatchAllEventsInIdeEventQueue()

        myFixture.testFoldingWithCollapseStatus(
                "$testDataPath/$fixtureName/app/templates/$templateName-expectation.hbs",
                "$testDataPath/$fixtureName/app/templates/$templateName.hbs")
    }

    fun testFolding() = doTest("folding-test")
    fun testUnknownTranslation() = doTest("missing-translation-folding-test")
    fun testPlaceholders() = doTest("placeholder-folding-test")
    fun testSubexpression() = doTest("sexpr-folding-test")

    fun testJson() = doTest("json", "ember-intl-json")
    fun testFoldingWithoutDependency() = doTest("folding-test", "no-dependencies")
    fun testBaseLocale() = doTest("base-locale-test", "ember-intl-with-base-locale")
}
