package com.emberjs.resolver

import com.emberjs.EmberFileType
import com.emberjs.utils.parentEmberModule
import com.emberjs.utils.parentModule
import com.emberjs.utils.parents
import com.intellij.openapi.vfs.VfsUtilCore.isAncestor
import com.intellij.openapi.vfs.VirtualFile

data class EmberName(val type: String, val name: String) {

    val fullName by lazy { "$type:$name" }

    val displayName by lazy {
        if (type == "template" && name.startsWith("components/")) {
            "${name.removePrefix("components/").replace('/', '.')} component-template"
        } else {
            "${name.replace('/', '.')} $type"
        }
    }

    val isTest: Boolean = type.endsWith("-test")

    companion object {
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

            return fromPod(appFolder, file) ?:
                    fromPod(addonFolder, file) ?:
                    fromPodTest(unitTestsFolder, file) ?:
                    fromPodTest(integrationTestsFolder, file) ?:
                    fromClassic(appFolder, file) ?:
                    fromClassic(addonFolder, file) ?:
                    fromClassicTest(unitTestsFolder, file) ?:
                    fromClassicTest(integrationTestsFolder, file) ?:
                    fromAcceptanceTest(acceptanceTestsFolder, file)
        }

        fun fromClassic(appFolder: VirtualFile?, file: VirtualFile): EmberName? {
            appFolder ?: return null

            val typeFolder = file.parents.find { it.parent == appFolder } ?: return null

            return EmberFileType.FOLDER_NAMES[typeFolder.name]?.let { type ->

                val path = file.parents
                        .takeWhile { it != typeFolder }
                        .map { it.name }
                        .reversed()
                        .joinToString("/")

                val name = "$path/${file.nameWithoutExtension}".removePrefix("/")

                EmberName(type.name.toLowerCase(), name)
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
