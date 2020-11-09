package com.emberjs

import com.dmarcotte.handlebars.parsing.HbTokenTypes
import com.dmarcotte.handlebars.psi.HbHash
import com.dmarcotte.handlebars.psi.HbParam
import com.dmarcotte.handlebars.psi.impl.HbOpenBlockMustacheImpl
import com.emberjs.icons.EmberIconProvider
import com.emberjs.index.EmberNameIndex
import com.emberjs.lookup.HbsInsertHandler
import com.emberjs.resolver.EmberName
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.Language
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.html.HtmlFileImpl
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.search.ProjectScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parents
import com.intellij.psi.util.parentsWithSelf
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.XmlTagNameProvider

class EmberTagNameProvider : XmlTagNameProvider {


    private fun resolve(anything: PsiElement?, path: MutableList<String>, elements: MutableList<LookupElement>) {
        var refElement: Any? = anything
        if (anything == null) {
            return
        }

        if (anything.references.firstOrNull() != null) {
            resolve(anything.references.first().resolve(), path, elements)
            return
        }

        if (anything.reference != null) {
            resolve(anything.reference?.resolve(), path, elements)
            return
        }

        if (refElement is HbParam) {
            if (refElement.children.find { it is HbParam }?.text == "hash") {
                val names = refElement.children.filter { it.elementType == HbTokenTypes.HASH }.map { it.children[0].text }
                elements.addAll(names.map {
                    val p = path.toMutableList()
                    p.add(it)
                    LookupElementBuilder.create(p.joinToString("."))
                })
                return
            }
            val ids = PsiTreeUtil.collectElements(refElement, {it.elementType == HbTokenTypes.ID && it !is LeafPsiElement } )
            if (ids.size == 1) {
                resolve(ids.first(), path, elements)
                return
            }
        }
        LookupElementBuilder.create(path.joinToString("."))
    }

    private fun fromLocalParams(tag: XmlTag, elements: MutableList<LookupElement>) {
        val angleBracketBlock: XmlTag? = tag.parents
                .find {
                    it is XmlTag && it.attributes.find { it.text.startsWith("|") } != null
                } as XmlTag?

        if (angleBracketBlock != null) {
            angleBracketBlock.attributes.filter { it.text.startsWith("|") }.forEach {
                val names = it.text.replace("|", "").split(" ")
                val references = it.references.toList()
                names.forEachIndexed { i, item ->
                    resolve(references[i].resolve(), mutableListOf(item), elements)
                }
            }
        }

        // find from named block yields  {{yield to='x'}}
        val angleComponent = tag.parents.find {
            it is XmlTag && it.descriptor is EmberXmlElementDescriptor
        } as XmlTag?
        if (angleComponent != null) {
            val data = (angleComponent.descriptor as EmberXmlElementDescriptor).getReferenceData()
            val tplYields = data.yields

            for (yieldRef in tplYields) {
                val yieldblock = yieldRef.yieldBlock
                val namedYields = yieldblock.children.filter { it is HbHash && it.hashName == "to"}.map { (it as HbHash).children.last().text.replace(Regex("\"|'"), "") }
                val names: List<String>

                // if the tag has already colon, then remove it from the lookup elements, otherwise intellij will
                // add it again and it wil turn into <::name
                if (tag.name.startsWith(":")) {
                    names = namedYields
                } else {
                    names = namedYields.map { ":$it" }
                }
                // needs prioritization to appear before common html tags
                elements.addAll(names.map { PrioritizedLookupElement.withPriority(LookupElementBuilder.create(it), 2.0) })
            }
        }


        // find mustache block |params|
        val hbsView = tag.containingFile.viewProvider.getPsi(Language.findLanguageByID("Handlebars")!!)
        val hbBlockRef = PsiTreeUtil.collectElements(hbsView, { it is HbOpenBlockMustacheImpl })
                .filter { it.children[0].text.contains(Regex("\\|.*\\|")) }
                .filter { block ->
                    val htmlContent = PsiTreeUtil.collectElements(block) { it.elementType == HbTokenTypes.CONTENT && it.text.contains(tag.name) }
                    val htmlParts = htmlContent.map { hbsView.findElementAt(it.textOffset) }
                    htmlParts.find { part -> PsiTreeUtil.collectElements(part) { it == tag }.isNotEmpty() } != null
                }
        hbBlockRef.forEach {
            val group = Regex("\\|(.*)\\|").find(it.text)
            val names = group?.groups?.toList()?.getOrNull(0)?.value?.split(" ") ?: emptyList()
            val refs = names.map { n -> it.children.find { it.elementType == HbTokenTypes.ID && it.text == n } }
                    .map { it?.references?.firstOrNull() }
            names.forEachIndexed { i, item ->
                resolve(refs[i]?.resolve(), mutableListOf(item), elements)
            }
        }
    }

    override fun addTagNameVariants(elements: MutableList<LookupElement>?, tag: XmlTag, prefix: String?) {
        if (elements == null) return
        fromLocalParams(tag, elements)
        if (tag.name.startsWith(":")) {
            return
        }

        elements.add(LookupElementBuilder.create("Textarea"))
        elements.add(LookupElementBuilder.create("Input"))
        elements.add(LookupElementBuilder.create("LinkTo"))


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

class PathKeyClass : Key<String>("PATH")
val PathKey = PathKeyClass()



fun toLookupElement(name: EmberName, priority: Double = 90.0): LookupElement {
    val lookupElement = LookupElementBuilder
            .create(name.tagName)
            .withTailText(" from ${name.importPath}")
            .withTypeText("component")
            .withIcon(EmberIconProvider.getIcon("component"))
            .withCaseSensitivity(true)
            .withInsertHandler(HbsInsertHandler())
    lookupElement.putUserData(PathKey, name.importPath)
    return PrioritizedLookupElement.withPriority(lookupElement, priority)
}
