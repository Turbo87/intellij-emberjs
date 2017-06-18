package com.emberjs.resolver

import com.emberjs.cli.EmberCliProjectConfigurator
import com.emberjs.utils.emberRoot
import com.emberjs.utils.isInRepoAddon
import com.emberjs.utils.parents
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.frameworks.amd.JSModuleReference
import com.intellij.lang.javascript.frameworks.modules.JSExactFileReference
import com.intellij.lang.javascript.psi.resolve.JSModuleReferenceContributor
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
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
class EmberModuleReferenceContributor : JSModuleReferenceContributor {
    override fun getAllReferences(unquotedRefText: String, host: PsiElement, offset: Int, provider: PsiReferenceProvider?): Array<out PsiReference> {
        return getCommonJSModuleReferences(unquotedRefText, host, offset, provider)
    }

    override fun getCommonJSModuleReferences(unquotedRefText: String, host: PsiElement, offset: Int, provider: PsiReferenceProvider?): Array<out PsiReference> {
        // return early for relative imports
        if (unquotedRefText.startsWith('.')) {
            return emptyArray()
        }

        // e.g. `my-app/controllers/foo` -> `my-app`
        val packageName = unquotedRefText.substringBefore('/')

        // e.g. `my-app/controllers/foo` -> `controllers/foo`
        val importPath = unquotedRefText.removePrefix("$packageName/")

        if (unquotedRefText == importPath) {
            // only for imports with slashes
            return emptyArray()
        }

        val appRoot = host.containingFile.virtualFile.parents
                .find { it.findChild("package.json") != null && !it.isInRepoAddon }
                ?: return emptyArray()

        val modules = when {
            getAppName(appRoot) == packageName ->
                // local import from this app/addon
                listOf(appRoot) + EmberCliProjectConfigurator.inRepoAddons(appRoot)

            else ->
                // check node_modules
                listOfNotNull(host.emberRoot?.findChild("node_modules")?.findChild(packageName))
        }

        /** Search the `/app` and `/addon` directories of the root and each in-repo-addon */
        val roots = modules
                .flatMap { listOfNotNull(it.findChild("app"), it.findChild("addon")) }
                .map { JSExactFileReference(host, TextRange.create(offset, offset + packageName.length), listOf(it.path), null) }

        val refs = object : FileReferenceSet(importPath, host, offset + packageName.length + 1, provider, false, true, DialectDetector.JAVASCRIPT_FILE_TYPES_ARRAY) {
            override fun createFileReference(range: TextRange, index: Int, text: String?): FileReference {
                return object : JSModuleReference(text, index, range, this, "file.js", true) {
                    override fun innerResolveInContext(referenceText: String, psiFileSystemItem: PsiFileSystemItem, resolveResults: MutableCollection<ResolveResult>?, b: Boolean) {
                        super.innerResolveInContext(referenceText, psiFileSystemItem, resolveResults, b)

                        // don't suggest the current file, e.g. when navigating from /app to /addon
                        resolveResults?.removeAll { it.element?.containingFile == host.containingFile }
                    }

                    override fun isAllowFolders() = false
                }
            }

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
    private fun getAppName(appRoot: VirtualFile): String? = getModulePrefix(appRoot) ?: getAddonName(appRoot)

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
