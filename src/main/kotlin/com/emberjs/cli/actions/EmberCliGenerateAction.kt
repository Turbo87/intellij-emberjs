package com.emberjs.cli.actions

import com.emberjs.cli.EmberCliBlueprint
import com.emberjs.cli.EmberCliBlueprintListLoader
import com.emberjs.cli.EmberCliFilter
import com.emberjs.cli.EmberCliProjectGenerator
import com.emberjs.icons.EmberIconProvider
import com.emberjs.icons.EmberIcons
import com.emberjs.utils.isEmberFolder
import com.intellij.execution.configurations.CommandLineTokenizer
import com.intellij.icons.AllIcons
import com.intellij.javascript.nodejs.CompletionModuleInfo
import com.intellij.javascript.nodejs.NodeModuleSearchUtil
import com.intellij.javascript.nodejs.NodeSettings
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter
import com.intellij.lang.javascript.boilerplate.NpmPackageProjectGenerator
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.keymap.KeymapManager
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.openapi.ui.popup.IconButton
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.text.StringUtil
import com.intellij.ui.*
import com.intellij.ui.components.JBList
import com.intellij.ui.speedSearch.ListWithFilter
import com.intellij.util.ui.JBUI
import icons.JavaScriptLanguageIcons
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import javax.swing.*

class EmberCliGenerateAction : DumbAwareAction(TEXT, DESCRIPTION, ICON) {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return

        val model = DefaultListModel<EmberCliBlueprint>()
        val list = JBList<EmberCliBlueprint>(model).apply {
            cellRenderer = object : JBList.StripedListCellRenderer() {
                override fun getListCellRendererComponent(list: JList<*>?, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus).apply {
                        icon = (value as? EmberCliBlueprint)?.name
                                ?.let { EmberIconProvider.getIcon(it) }
                                ?: EmberIcons.EMPTY_16
                    }
                }
            }
        }

        updateList(list, model, project)

        val actionGroup = DefaultActionGroup()
        val refresh: AnAction = object : AnAction(JavaScriptLanguageIcons.BuildTools.Refresh) {
            init {
                shortcutSet = CustomShortcutSet(*KeymapManager.getInstance().activeKeymap.getShortcuts("Refresh"))
            }

            override fun actionPerformed(e: AnActionEvent?) {
                EmberCliBlueprintListLoader.CacheModificationTracker.count++
                updateList(list, model, project)
            }
        }
        refresh.registerCustomShortcutSet(refresh.shortcutSet, list)
        actionGroup.addAction(refresh)

        val actionToolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLBAR, actionGroup, true).apply {
            setReservePlaceAutoPopupIcon(false)
            setMinimumButtonSize(Dimension(22, 22))

            component.apply {
                isOpaque = false
            }
        }

        val scroll = ScrollPaneFactory.createScrollPane(list).apply {
            border = IdeBorderFactory.createEmptyBorder()
        }

        val pane = ListWithFilter.wrap(list, scroll, StringUtil.createToStringFunction(Any::class.java))

        val popup = JBPopupFactory.getInstance().createComponentPopupBuilder(pane, list)
                .setMayBeParent(true)
                .setRequestFocus(true)
                .setFocusable(true)
                .setFocusOwners(arrayOf<Component>(list))
                .setLocateWithinScreenBounds(true)
                .setCancelOnOtherWindowOpen(true)
                .setMovable(true)
                .setResizable(true)
                .setSettingButtons(actionToolbar.component)
                .setCancelOnWindowDeactivation(false)
                .setCancelOnClickOutside(true)
                .setDimensionServiceKey(project, "org.ember.cli.generate", true)
                .setMinSize(Dimension(JBUI.scale(200), JBUI.scale(200)))
                .setCancelButton(IconButton("Close", AllIcons.Actions.Close, AllIcons.Actions.CloseHovered))
                .createPopup()

        list.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent?) {
                if (e?.keyCode == KeyEvent.VK_ENTER) {
                    e?.consume()
                    askOptions(project, popup, list.selectedValue as EmberCliBlueprint)
                }
            }
        })

        object : DoubleClickListener() {
            override fun onDoubleClick(event: MouseEvent?): Boolean {
                if (list.selectedValue == null) return true
                askOptions(project, popup, list.selectedValue as EmberCliBlueprint)
                return true
            }
        }.installOn(list)

        popup.showCenteredInCurrentWindow(project)
    }

    private fun updateList(list: JBList<EmberCliBlueprint>, model: DefaultListModel<EmberCliBlueprint>, project: Project) {
        list.setPaintBusy(true)
        model.clear()
        ApplicationManager.getApplication().executeOnPooledThread {
            val blueprints = EmberCliBlueprintListLoader.load(project)
            ApplicationManager.getApplication().invokeLater {
                blueprints.forEach { model.addElement(it) }
                list.setPaintBusy(false)
            }
        }
    }

    private fun askOptions(project: Project, popup: JBPopup, blueprint: EmberCliBlueprint) {
        popup.closeOk(null)
        val dialog = object : DialogWrapper(project, true) {
            private lateinit var editor: EditorTextField

            init {
                title = "Generate ${blueprint.name}"
                init()
            }

            override fun createCenterPanel(): JComponent {
                val panel = JPanel(BorderLayout())
                panel.add(JLabel(blueprint.description), BorderLayout.NORTH)
                editor = TextFieldWithAutoCompletion.create(project, blueprint.args, false, null)
                editor.setPreferredWidth(250)
                panel.add(LabeledComponent.create(editor, "Parameters"), BorderLayout.SOUTH)
                return panel
            }

            override fun getPreferredFocusedComponent(): JComponent {
                return editor
            }

            fun arguments(): Array<String> {
                val tokenizer = CommandLineTokenizer(editor.text)
                val result: MutableList<String> = mutableListOf()
                while (tokenizer.hasMoreTokens()) {
                    result.add(tokenizer.nextToken())
                }
                return result.toTypedArray()
            }
        }

        if (dialog.showAndGet()) {
            runGenerator(project, blueprint, dialog.arguments())
        }
    }

    private fun runGenerator(project: Project, blueprint: EmberCliBlueprint, arguments: Array<String>) {
        val interpreter = NodeJsInterpreterManager.getInstance(project).default
        val node = NodeJsLocalInterpreter.tryCast(interpreter) ?: return

        val modules: MutableList<CompletionModuleInfo> = mutableListOf()
        val baseDir = project.baseDir
        NodeModuleSearchUtil.findModulesWithName(modules, "ember-cli", baseDir, NodeSettings.create(node), false)

        val modulePath = modules.firstOrNull()?.virtualFile?.path ?: return
        val ember = EmberCliProjectGenerator.executable(modulePath)

        val filter = EmberCliFilter(project, baseDir.path)
        NpmPackageProjectGenerator.generate(node, ember, project.baseDir, project.baseDir, project, null, arrayOf(filter), "generate", blueprint.name, *arguments)
    }

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabledAndVisible = event.project?.baseDir?.isEmberFolder == true
    }

    companion object {
        val TEXT = "Ember CLI ..."
        val DESCRIPTION = "Generates new code from Ember CLI blueprints"
        val ICON = EmberIcons.ICON_16
    }
}
