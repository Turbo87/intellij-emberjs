package com.emberjs.configuration.serve.ui

import com.emberjs.configuration.serve.EmberServeConfiguration
import com.emberjs.configuration.serve.EmberServeOptions
import com.emberjs.project.EmberModuleType
import com.intellij.application.options.ModulesComboBox
import com.intellij.execution.configuration.EnvironmentVariablesTextFieldWithBrowseButton
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterField
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.ui.CollectionComboBoxModel
import com.intellij.ui.EditorTextField
import com.intellij.ui.HideableDecorator
import com.intellij.ui.IdeBorderFactory
import org.jetbrains.annotations.NotNull
import java.util.*
import javax.swing.*

class EmberServeSettingsEditor(
        val project: Project
) : SettingsEditor<EmberServeConfiguration>() {
    private var myPanel: JPanel? = null
    private var advancedSettings: JPanel? = null
    private var advancedSettingsWrapper: JPanel? = null
    private var emberServePanel: JPanel? = null

    private var portTextField: EditorTextField? = null
    private var hostTextField: EditorTextField? = null
    private var proxyTextField: EditorTextField? = null

    private var watcherTextField: EditorTextField? = null
    private var liveReloadHostTextField: EditorTextField? = null
    private var liveReloadBaseUrlTextField: EditorTextField? = null
    private var liveReloadPortTextField: EditorTextField? = null

    private var environmentComboBox: JComboBox<String>? = null
    private var outputPathTextField: EditorTextField? = null

    private var sslKeyTextField: EditorTextField? = null
    private var sslCertTextField: EditorTextField? = null

    private var liveReloadCheckBox: JCheckBox? = null
    private var secureProxyCheckBox: JCheckBox? = null
    private var transparentProxyCheckBox: JCheckBox? = null
    private var sslCheckBox: JCheckBox? = null

    private var myModulesComboBox: ModulesComboBox? = null
    private var myNodeSettingsPanel: JPanel? = null
    private var myNodeInterpreterField: NodeJsInterpreterField? = null
    private var myEnvVars: EnvironmentVariablesTextFieldWithBrowseButton? = null

    private val bundle: ResourceBundle = ResourceBundle.getBundle("com.emberjs.locale.EmberServeConfigurationEditor")

    private val mappings = listOf(
            portTextField to EmberServeOptions::port,
            environmentComboBox to EmberServeOptions::environment,

            hostTextField to EmberServeOptions::host,
            outputPathTextField to EmberServeOptions::outputPath,
            watcherTextField to EmberServeOptions::watcher,

            proxyTextField to EmberServeOptions::proxy,
            secureProxyCheckBox to EmberServeOptions::proxySecure,
            transparentProxyCheckBox to EmberServeOptions::proxyTransparent,

            liveReloadCheckBox to EmberServeOptions::liveReload,
            liveReloadHostTextField to EmberServeOptions::liveReloadHost,
            liveReloadBaseUrlTextField to EmberServeOptions::liveReloadBaseUrl,
            liveReloadPortTextField to EmberServeOptions::liveReloadPort,

            sslCheckBox to EmberServeOptions::ssl,
            sslKeyTextField to EmberServeOptions::sslKey,
            sslCertTextField to EmberServeOptions::sslCert
    )

    override fun resetEditorFrom(configuration: EmberServeConfiguration) {
        mappings.forEach { (first, second) ->
            first?.let { second.get(configuration.options).writeTo(it) }
        }

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

    override fun applyEditorTo(configuration: EmberServeConfiguration) {
        mappings.forEach { (first, second) ->
            first?.let { second.get(configuration.options).readFrom(it) }
        }
        configuration.module = myModulesComboBox?.selectedModule
        configuration.nodeInterpreter = myNodeInterpreterField?.interpreterRef?.referenceName
        configuration.env = myEnvVars?.envs
    }

    @NotNull
    override fun createEditor(): JComponent {
        emberServePanel?.border = IdeBorderFactory.createTitledBorder(bundle.getString("configuration.title"), false)

        // Create the collapsible component for advances settings
        HideableDecorator(advancedSettingsWrapper, bundle.getString("advanced"), false).apply {
            setContentComponent(advancedSettings as JComponent)
            setOn(false)
        }

        // Add interpreter field to editor
        myNodeInterpreterField = NodeJsInterpreterField(project, false)
                .apply { interpreterRef = NodeJsInterpreterRef.createProjectRef() }
        myNodeSettingsPanel?.layout = BoxLayout(myNodeSettingsPanel, BoxLayout.PAGE_AXIS)
        myNodeSettingsPanel?.add(myNodeInterpreterField)

        val comboBoxModel = CollectionComboBoxModel<String>().apply {
            add("development")
            add("production")
        }

        environmentComboBox?.model = comboBoxModel

        return myPanel as JComponent
    }
}