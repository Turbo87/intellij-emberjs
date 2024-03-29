package com.emberjs.translations

import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.indexing.FileBasedIndex
import java.nio.file.Paths

class EmberI18nFoldingBuilderTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String? {
        val resource = ClassLoader.getSystemResource("com/emberjs/translations/fixtures")
        return Paths.get(resource.toURI()).toAbsolutePath().toString()
    }

    fun doTest(templateName: String, fixtureName: String = "ember-i18n") {
        // Load fixture files into the project
        myFixture.copyDirectoryToProject(fixtureName, "/")

        // Rebuild index now that the `package.json` file is copied over
        FileBasedIndex.getInstance().requestRebuild(EmberI18nIndex.NAME)

        PlatformTestUtil.dispatchAllEventsInIdeEventQueue()

        myFixture.testFoldingWithCollapseStatus(
                "$testDataPath/$fixtureName/app/templates/$templateName-expectation.hbs",
                "$testDataPath/$fixtureName/app/templates/$templateName.hbs")
    }

    fun testFolding() = doTest("application")
    fun testFoldingWithoutDependency() = doTest("folding-test", "no-dependencies")
}
