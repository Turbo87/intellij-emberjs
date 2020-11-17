package com.emberjs

import com.emberjs.icons.EmberIconProvider
import com.emberjs.index.EmberNameIndex
import com.emberjs.resolver.EmberName
import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.Key
import com.intellij.psi.impl.source.html.HtmlFileImpl
import com.intellij.psi.search.ProjectScope
import com.intellij.psi.xml.XmlTag
import com.intellij.util.containers.toArray
import com.intellij.xml.XmlTagNameProvider

class EmberTagNameProvider : XmlTagNameProvider {

    override fun addTagNameVariants(elements: MutableList<LookupElement>?, tag: XmlTag, prefix: String?) {
        if (elements == null) return
        if (prefix != null && !prefix.isEmpty()) return

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
            .map { Pair(it.path, toLookupElement(it)) }
            .toMap(componentMap)

        // Collect all component templates from the index
        EmberNameIndex.getFilteredKeys(scope) { it.isComponentTemplate }

            // Filter out components that are not related to this project
            .filter { EmberNameIndex.hasContainingFiles(it, scope) }

            // Filter out components that are already in the map
            .filter { !componentMap.containsKey(it.path) }

            // Convert search results for LookupElements
            .map { Pair(it.path, toLookupElement(it)) }
            .toMap(componentMap)


        elements.addAll(componentMap.values)
    }
}

class PathKeyClass : Key<String>("PATH")
val PathKey = PathKeyClass()

private class HbsInsertHandler : InsertHandler<LookupElement> {

    override fun handleInsert(context: InsertionContext, item: LookupElement) {
        // todo parse imports and merge
        val path = item.getUserData(PathKey)
        val fullName = item.lookupString.replace("::", "/")
        val name = fullName.split("/").last()
        val pattern = "\\{\\{\\s*import\\s+([\\w*\"']+[-,\\w*\\n'\" ]+)\\s+from\\s+['\"]([^'\"]+)['\"]\\s*\\}\\}"
        val matches = Regex(pattern).findAll(context.document.text)
        val importsSq = matches.map {
            it.groups.filterNotNull().map { it.value }.toMutableList().takeLast(2).toMutableList()
        }
        val imports = importsSq.asIterable().toMutableList()
        val m = imports.indexOfFirst { it.last() == path }

        if (m != -1) {
            val groups = imports.elementAt(m)
            if (groups.first().contains(name)) return
            val g = groups.elementAt(0).replace("'", "").replace("\"", "")
            groups.removeAt(0)
            groups[0] = "'$g,$name'"
        } else {
            val l = arrayOf(name, path!!)
            imports.add(l.toMutableList())
        }
        var text = context.document.text
        text = text.replace(Regex("$pattern.*\n"), "")
        for (imp in imports.reversed()) {
            val l = arrayOf("{{import", imp.elementAt(0), "from '${imp.elementAt(1)}'}}")
            text = l.joinToString(" ") + "\n" + text
        }
        context.document.setText(text)
        context.commitDocument()
    }

}

fun toLookupElement(name: EmberName, priority: Double = 90.0): LookupElement {
    val lookupElement = LookupElementBuilder
            .create(name.tagName)
            .withTailText(" from ${name.path}")
            .withTypeText("component")
            .withIcon(EmberIconProvider.getIcon("component"))
            .withCaseSensitivity(true)
            .withInsertHandler(HbsInsertHandler())
    lookupElement.putUserData(PathKey, name.path)
    return PrioritizedLookupElement.withPriority(lookupElement, priority)
}
