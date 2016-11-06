package com.emberjs.intl

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import com.intellij.util.indexing.FileBasedIndex
import java.nio.file.Paths

class EmberIntlFoldingBuilderTest : LightPlatformCodeInsightFixtureTestCase() {

    override fun getTestDataPath(): String? {
        val resource = ClassLoader.getSystemResource("com/emberjs/intl/fixtures")
        return Paths.get(resource.toURI()).toAbsolutePath().toString()
    }

    override fun setUp() {
        super.setUp()

        // Load fixture files into the project
        myFixture.copyDirectoryToProject("fixture1", "/")

        // Rebuild index now that the `package.json` file is copied over
        FileBasedIndex.getInstance().requestRebuild(EmberIntlIndex.NAME)
    }

    fun doTest(templateName: String) = myFixture.testFoldingWithCollapseStatus(
            "$testDataPath/fixture1/app/templates/$templateName-expectation.hbs",
            "$testDataPath/fixture1/app/templates/$templateName.hbs")

    fun testFolding() = doTest("folding-test")
    fun testUnknownTranslation() = doTest("missing-translation-folding-test")
    fun testPlaceholders() = doTest("placeholder-folding-test")
    fun testSubexpression() = doTest("sexpr-folding-test")
}
