package com.emberjs.psi

import com.emberjs.resolver.EmberResolver
import com.emberjs.utils.emberModule
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult.createResults
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult

class EmberReference(element: JSLiteralExpression, val types: Iterable<String>) :
        PsiPolyVariantReferenceBase<JSLiteralExpression>(element, true) {

    val project = element.project

    override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
        val value = element.value
        if (value !is String)
            return arrayOf()

        return createResults(resolve(value.replace(".", "/")))
    }

    private fun resolve(value: String): Collection<PsiElement> {
        val module = element.emberModule ?: return listOf()

        val psiProject = PsiManager.getInstance(project)
        val contentRoots = ModuleRootManager.getInstance(module).contentRoots.asSequence()

        // Iterate over types that we are looking for (e.g. "model" and "adapter")
        return types.asSequence()

                // Additionally iterate over content roots of the Ember.js module
                .flatMap { type ->
                    contentRoots.flatMap {
                        val resolver = EmberResolver(it)

                        // Look for type with matching name in module content root
                        sequenceOf(value, value.removeSuffix("s"))
                                .map { resolver.resolve("$type:$it") }
                    }
                }

                // Remove duplicates
                .distinct()
                .filterNotNull()

                // Convert VirtualFile to PsiFile
                .map { psiProject.findFile(it) }
                .filterNotNull()
                .toList()
    }

    override fun getVariants(): Array<out Any> = arrayOf()

}
