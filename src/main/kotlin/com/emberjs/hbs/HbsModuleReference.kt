package com.emberjs.hbs

import com.emberjs.index.EmberNameIndex
import com.emberjs.lookup.EmberLookupElementBuilder
import com.emberjs.resolver.JsOrFileReference
import com.emberjs.resolver.EmberName
import com.emberjs.utils.EmberUtils
import com.intellij.lang.Language
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.PsiElementResolveResult.createResults
import com.intellij.psi.search.ProjectScope

open class HbsModuleReference(element: PsiElement, val moduleType: String) :
        PsiPolyVariantReferenceBase<PsiElement>(element, TextRange(0, element.textLength), true) {

    val project = element.project
    private val scope = ProjectScope.getAllScope(project)
    private val internalHelpersFile = PsiFileFactory.getInstance(project).createFileFromText("intellij-emberjs/internal/helpers-stub", Language.findLanguageByID("TypeScript")!!, this::class.java.getResource("/com/emberjs/external/ember-helpers.ts").readText())
    private val internalModifiersFile = PsiFileFactory.getInstance(project).createFileFromText("intellij-emberjs/internal/modifiers-stub", Language.findLanguageByID("TypeScript")!!, this::class.java.getResource("/com/emberjs/external/ember-modifiers.ts").readText())
    private val internalComponentsFile = PsiFileFactory.getInstance(project).createFileFromText("intellij-emberjs/internal/components-stub", Language.findLanguageByID("TypeScript")!!, this::class.java.getResource("/com/emberjs/external/ember-components.ts").readText())

    private val internalHelpers = EmberUtils.resolveDefaultExport(internalHelpersFile) as JSObjectLiteralExpression
    private val internalModifiers = EmberUtils.resolveDefaultExport(internalModifiersFile) as JSObjectLiteralExpression
    protected val internalComponents = EmberUtils.resolveDefaultExport(internalComponentsFile) as JSObjectLiteralExpression

    private val psiManager: PsiManager by lazy { PsiManager.getInstance(project) }

    open fun matches(module: EmberName) =
            module.type == moduleType && module.name == value

    override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
        // Collect all components from the index

        if (moduleType == "helper") {
            if (internalHelpers.properties.map { it.name }.contains(element.text)) {
                val prop = internalHelpers.properties.find { it.name == element.text }
                return createResults((prop?.jsType?.sourceElement as JSReferenceExpression).resolve())
            }
        }

        if (moduleType == "modifier") {
            if (internalModifiers.properties.map { it.name }.contains(element.text)) {
                val prop = internalModifiers.properties.find { it.name == element.text }
                return createResults((prop?.jsType?.sourceElement as JSReferenceExpression).resolve())
            }
        }

        return EmberNameIndex.getFilteredKeys(scope) { matches(it) }

                // Filter out components that are not related to this project
                .flatMap { EmberNameIndex.getContainingFiles(it, scope) }

                // Convert search results for LookupElements
                .map { psiManager.findFile(it) }
                .filterNotNull()
                .map { JsOrFileReference(it).resolve() }
                .take(1)
                .let(::createResults)
    }

    override fun getVariants(): Array<out Any?> {

        // Collect all components from the index
        return EmberNameIndex.getFilteredKeys(scope) { it.type == moduleType }

                // Filter out components that are not related to this project
                .filter { EmberNameIndex.hasContainingFiles(it, scope) }

                // Convert search results for LookupElements
                .map { EmberLookupElementBuilder.create(it, dots = false) }
                .toTypedArray()
    }
}
