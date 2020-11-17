package com.emberjs.resolver

import com.emberjs.EmberFileType
import com.emberjs.utils.parentEmberModule
import com.emberjs.utils.parents
import com.intellij.openapi.vfs.VfsUtilCore.isAncestor
import com.intellij.openapi.vfs.VirtualFile

data class EmberName(val type: String, val name: String) {

    val fullName by lazy { "$type:$name" }

    val displayName by lazy {
        if (isComponentStyles) {
            "${name.removePrefix("components/").replace('/', '.')} component-styles"
        } else if (isComponentTemplate) {
            "${name.removePrefix("components/").replace('/', '.')} component-template"
        } else {
            "${name.replace('/', '.')} $type"
        }
    }

    // adapted from https://github.com/ember-codemods/ember-angle-brackets-codemod/blob/v3.0.1/transforms/angle-brackets/transform.js#L36-L62
    val angleBracketsName by lazy {
        assert(type == "component" || isComponentTemplate)

        val baseName = if (isComponentTemplate) name.removePrefix("components/") else name

        baseName.replace(SIMPLE_DASHERIZE_REGEXP) {
            assert(it.range.first - it.range.last == 0)

            if (it.value == "/") return@replace "::"

            if (it.range.first == 0 || !ALPHA.matches(baseName.subSequence(it.range.start - 1, it.range.start))) {
                return@replace it.value.toUpperCase()
            }

            if (it.value == "-") "" else it.value.toLowerCase()
        }
    }


    val path by lazy {
        assert(type == "component" || isComponentTemplate)

        name
    }

    val tagName by lazy {
        assert(type == "component" || isComponentTemplate)

        val baseName = name.split('/').last()
        baseName.replace(SIMPLE_DASHERIZE_REGEXP) {
            assert(it.range.first - it.range.last == 0)

            if (it.range.first == 0 || !ALPHA.matches(baseName.subSequence(it.range.start - 1, it.range.start))) {
                return@replace it.value.toUpperCase()
            }

            if (it.value == "-") "" else it.value.toLowerCase()
        }
    }

    val isTest: Boolean = type.endsWith("-test")
    val isComponentStyles = type == "styles" && name.startsWith("components/")
    val isComponentTemplate = type == "template" && name.startsWith("components/")

