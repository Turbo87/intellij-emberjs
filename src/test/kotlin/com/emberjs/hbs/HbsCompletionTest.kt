package com.emberjs.hbs

import com.dmarcotte.handlebars.file.HbFileType
import com.dmarcotte.handlebars.parsing.HbTokenTypes
import com.dmarcotte.handlebars.psi.HbParam
import com.dmarcotte.handlebars.psi.impl.HbHashImpl
import com.dmarcotte.handlebars.psi.impl.HbPathImpl
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.lang.ecmascript6.psi.JSClassExpression
import com.intellij.lang.javascript.psi.JSField
import com.intellij.lang.javascript.psi.ecma6.TypeScriptPropertySignature
import com.intellij.project.stateStore
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parentsWithSelf
import com.intellij.refactoring.suggested.endOffset
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.jetbrains.rd.util.assert
import kotlin.test.assert

class HbsCompletionTest : BasePlatformTestCase() {
    fun testLocalFromMustach() {
        val hbs = """
            {{#each this.items as |item index|}}
                {{i}}
                {{it}}
                {{th}}
                {{ind}}
                {{in-helper (fn it)}}
            {{/each}}
        """.trimIndent()
        myFixture.configureByText(HbFileType.INSTANCE, hbs)
        val element = PsiTreeUtil.collectElements(myFixture.file, { it.elementType == HbTokenTypes.ID })
        val resolvedItem = element.find { it.text == "i" }
        val resolvedItemA = element.find { it.text == "it" }
        val resolvedItemThis = element.find { it.text == "th" }
        val resolvedIndex = element.find { it.text == "ind" }
        val resolvedItemInFn = element.find { it.text == "it" }
        assert(resolvedItem != null)

        myFixture.editor.caretModel.moveToOffset(resolvedItem!!.endOffset)
        myFixture.complete(CompletionType.BASIC)
        var completions = myFixture.lookupElementStrings!!
        assert(completions.containsAll(listOf("if", "in-element", "input", "item", "index", "action", "link-to", "yield")))

        myFixture.editor.caretModel.moveToOffset(resolvedItemA!!.endOffset)
        myFixture.complete(CompletionType.BASIC)
        assert(myFixture.lookupElementStrings == null && resolvedItemA.text == "item")

        myFixture.editor.caretModel.moveToOffset(resolvedItemThis!!.endOffset)
        myFixture.complete(CompletionType.BASIC)
        assert(myFixture.lookupElementStrings == null && resolvedItemThis.text == "this")

        myFixture.editor.caretModel.moveToOffset(resolvedIndex!!.endOffset)
        myFixture.complete(CompletionType.BASIC)
        assert(myFixture.lookupElementStrings == null && resolvedIndex.text == "index")

        myFixture.editor.caretModel.moveToOffset(resolvedItemInFn!!.endOffset)
        myFixture.complete(CompletionType.BASIC)
        assert(myFixture.lookupElementStrings == null && resolvedItemInFn.text == "item")
    }

    fun testLocalFromAnglebracket() {
        val hbs = """
            <MyComponent as |item index|>
                {{i}}
                {{it}}
                {{th}}
                {{ind}}
                {{in-helper (fn it)}}
            </MyComponent>
        """.trimIndent()
        myFixture.configureByText(HbFileType.INSTANCE, hbs)
        val element = PsiTreeUtil.collectElements(myFixture.file, { it.elementType == HbTokenTypes.ID })
        val resolvedItem = element.find { it.text == "i" }
        val resolvedItemA = element.find { it.text == "it" }
        val resolvedItemThis = element.find { it.text == "th" }
        val resolvedIndex = element.find { it.text == "ind" }
        val resolvedItemInFn = element.find { it.text == "it" }
        assert(resolvedItem != null)

            myFixture.editor.caretModel.moveToOffset(resolvedItem!!.endOffset)
            myFixture.complete(CompletionType.BASIC)
            var completions = myFixture.lookupElementStrings!!
            assert(completions.containsAll(listOf("item", "index")))

            myFixture.editor.caretModel.moveToOffset(resolvedItemA!!.endOffset)
            myFixture.complete(CompletionType.BASIC)
            assert(myFixture.lookupElementStrings == null && resolvedItemA.text == "item")

            myFixture.editor.caretModel.moveToOffset(resolvedItemThis!!.endOffset)
            myFixture.complete(CompletionType.BASIC)
            assert(myFixture.lookupElementStrings == null && resolvedItemThis.text == "this")

            myFixture.editor.caretModel.moveToOffset(resolvedIndex!!.endOffset)
            myFixture.complete(CompletionType.BASIC)
            assert(myFixture.lookupElementStrings == null && resolvedIndex.text == "index")

            myFixture.editor.caretModel.moveToOffset(resolvedItemInFn!!.endOffset)
            myFixture.complete(CompletionType.BASIC)
            assert(myFixture.lookupElementStrings == null && resolvedItemInFn.text == "item")
    }

