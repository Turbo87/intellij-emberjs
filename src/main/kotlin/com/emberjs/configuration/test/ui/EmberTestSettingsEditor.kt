package com.emberjs.configuration.test.ui

import com.emberjs.configuration.StringOptionsField
import com.emberjs.configuration.test.EmberTestConfiguration
import com.emberjs.configuration.test.EmberTestOptions
import com.emberjs.configuration.test.FilterType
import com.emberjs.configuration.test.LaunchType
import com.emberjs.configuration.utils.PublicStringAddEditDeleteListPanel
import com.emberjs.project.EmberModuleType
import com.intellij.application.options.ModulesComboBox
import com.intellij.execution.configuration.EnvironmentVariablesTextFieldWithBrowseButton
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterField
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.ui.CollectionComboBoxModel
import com.intellij.ui.EditorTextField
import com.intellij.ui.HideableDecorator
import com.intellij.ui.IdeBorderFactory
import org.jetbrains.annotations.NotNull
import java.util.*
import javax.swing.*
import kotlin.reflect.KProperty1

class EmberTestSettingsEditor(
        val project: Project
) : SettingsEditor<EmberTestConfiguration>() {
    private var myPanel: JPanel? = null
    private var advancedSettings: JPanel? = null
    private var advancedSettingsWrapper: JPanel? = null
    private var launcherPanel: JPanel? = null
    private var launcherWrapperPanel: JPanel? = null
    private var emberTestFilterPanel: JPanel? = null

    private var testPortTextField: EditorTextField? = null
    private var environmentComboBox: JComboBox<String>? = null

    private var hostTextField: EditorTextField? = null
    private var configFileTextField: EditorTextField? = null
    private var filterTextField: EditorTextField? = null
    private var moduleTextField: EditorTextField? = null
    private var watcherTextField: EditorTextField? = null
    private var testemDebugTextField: EditorTextField? = null
    private var testPageTextField: EditorTextField? = null
    private var pathTextField: EditorTextField? = null
    private var queryTextField: EditorTextField? = null

    private var launcherDefaultRadioButton: JRadioButton? = null
    private var launcherCustomRadioButton: JRadioButton? = null

    private var filterAllRadioButton: JRadioButton? = null
    private var filterModuleRadioButton: JRadioButton? = null
    private var filterFilterRadioButton: JRadioButton? = null

    private var launchAddEditDeleteListPanel: PublicStringAddEditDeleteListPanel? = null

    private var filterModulePanel: JPanel? = null
    private var filterFilterPanel: JPanel? = null

    private var launcherButtonGroup: ButtonGroup? = null
    private var filterButtonGroup: ButtonGroup? = null

    private var myModulesComboBox: ModulesComboBox? = null
    private var myNodeSettingsPanel: JPanel? = null
    private var myNodeInterpreterField: NodeJsInterpreterField? = null
    private var myEnvVars: EnvironmentVariablesTextFieldWithBrowseButton? = null

    val bundle: ResourceBundle = ResourceBundle.getBundle("com.emberjs.locale.EmberTestConfigurationEditor")
    val launchers: MutableList<String> = mutableListOf()

    private val mappings = mutableListOf<Pair<Any?, KProperty1<EmberTestOptions, StringOptionsField>>>(
            testPortTextField to EmberTestOptions::testPort,
            environmentComboBox to EmberTestOptions::environment,
            hostTextField to EmberTestOptions::host,
            configFileTextField to EmberTestOptions::configFile,
            filterTextField to EmberTestOptions::filter,
            moduleTextField to EmberTestOptions::module,
            watcherTextField to EmberTestOptions::watcher,
            testemDebugTextField to EmberTestOptions::testemDebug,
            pathTextField to EmberTestOptions::path,
            queryTextField to EmberTestOptions::query,
            testPageTextField to EmberTestOptions::testPage
    )

    private val launchTypeToRadioButton = mutableListOf(
            LaunchType.DEFAULT to launcherDefaultRadioButton,
            LaunchType.CUSTOM to launcherCustomRadioButton
    )

    private val filterTypeToRadioButton = mutableListOf(
            FilterType.ALL to filterAllRadioButton,
            FilterType.FILTER to filterFilterRadioButton,
            FilterType.MODULE to filterModuleRadioButton
    )

    override fun resetEditorFrom(configuration: EmberTestConfiguration) {
        val launchType = try {
            LaunchType.valueOf(configuration.options.launchOption.value)
        } catch (_: Exception) {
            LaunchType.DEFAULT
        }
        val filterType = try {
            FilterType.valueOf(configuration.options.filterOption.value)
        } catch (_: Exception) {
            FilterType.ALL
        }

        handleLaunchRadio(launchType)
        handleFilterRadio(filterType)

        launchTypeToRadioButton.forEach { (first, second) ->
            second!!.isSelected = first.value == launchType.toString()
        }
        filterTypeToRadioButton.forEach { (first, second) ->
            second!!.isSelected = first.value == filterType.toString()
        }

        mappings
                .filter { it.first != null }
                .forEach { (first, second) -> first?.let { second.get(configuration.options).writeTo(it) } }

        myModulesComboBox?.fillModules(configuration.project, EmberModuleType.instance)
        val module = configuration.module
        when (module) {
            null -> myModulesComboBox?.selectedModule = myModulesComboBox?.model?.getElementAt(0)
            else -> myModulesComboBox?.selectedModule = module
        }

        myNodeInterpreterField?.interpreterRef = when (configuration.nodeInterpreter) {
            is String -> NodeJsInterpreterRef.create(configuration.nodeInterpreter)
            else -> NodeJsInterpreterRef.createProjectRef()
        }

        configuration.env?.let {
            myEnvVars?.envs = it
        }
    }

    @Throws(ConfigurationException::class)
    override fun applyEditorTo(configuration: EmberTestConfiguration) {
        mappings
                .filter { it.first != null }
                .forEach { (first, second) -> first?.let { second.get(configuration.options).readFrom(it) } }

        configuration.module = myModulesComboBox?.selectedModule
        configuration.nodeInterpreter = myNodeInterpreterField?.interpreterRef?.referenceName
        configuration.env = myEnvVars?.envs
    }

    @NotNull
    override fun createEditor(): JComponent {
        // Create the collapsible component for advances settings
        HideableDecorator(advancedSettingsWrapper, bundle.getString("advanced"), false).apply {
            setContentComponent(advancedSettings as JComponent)
            setOn(false)
        }

        emberTestFilterPanel?.border = IdeBorderFactory.createTitledBorder(bundle.getString("configuration.title"), false)

        // somehow the ButtonGroup aren't bound :(
        launcherButtonGroup = ButtonGroup().apply {
            add(launcherCustomRadioButton)
            add(launcherDefaultRadioButton)
        }

        filterButtonGroup = ButtonGroup().apply {
            add(filterAllRadioButton)
            add(filterModuleRadioButton)
            add(filterFilterRadioButton)
        }

        mappings.add(filterButtonGroup to EmberTestOptions::filterOption)
        mappings.add(launcherButtonGroup to EmberTestOptions::launchOption)

        environmentComboBox?.model = CollectionComboBoxModel<String>().apply {
            add("test")
            add("development")
            add("production")
        }

        launchAddEditDeleteListPanel = object : PublicStringAddEditDeleteListPanel(null, launchers) {
            override fun editSelectedItem(item: String?): String? {
                val message = Messages.showInputDialog(this,
                        bundle.getString("launchers.edit.message"),
                        bundle.getString("launchers.edit.title"),
                        Messages.getQuestionIcon(),
                        item, null)

                return if (message === null || message.isEmpty()) {
                    null
                } else {
                    message
                }
            }

            override fun findItemToAdd(): String? {
                val message = Messages.showInputDialog(this,
                        bundle.getString("launchers.create.message"),
                        bundle.getString("launchers.create.title"),
                        Messages.getQuestionIcon(),
                        "", null)

                return if (message === null || message.isEmpty()) {
                    null
                } else {
                    message
                }
            }
        }

        launchTypeToRadioButton.forEach { (first, second) ->
            second!!.actionCommand = first.value
            second.addActionListener { handleLaunchRadio(first) }
        }
        filterTypeToRadioButton.forEach { (first, second) ->
            second!!.actionCommand = first.value
            second.addActionListener { handleFilterRadio(first) }
        }

        launcherPanel!!.add(launchAddEditDeleteListPanel)
        mappings.add(launchAddEditDeleteListPanel to EmberTestOptions::launch)

        launcherWrapperPanel!!.border = IdeBorderFactory.createTitledBorder(bundle.getString("launchers"), false)

        // Add interpreter field to editor
        myNodeInterpreterField = NodeJsInterpreterField(project, false)
                .apply { interpreterRef = NodeJsInterpreterRef.createProjectRef() }
        myNodeSettingsPanel?.layout = BoxLayout(myNodeSettingsPanel, BoxLayout.PAGE_AXIS)
        myNodeSettingsPanel?.add(myNodeInterpreterField)

        return myPanel as JComponent
    }

    fun handleLaunchRadio(type: LaunchType) {
        when (type) {
            LaunchType.CUSTOM -> launcherPanel!!.isVisible = true
            else -> launcherPanel!!.isVisible = false
        }
    }

    fun handleFilterRadio(type: FilterType) {
        when (type) {
            FilterType.MODULE -> {
                filterModulePanel!!.isVisible = true
                filterFilterPanel!!.isVisible = false
            }
            FilterType.FILTER -> {
                filterModulePanel!!.isVisible = false
                filterFilterPanel!!.isVisible = true
            }
            else -> {
                filterModulePanel!!.isVisible = false
                filterFilterPanel!!.isVisible = false
            }
        }
    }
}