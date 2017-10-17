package com.emberjs.configuration.serve.ui

import com.emberjs.configuration.serve.EmberServeConfiguration
import com.emberjs.configuration.serve.EmberServeOptions
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.options.SettingsEditor
import com.intellij.ui.CollectionComboBoxModel
import com.intellij.ui.EditorTextField
import com.intellij.ui.HideableDecorator
import org.jetbrains.annotations.NotNull
import javax.swing.*

class EmberServeSettingsEditor : SettingsEditor<EmberServeConfiguration>() {
    private var myPanel: JPanel? = null
    private var advancedSettings: JPanel? = null
    private var advancedSettingsWrapper: JPanel? = null

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

    override fun resetEditorFrom(serveConfiguration: EmberServeConfiguration) {
        mappings.forEach { (first, second) ->
            first?.let { second.get(serveConfiguration.options).writeTo(it) }
        }
    }

    @Throws(ConfigurationException::class)
    override fun applyEditorTo(serveConfiguration: EmberServeConfiguration) {
        mappings.forEach { (first, second) ->
            first?.let { second.get(serveConfiguration.options).readFrom(it) }
        }
    }

    override fun getSnapshot(): EmberServeConfiguration {
        return super.getSnapshot()
    }

    @NotNull
    override fun createEditor(): JComponent {
        // Create the collapsible component for advances settings
        HideableDecorator(advancedSettingsWrapper, "Advanced settings", false).apply {
            setContentComponent(advancedSettings as JComponent)
            setOn(false)
        }

        val comboBoxModel = CollectionComboBoxModel<String>().apply {
            add("development")
            add("production")
        }

        environmentComboBox?.model = comboBoxModel

        return myPanel as JComponent
    }
}