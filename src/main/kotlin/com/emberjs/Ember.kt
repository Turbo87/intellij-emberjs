package com.emberjs

import com.emberjs.utils.isEmberFolder
import com.emberjs.utils.parents
import com.intellij.openapi.vfs.VirtualFile
import java.util.regex.Pattern

object Ember {
    /**
     * Traverses the `file` parents until it finds root folder of the Ember.js project.
     *
     * This does not stop for in-repo-addon roots.
     */
    fun findProjectFolder(file: VirtualFile) = sequenceOf(file).plus(file.parents).firstOrNull { it.isEmberFolder }

    fun findEnvironmentConfigFile(file: VirtualFile) =
            Ember.findProjectFolder(file)?.findFileByRelativePath("config/environment.js")


    /** Detect the name of the ember application */
    fun getAppName(appRoot: VirtualFile): String? = getModulePrefix(appRoot) ?: getAddonName(appRoot)

    private fun getModulePrefix(appRoot: VirtualFile): String? {
        val env = appRoot.findFileByRelativePath("config/environment.js") ?: return null
        return env.inputStream.use { stream ->
            stream.reader().useLines { lines ->
                lines.mapNotNull { line ->
                    val matcher = ModulePrefixPattern.matcher(line)
                    if (matcher.find()) matcher.group(1) else null
                }.firstOrNull()
            }
        }
    }

    /** Captures `my-app` from the string `modulePrefix: 'my-app'` */
    private val ModulePrefixPattern = Pattern.compile("modulePrefix:\\s*['\"](.+?)['\"]")

    private fun getAddonName(appRoot: VirtualFile): String? {
        val index = appRoot.findFileByRelativePath("index.js") ?: return null
        return index.inputStream.use { stream ->
            stream.reader().useLines { lines ->
                lines.mapNotNull { line ->
                    val matcher = NamePattern.matcher(line)
                    if (matcher.find()) matcher.group(1) else null
                }.firstOrNull()
            }
        }
    }

    /** Captures `my-app` from the string `name: 'my-app'` */
    private val NamePattern = Pattern.compile("name:\\s*['\"](.+?)['\"]")
}

