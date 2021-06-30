package com.emberjs.resolver

import com.emberjs.Ember
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

        val modules = if (Ember.getAppName(hostPackageRoot) == packageName) {
            // local import from this app/addon
            listOf(hostPackageRoot) + EmberCliProjectConfigurator.inRepoAddons(hostPackageRoot)
        } else {
            // check node_modules
            listOfNotNull(host.emberRoot?.findChild("node_modules")?.findChild(packageName))
        }

        /** Search the `/app` and `/addon` directories of the root and each in-repo-addon */
        val roots = modules
                .flatMap { listOfNotNull(it.findChild("app"), it.findChild("addon"), it.findChild("addon-test-support")) }
                .map { JSExactFileReference(host, TextRange.create(offset, offset + packageName.length), listOf(it.path), null) }

        val refs : FileReferenceSet
        val startInElement = offset + packageName.length + 1

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

        return (roots + refs.allReferences).toTypedArray()
    }

    override fun isApplicable(host: PsiElement): Boolean = DialectDetector.isES6(host)
}
