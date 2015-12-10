package com.emberjs.patterns

import com.emberjs.project.EmberProjectComponent
import com.emberjs.resolver.EmberName
import com.emberjs.utils.originalVirtualFile
import com.intellij.lang.javascript.patterns.JSElementPattern
import com.intellij.lang.javascript.patterns.JSPatterns.jsArgument
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.patterns.InitialPatternCondition
import com.intellij.patterns.ObjectPattern
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext

object EmberPatterns {

    fun jsQuotedLiteralExpression(): JSElementPattern.Capture<JSLiteralExpression> {
        return JSElementPattern.Capture(object : InitialPatternCondition<JSLiteralExpression>(
                JSLiteralExpression::class.java) {
            override fun accepts(o: Any?, context: ProcessingContext?): Boolean {
                return o is JSLiteralExpression && o.isQuotedLiteral
            }
        })
    }

    fun jsQuotedArgument(functionName: String, index: Int) =
            jsQuotedLiteralExpression().and(jsArgument(functionName, index))

    fun inEmberFile(vararg types: String): ObjectPattern.Capture<PsiElement> {
        return ObjectPattern.Capture(object : InitialPatternCondition<PsiElement>(PsiElement::class.java) {
            override fun accepts(o: Any?, context: ProcessingContext?): Boolean {
                if (o !is PsiElement)
                    return false

                val roots = EmberProjectComponent.getInstance(o.project)?.roots ?: return false
                val file = o.originalVirtualFile

                return roots.filter { VfsUtil.isAncestor(it, file, true) }
                        .map { EmberName.from(it, file) }
                        .filterNotNull()
                        .any { it.type in types }
            }
        })
    }
}