    fun testToTsFile() {
        val hbs = """
            {{this.}}
            {{this.x.}}
            {{this.y.}}
            {{x}}
            {{in-helper (fn this.)}}
        """.trimIndent()
        val ts = """
            export default class extends Component {
                @tracked a;
                x: { b: string };
                /**
                 * @type {{b: string}}
                 */
                y;
            }
        """.trimIndent()
        myFixture.addFileToProject("component.ts", ts)
        myFixture.configureByText("template.hbs", hbs)
        val element = PsiTreeUtil.collectElements(myFixture.file, { it.elementType == HbTokenTypes.ID })
        val resolvedA = element.find { it.parent.text == "this." }!!.nextSibling.nextSibling
        val resolvedXB = element.find { it.parent.text == "this.x" }!!.parent.parent.nextSibling
        val resolvedYB = element.find { it.parent.text == "this.y" }!!.parent.parent.nextSibling
        val notResolvedX = element.find { it.parentsWithSelf.find { it is HbPathImpl }?.text == "x" }!!
        val resolvedXInHelper = element.findLast { it.parent.text == "this." }!!.nextSibling.nextSibling

        myFixture.editor.caretModel.moveToOffset(resolvedA.endOffset)
        myFixture.complete(CompletionType.BASIC)
        var completions = myFixture.lookupElementStrings!!
        assert(completions.containsAll(listOf("x", "y", "a")))

        myFixture.editor.caretModel.moveToOffset(resolvedXB.endOffset)
        myFixture.complete(CompletionType.BASIC)
        completions = myFixture.lookupElementStrings!!
        assert(completions.containsAll(listOf("b")))

        myFixture.editor.caretModel.moveToOffset(resolvedYB.endOffset)
        myFixture.complete(CompletionType.BASIC)
        completions = myFixture.lookupElementStrings!!
        assert(completions.containsAll(listOf("b")))

        myFixture.editor.caretModel.moveToOffset(notResolvedX.endOffset)
        myFixture.complete(CompletionType.BASIC)
        completions = myFixture.lookupElementStrings!!
        assert(completions.containsAll(listOf("textarea")))

        myFixture.editor.caretModel.moveToOffset(resolvedXInHelper.endOffset)
        myFixture.complete(CompletionType.BASIC)
        completions = myFixture.lookupElementStrings!!
        assert(completions.containsAll(listOf("a", "x", "y")))
    }

    fun testToJsFile() {
        val hbs = """
            {{this.}}
            {{this.x.}}
            {{this.y.}}
            {{x}}
            {{in-helper (fn this.)}}
        """.trimIndent()
        val js = """
            export default class extends Component {
                @tracked a;
                x: { b: string };
                /**
                 * @type {{b: string}}
                 */
                y;
            }
        """.trimIndent()
        myFixture.addFileToProject("component.js", js)
        myFixture.configureByText("template.hbs", hbs)
        val element = PsiTreeUtil.collectElements(myFixture.file, { it.elementType == HbTokenTypes.ID })
        val resolvedA = element.find { it.parent.text == "this." }!!.nextSibling.nextSibling
        val resolvedXB = element.find { it.parent.text == "this.x" }!!.parent.parent.nextSibling
        val resolvedYB = element.find { it.parent.text == "this.y" }!!.parent.parent.nextSibling
        val notResolvedX = element.find { it.parentsWithSelf.find { it is HbPathImpl }?.text == "x" }!!
        val resolvedXInHelper = element.findLast { it.parent.text == "this." }!!.nextSibling.nextSibling

        myFixture.editor.caretModel.moveToOffset(resolvedA.endOffset)
        myFixture.complete(CompletionType.BASIC)
        var completions = myFixture.lookupElementStrings!!
        assert(completions.containsAll(listOf("x", "y", "a")))

        myFixture.editor.caretModel.moveToOffset(resolvedXB.endOffset)
        myFixture.complete(CompletionType.BASIC)
        completions = myFixture.lookupElementStrings!!
        assert(completions.containsAll(listOf("b")))

        myFixture.editor.caretModel.moveToOffset(resolvedYB.endOffset)
        myFixture.complete(CompletionType.BASIC)
        completions = myFixture.lookupElementStrings!!
        assert(completions.containsAll(listOf("b")))

        myFixture.editor.caretModel.moveToOffset(notResolvedX.endOffset)
        myFixture.complete(CompletionType.BASIC)
        completions = myFixture.lookupElementStrings!!
        assert(completions.containsAll(listOf("textarea")))

        myFixture.editor.caretModel.moveToOffset(resolvedXInHelper.endOffset)
        myFixture.complete(CompletionType.BASIC)
        completions = myFixture.lookupElementStrings!!
        assert(completions.containsAll(listOf("a", "x", "y")))
    }

    fun testBlockToYieldCompletion() {
        val hbsWithYield = """
            {{#let (hash name='Sarah' title=office) as |item|}}
                {{yield item}}
            {{/each}} 
        """.trimIndent()
        val hbs = """
            <MyComponent as |item|>
                {{item.}}
            </MyComponent>
        """.trimIndent()
        myFixture.addFileToProject("app/components/my-component/template.hbs", hbsWithYield)
        myFixture.addFileToProject("app/routes/index/template.hbs", hbs)
        myFixture.addFileToProject("package.json", "{}")
        myFixture.addFileToProject(".ember-cli", "")
        myFixture.configureByFile("app/routes/index/template.hbs")
        val element = PsiTreeUtil.collectElements(myFixture.file, { it.elementType == HbTokenTypes.ID })
        val resolvedA = element.findLast { it.parent.text == "item" }!!.parent.parent.parent.nextSibling
        myFixture.editor.caretModel.moveToOffset(resolvedA.endOffset)
        myFixture.complete(CompletionType.BASIC)
        val completions = myFixture.lookupElementStrings!!
        assert(completions.containsAll(listOf("name", "title")))
    }
}
