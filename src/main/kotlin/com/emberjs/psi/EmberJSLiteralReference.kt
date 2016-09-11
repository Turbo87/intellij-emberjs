package com.emberjs.psi

import com.emberjs.icons.EmberIconProvider
import com.emberjs.icons.EmberIcons
import com.emberjs.index.EmberNameIndex
import com.emberjs.lookup.EmberLookupElementBuilder
import com.emberjs.project.EmberProjectComponent
import com.emberjs.resolver.EmberName
import com.emberjs.resolver.EmberResolver
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult.createResults
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.CommonProcessors
import com.intellij.util.indexing.FileBasedIndex

class EmberJSLiteralReference(element: JSLiteralExpression, val types: Iterable<String>) :
        PsiPolyVariantReferenceBase<JSLiteralExpression>(element, true) {

    val project = element.project

    override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
        val value = element.value
        if (value !is String)
            return emptyArray()

        return createResults(resolve(value.replace(".", "/")))
    }

    private fun resolve(value: String): Collection<PsiElement> {
        val rootsSeq = EmberProjectComponent.getInstance(project)?.roots?.asSequence() ?: return listOf()

        val psiManager = PsiManager.getInstance(project)

        // Iterate over types that we are looking for (e.g. "model" and "adapter")
        return types.asSequence()

                // Additionally iterate over Ember.js roots of the project
                .flatMap { type ->
                    rootsSeq.flatMap {
                        val resolver = EmberResolver(it)

                        // Look for type with matching name in root folder
                        sequenceOf(value, value.removeSuffix("s"))
                                .map { resolver.resolve("$type:$it") }
                                .distinct()
                    }
                }
                .filterNotNull()

                // Convert VirtualFile to PsiFile
                .map { psiManager.findFile(it) }
                .filterNotNull()
                .toList()
    }

    override fun getVariants(): Array<out Any> {
        val scope = GlobalSearchScope.projectScope(project)

        val keys = arrayListOf<EmberName>()
        val processor = CommonProcessors.CollectProcessor(keys)

        FileBasedIndex.getInstance().processAllKeys(EmberNameIndex.NAME, processor, scope, null)

        return keys.filter { it.type == types.firstOrNull() }
                .map { EmberLookupElementBuilder.create(it) }
                .toTypedArray()
    }
}
