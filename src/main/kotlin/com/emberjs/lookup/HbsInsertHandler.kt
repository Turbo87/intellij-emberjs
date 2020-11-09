package com.emberjs.lookup

import com.emberjs.PathKey
import com.emberjs.hbs.HbsLocalReference
import com.emberjs.utils.originalVirtualFile
import com.emberjs.utils.parentModule
import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.psi.util.PsiTreeUtil

class HbsInsertHandler : InsertHandler<LookupElement> {

    override fun handleInsert(context: InsertionContext, item: LookupElement) {
        if (PsiTreeUtil.collectElements(item.psiElement) { it.references.find { it is HbsLocalReference } != null }.size > 0) {
            return
        }
        if (context.project.projectFile?.parentModule?.findChild("node_modules")?.findChild("ember-template-imports") == null) {
            return
        }
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
