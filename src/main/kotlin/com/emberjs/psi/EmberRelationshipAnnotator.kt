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

class EmberRelationshipAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is JSProperty) return

        val key = element.name ?: return
        val identifier = element.nameIdentifier ?: return
        val value = element.value?.text ?: return

        // Extract e.g. "model:foobar" from property key and optional injection name
        val name = extractRelationshipModel(key, value) ?: return
        val icon = EmberIconProvider.getIcon(name.type) ?: return

        // Find corresponding PsiFiles
        val project = element.project
        val scope = ProjectScope.getAllScope(project)
        val psiManager = PsiManager.getInstance(project)

        val referencedFiles = EmberNameIndex.getFilteredKeys(scope) { it == name }
                .flatMap { EmberNameIndex.getContainingFiles(it, scope) }
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
        val CAPTURED_STRING_VAL = """(?:'([^']*)'|"([^"]*)")"""

        val BELONGS_TO = """(?:DS\.belongsTo|belongsTo)"""
        val HAS_MANY = """(?:DS\.hasMany|hasMany)"""

        val ANY_OBJECT = """\{[^}]*}"""
        val ANY_VARIABLE = """[\w]+"""

        val BELONGS_TO_RE = """$BELONGS_TO\((?:$CAPTURED_STRING_VAL\s*(?:,\s*(?:$ANY_OBJECT|$ANY_VARIABLE))?)?\)""".toRegex()
        val HAS_MANY_RE = """$HAS_MANY\((?:$CAPTURED_STRING_VAL\s*(?:,\s*(?:$ANY_OBJECT|$ANY_VARIABLE))?)?\)""".toRegex()
        val MODEL = "model"

        fun extractRelationshipModel(key: String, value: String): EmberName? {
            val (modelName) = BELONGS_TO_RE.matchEntire(value)?.destructured ?:
                    HAS_MANY_RE.matchEntire(value)?.destructured ?:
                    return null

            val name = when {
                modelName.isNotEmpty() -> modelName
                else -> key
            }

            return EmberName(MODEL, name.dasherize())
        }
    }
}
