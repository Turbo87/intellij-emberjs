package com.emberjs.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilder
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.lang.javascript.psi.*
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtil.isJavaIdentifier

/**
 * Enables folding of `this.get('foo')` to `this.foo`.
 */
class EmberGetFoldingBuilder : FoldingBuilder {
    override fun buildFoldRegions(node: ASTNode, document: Document): Array<FoldingDescriptor> {
        val file = node.psi as? JSFile ?: return emptyArray()

        val descriptors = mutableListOf<FoldingDescriptor>()

        // visit all CallExpressions in the file
        file.acceptChildren(object : JSRecursiveElementVisitor() {
            override fun visitJSCallExpression(node: JSCallExpression) {
                super.visitJSCallExpression(node)

                val methodExpression = node.methodExpression as? JSReferenceExpression ?: return
                when (methodExpression.referenceName) {
                    "get" -> visitGetter(node)
                    "set" -> visitSetter(node)
                }
            }

            fun visitGetter(node: JSCallExpression) {
                val methodExpression = node.methodExpression as? JSReferenceExpression
                val getElement = methodExpression?.referenceNameElement ?: return

                // check that we only have a single argument
                val arguments = node.arguments
                if (arguments.count() != 1) return

                // check that the argument is a static string
                val firstArg = arguments[0] as? JSLiteralExpression ?: return
                if (!firstArg.isQuotedLiteral) return

                // check that the string is a valid key path
                val key = firstArg.value as String
                val isValidKey = key.split('.').all { isJavaIdentifier(it) }
                if (!isValidKey) return

                // save placeholder text on the PSI element
                node.putUserData(GET_SET_FOLDING_REPLACEMENT, key)

                // build text range only covering the `get('foo')` part of the CallExpression
                val textRange = TextRange(getElement.textRange.startOffset, node.textRange.endOffset)

                descriptors.add(FoldingDescriptor(node, textRange))
            }

            fun visitSetter(node: JSCallExpression) {
                val methodExpression = node.methodExpression as? JSReferenceExpression
                val setElement = methodExpression?.referenceNameElement ?: return

                // check that we have exactly two arguments
                val arguments = node.arguments
                if (arguments.count() != 2) return

                // check that the argument is a static string
                val firstArg = arguments[0] as? JSLiteralExpression ?: return
                if (!firstArg.isQuotedLiteral) return

                // check that the string is a valid key path
                val key = firstArg.value as String
                val isValidKey = key.split('.').all { isJavaIdentifier(it) }
                if (!isValidKey) return

                // save placeholder text on the PSI element
                node.putUserData(GET_SET_FOLDING_REPLACEMENT, "$key = ${arguments[1].text}")

                // build text range only covering the `set('foo', bar)` part of the CallExpression
                val textRange = TextRange(setElement.textRange.startOffset, node.textRange.endOffset)

                descriptors.add(FoldingDescriptor(node, textRange))
            }
        })

        return descriptors.toTypedArray()
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean = node.psi.getUserData(GET_SET_FOLDING_REPLACEMENT) != null

    override fun getPlaceholderText(node: ASTNode): String? = node.psi.getUserData(GET_SET_FOLDING_REPLACEMENT)

    companion object {
        private val GET_SET_FOLDING_REPLACEMENT = Key<String>("ember.get-set-folding-replacement")
    }
}
