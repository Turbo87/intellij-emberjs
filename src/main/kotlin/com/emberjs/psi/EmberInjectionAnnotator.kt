package com.emberjs.psi

import com.emberjs.icons.EmberIconProvider
import com.emberjs.index.EmberNameIndex
import com.emberjs.resolver.EmberName
import com.emberjs.utils.dasherize
import com.intellij.codeHighlighting.Pass
import com.intellij.codeInsight.daemon.DefaultGutterIconNavigationHandler
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.ProjectScope

class EmberInjectionAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is JSProperty) return

        val key = element.name ?: return
        val identifier = element.nameIdentifier ?: return
        val value = element.value?.text ?: return

        // Extract e.g. "service:foobar" from property key and optional injection name
        val name = extractInjectionName(key, value) ?: return
        val icon = EmberIconProvider.getIcon(name.type) ?: return

        // Find corresponding PsiFiles
        val project = element.project
        val scope = ProjectScope.getAllScope(project)
        val psiManager = PsiManager.getInstance(project)

        val referencedFiles = EmberNameIndex.getFilteredFiles(scope) { it == name }
                .map { psiManager.findFile(it) }
                .filterNotNull()

        if (referencedFiles.isEmpty()) return

        // Create an annotation with a gutter icon renderer
        holder.createInfoAnnotation(identifier, null).apply {
            val navHandler = DefaultGutterIconNavigationHandler<PsiElement>(referencedFiles, name.displayName)
            val lmi = LineMarkerInfo(identifier, identifier.textRange, icon, Pass.LINE_MARKERS,
                    null, navHandler, GutterIconRenderer.Alignment.CENTER)

            gutterIconRenderer = LineMarkerInfo.LineMarkerGutterIconRenderer(lmi)
        }
    }

    companion object {
        val VALID_INJECTIONS = arrayOf("controller", "service")
        val INJECTION_RE = """(?:Ember\.inject\.|inject\.)?(\w+)\((?:'([^']*)'|"([^"]*)")?\)""".toRegex()

        fun extractInjectionName(key: String, value: String): EmberName? {
            val (type, name1, name2) = INJECTION_RE.matchEntire(value)?.destructured ?: return null

            if (type !in VALID_INJECTIONS) return null

            val name = when {
                name1.isNotEmpty() -> name1
                name2.isNotEmpty() -> name2
                else -> key
            }

            return EmberName(type, name.dasherize())
        }
    }
}
