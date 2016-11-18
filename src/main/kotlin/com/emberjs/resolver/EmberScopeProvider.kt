package com.emberjs.resolver

import com.emberjs.cli.EmberCliProjectConfigurator
import com.emberjs.utils.emberRoot
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.frameworks.amd.JSModuleReference
import com.intellij.lang.javascript.frameworks.modules.JSExactFileReference
import com.intellij.lang.javascript.psi.resolve.JSModuleReferenceContributor
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileSystemItem
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet
import java.util.regex.Pattern

/**
 * Resolves absolute imports from the ember application root, e.g.
 * ```
 * import FooController from 'my-app/controllers/foo'
 * ```
 *
 * Navigating to `FooController` will browse to `/app/controllers/foo.js`
 */
class EmberAppReferenceContributor : JSModuleReferenceContributor {
    override fun getAllReferences(unquotedRefText: String, host: PsiElement, offset: Int, provider: PsiReferenceProvider?): Array<out PsiReference> {
        return getCommonJSModuleReferences(unquotedRefText, host, offset, provider)
    }

    override fun getCommonJSModuleReferences(unquotedRefText: String, host: PsiElement, offset: Int, provider: PsiReferenceProvider?): Array<out PsiReference> {
        val appRoot = host.emberRoot ?: return emptyArray()
        val appName = getAppName(appRoot) ?: return emptyArray()

        val importPath = unquotedRefText.removePrefix(appName + "/")
        if (unquotedRefText == importPath) {
            // only match ember app imports
            return emptyArray()
        }

        /** Search the `/app` directories of the root and each in-repo-addon */
        val roots = listOf(appRoot)
            .plus(EmberCliProjectConfigurator.inRepoAddons(appRoot))
            .map { JSExactFileReference(host, TextRange.create(offset, offset + appName.length), listOf("${it.path}/app"), null) }

        val refs = object : FileReferenceSet(importPath, host, offset + appName.length + 1, provider, false, true, DialectDetector.JAVASCRIPT_FILE_TYPES_ARRAY) {
            override fun createFileReference(range: TextRange?, index: Int, text: String?): FileReference = JSModuleReference(text, index, range, this, "file.js", true)
            override fun computeDefaultContexts(): MutableCollection<PsiFileSystemItem> {
                return roots
                    .flatMap { it.multiResolve(false).asIterable() }
                    .map { it.element }
                    .filterIsInstance(PsiFileSystemItem::class.java)
                    .toMutableList()
            }
        }

        return (roots + refs.allReferences).toTypedArray()
    }

    override fun isApplicable(host: PsiElement): Boolean = DialectDetector.isES6(host)

    /** Detect the name of the ember application */
    private fun getAppName(appRoot: VirtualFile): String? {
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
}
