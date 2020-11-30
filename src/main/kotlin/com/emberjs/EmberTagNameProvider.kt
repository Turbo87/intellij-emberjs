package com.emberjs

import com.emberjs.icons.EmberIconProvider
import com.emberjs.index.EmberNameIndex
import com.emberjs.resolver.EmberName
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.impl.source.html.HtmlFileImpl
import com.intellij.psi.search.ProjectScope
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.XmlTagNameProvider

class EmberTagNameProvider : XmlTagNameProvider {

    override fun addTagNameVariants(elements: MutableList<LookupElement>?, tag: XmlTag, prefix: String?) {
        if (elements == null) return

        val containingFile = tag.containingFile as? HtmlFileImpl ?: return
        val language = containingFile.contentElementType?.language ?: return
        if (language.id !== "Handlebars") return

        val project = tag.project
        val scope = ProjectScope.getAllScope(project)

        val componentMap = hashMapOf<String, LookupElement>()

        // Collect all components from the index
        EmberNameIndex.getFilteredKeys(scope) { it.type == "component" }

                // Filter out components that are not related to this project
                .filter { EmberNameIndex.hasContainingFiles(it, scope) }

                // Convert search results for LookupElements
            .map { Pair(it.storageKey, toLookupElement(it)) }
                .toMap(componentMap)

        // Collect all component templates from the index
        EmberNameIndex.getFilteredKeys(scope) { it.isComponentTemplate }

                // Filter out components that are not related to this project
                .filter { EmberNameIndex.hasContainingFiles(it, scope) }

                // Filter out components that are already in the map
            .filter { !componentMap.containsKey(it.storageKey) }

                // Convert search results for LookupElements
            .map { Pair(it.storageKey, toLookupElement(it)) }
                .toMap(componentMap)


        elements.addAll(componentMap.values)
    }
}


fun toLookupElement(name: EmberName, priority: Double = 90.0): LookupElement {
    val lookupElement = LookupElementBuilder
            .create(name.angleBracketsName)
            .withTypeText("component")
            .withIcon(EmberIconProvider.getIcon("component"))
            .withCaseSensitivity(true)
    return PrioritizedLookupElement.withPriority(lookupElement, priority)
}