    companion object {
        private val SIMPLE_DASHERIZE_REGEXP = Regex("[a-z]|/|-")
        private val ALPHA = Regex("[A-Za-z0-9]")

        fun from(fullName: String): EmberName? {
            val parts = fullName.split(":")
            return when {
                parts.count() == 2 -> EmberName(parts[0], parts[1])
                else -> null
            }
        }

        fun from(file: VirtualFile) = file.parentEmberModule?.let { from(it, file) }

        fun from(root: VirtualFile, file: VirtualFile): EmberName? {
            val appFolder = root.findChild("app")
            val addonFolder = root.findChild("addon")
            val testsFolder = root.findChild("tests")
            val unitTestsFolder = testsFolder?.findChild("unit")
            val integrationTestsFolder = testsFolder?.findChild("integration")
            val acceptanceTestsFolder = testsFolder?.findChild("acceptance")
            val dummyAppFolder = testsFolder?.findFileByRelativePath("dummy/app")

            return fromPod(appFolder, file) ?: fromPod(addonFolder, file) ?: fromPodTest(unitTestsFolder, file)
            ?: fromPodTest(integrationTestsFolder, file) ?: fromClassic(appFolder, file)
            ?: fromClassic(addonFolder, file) ?: fromClassic(dummyAppFolder, file)
            ?: fromClassicTest(unitTestsFolder, file) ?: fromClassicTest(integrationTestsFolder, file)
            ?: fromAcceptanceTest(acceptanceTestsFolder, file)
        }

        fun fromClassic(appFolder: VirtualFile?, file: VirtualFile): EmberName? {
            appFolder ?: return null

            val typeFolder = file.parents.find { it.parent == appFolder } ?: return null

            if (typeFolder.name == "styles") {
                val path = file.parents
                        .takeWhile { it != typeFolder }
                        .map { it.name }
                        .reversed()
                        .joinToString("/")

                val name = "$path/${file.nameWithoutExtension}".removePrefix("/")

                return EmberName("styles", name.removeSuffix(".module"))
            }

            return EmberFileType.FOLDER_NAMES[typeFolder.name]?.let { type ->

                // e.g. private helpers for component
                if (file.path.contains("/helpers")) {
                    return null
                }
                var path = ""
                if (file.path.contains("node_modules")) {
                    path = file.parents
                            .takeWhile { it.name != "node_modules" }
                            .map { it.name }
                            .reversed()
                            .joinToString("/")
                } else {
                    path = file.parents
                            .takeWhile { it != file.parentEmberModule }
                            .map { it.name }
                            .reversed()
                            .joinToString("/")
                }

                path = path.replace("/app/", "/")
                path = path.replace("/addon/", "/")

                var name = file.nameWithoutExtension

                // detect flat and nested component layout (where hbs file lies in the components/ folder)
                if (type == EmberFileType.COMPONENT) {
                    // if component.d.ts exists
                    if (file.name.endsWith("component") || file.name.endsWith("component.d.ts")) {
                        name = ""
                    }
                    if (file.name.endsWith("template.hbs")) {
                        name = ""
                    }
                    if (file.extension == "css" || file.extension == "scss") {
                        return EmberName("styles", "components/${name.removeSuffix(".module")}")
                    }
                }
                var importPath = path + "/" + name
                if (importPath.startsWith("app/")) {
                    importPath = importPath.replace("app/", "~/")
                }
                EmberName(type.name.toLowerCase(), importPath.removeSuffix("/"))
            }
        }

        fun fromClassicTest(testsFolder: VirtualFile?, file: VirtualFile): EmberName? {
            testsFolder ?: return null

            val typeFolder = file.parents.find { it.parent == testsFolder } ?: return null

            val testSuffix = when (testsFolder.name) {
                "unit" -> "-test"
                else -> "-${testsFolder.name}-test"
            }

            return EmberFileType.FOLDER_NAMES[typeFolder.name]?.let { type ->

                val path = file.parents
                        .takeWhile { it != typeFolder }
                        .map { it.name }
                        .reversed()
                        .joinToString("/")

                val name = "$path/${file.nameWithoutExtension.removeSuffix("-test")}".removePrefix("/")

                EmberName("${type.name.toLowerCase()}$testSuffix", name)
            }
        }

        fun fromAcceptanceTest(testsFolder: VirtualFile?, file: VirtualFile): EmberName? {
            testsFolder ?: return null

            if (!isAncestor(testsFolder, file, true))
                return null

            val path = file.parents
                    .takeWhile { it != testsFolder }
                    .map { it.name }
                    .reversed()
                    .joinToString("/")

            val name = "$path/${file.nameWithoutExtension.removeSuffix("-test")}".removePrefix("/")

            return EmberName("acceptance-test", name)
        }

        fun fromPod(appFolder: VirtualFile?, file: VirtualFile): EmberName? {
            appFolder ?: return null

            if (!isAncestor(appFolder, file, true))
                return null

            if (file.nameWithoutExtension.removeSuffix(".module") == "styles") {
                return file.parents.takeWhile { it != appFolder }
                        .map { it.name }
                        .reversed()
                        .joinToString("/")
                        .let { EmberName("styles", it) }
            }

            return EmberFileType.FILE_NAMES[file.name]?.let { type ->

                file.parents.takeWhile { it != appFolder }
                        .map { it.name }
                        .reversed()
                        .joinToString("/")
                        .let {
                            when (type) {
                                EmberFileType.COMPONENT -> it.removePrefix("components/")
                                else -> it
                            }
                        }
                        .let { EmberName(type.name.toLowerCase(), it) }
            }
        }

        fun fromPodTest(testsFolder: VirtualFile?, file: VirtualFile): EmberName? {
            testsFolder ?: return null

            if (!isAncestor(testsFolder, file, true))
                return null

            val fileName = "${file.nameWithoutExtension.removeSuffix("-test")}.${file.extension}"

            val testSuffix = when (testsFolder.name) {
                "unit" -> "-test"
                else -> "-${testsFolder.name}-test"
            }

            return EmberFileType.FILE_NAMES[fileName]?.let { type ->

                val name = file.parents
                        .takeWhile { it != testsFolder }
                        .map { it.name }
                        .reversed()
                        .joinToString("/")
                        .let {
                            when (type) {
                                EmberFileType.COMPONENT -> it.removePrefix("components/")
                                else -> it
                            }
                        }

                EmberName("${type.name.toLowerCase()}$testSuffix", name)
            }
        }
    }
}
