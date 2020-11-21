package com.emberjs.resolver

import com.emberjs.cli.EmberCliProjectConfigurator
import com.emberjs.utils.emberRoot
import com.emberjs.utils.isEmberAddonFolder
import com.emberjs.utils.isInRepoAddon
import com.emberjs.utils.parents
import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.frameworks.amd.JSModuleReference
import com.intellij.lang.javascript.frameworks.modules.JSExactFileReference
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils
import com.intellij.lang.javascript.psi.resolve.JSModuleReferenceContributor
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.impl.source.PsiFileImpl
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet
import org.jetbrains.annotations.NotNull
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
    override fun getCommonJSModuleReferences(unquotedRefText: String, host: PsiElement, offset: Int, provider: PsiReferenceProvider?): Array<out PsiReference> {
        return emptyArray()
    }

    override fun getAllReferences(unquotedRefText: String, host: PsiElement, offset: Int, provider: PsiReferenceProvider?): Array<out PsiReference> {
        // return early for relative imports
        if (unquotedRefText.startsWith('.')) {
            return emptyArray()
        }

        // e.g. `my-app/controllers/foo` -> `my-app`
        val packageName = unquotedRefText.substringBefore('/')

        // e.g. `my-app/controllers/foo` -> `controllers/foo`
        val importPath = unquotedRefText.removePrefix("$packageName/")

        // find root package folder of current file (ignoring package.json in in-repo-addons)
        val hostPackageRoot = host.containingFile.virtualFile?.parents
                ?.find { it.findChild("package.json") != null && !it.isInRepoAddon }
                ?: return emptyArray()

        val modules = if (getAppName(hostPackageRoot) == packageName) {
            // local import from this app/addon
            listOf(hostPackageRoot) + EmberCliProjectConfigurator.inRepoAddons(hostPackageRoot)
        } else {
            // check node_modules
            listOfNotNull(host.emberRoot?.findChild("node_modules")?.findChild(packageName))
        }

        /** Search the `/app` and `/addon` directories of the root and each in-repo-addon */
        val roots = modules
                .filter { it.isEmberAddonFolder }
                .flatMap { listOfNotNull(it.findChild("addon"), it.findChild("app"), it.findChild("addon-test-support")) }
                .map { JSExactFileReference(host, TextRange.create(offset, offset + packageName.length), listOf(it.path), null) }

        if (roots.isEmpty()) {
            return emptyArray()
        }
        val refs : FileReferenceSet
        val startInElement = offset + packageName.length + 1

        if (importPath == "") {
            return roots.toTypedArray()
        }

        try {
            refs = object : FileReferenceSet(importPath, host, startInElement, provider, false, true, DialectDetector.JAVASCRIPT_FILE_TYPES_ARRAY) {
                override fun createFileReference(range: TextRange, index: Int, text: String?): FileReference {
                    return object : JSModuleReference(text, index, range, this, null, true) {
                        override fun innerResolveInContext(referenceText: String, psiFileSystemItem: PsiFileSystemItem, resolveResults: MutableCollection<ResolveResult>, caseSensitive: Boolean) {
                            super.innerResolveInContext(referenceText, psiFileSystemItem, resolveResults, caseSensitive)

                            // don't suggest the current file, e.g. when navigating from /app to /addon
                            resolveResults.removeAll { it.element?.containingFile == host.containingFile }
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
        } catch (e: StringIndexOutOfBoundsException) {
            // TODO: this sometimes happens if startInElement is >= importPath.length but we don't exactly know why.
            println("Error in EmberModuleReferenceContributor for importPath: \"$importPath\" (starting at $startInElement). " +
                    "This is a known issue and can be ignored. See https://github.com/Turbo87/intellij-emberjs/issues/176")
            return arrayOf()
        }

        // filter out invalid references
        // reference default export if available, otherwise file
        return (roots + refs.allReferences)
                .filter { it.resolve() != null }
                .toTypedArray()
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
