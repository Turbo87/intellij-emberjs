package com.emberjs.configuration.serve

import com.emberjs.configuration.BooleanOptionsField
import com.emberjs.configuration.EmberOptions
import com.emberjs.configuration.OptionsField
import com.emberjs.configuration.StringOptionsField

class EmberServeOptions : EmberOptions {
    val port = StringOptionsField("4200", "PORT", "port")
    val host = StringOptionsField("", "HOST", "host")
    val environment = StringOptionsField("development", "ENVIRONMENT", "environment")
    val outputPath = StringOptionsField("dist/", "OUTPUT_PATH", "output-path")
    val watcher = StringOptionsField("events", "WATCHER", "watcher")

    val proxy = StringOptionsField("", "PROXY", "proxy")
    val proxySecure = BooleanOptionsField(true, "PROXY_SECURE", "secure-proxy")
    val proxyTransparent = BooleanOptionsField(true, "PROXY_TRANSPARENT", "transparent-proxy")

    val liveReload = BooleanOptionsField(true, "LIVE_RELOAD", "live-reload")
    val liveReloadHost = StringOptionsField("", "LIVE_RELOAD_HOST", "live-reload-host")
    val liveReloadBaseUrl = StringOptionsField("", "LIVE_RELOAD_BASE_URL", "live-reload-base-url")
    val liveReloadPort = StringOptionsField("", "LIVE_RELOAD_PORT", "live-reload-port")

    val ssl = BooleanOptionsField(false, "SSL", "ssl")
    val sslKey = StringOptionsField("ssl/server.key", "SSL_KEY", "ssl-key")
    val sslCert = StringOptionsField("ssl/server.crt", "SSL_CERT", "ssl-cert")

    override fun fields(): Array<OptionsField<out Any>> {
        return arrayOf(
                port,
                host,
                environment,
                outputPath,
                watcher,

                proxy,
                proxySecure,
                proxyTransparent,

                liveReload,
                liveReloadHost,
                liveReloadBaseUrl,
                liveReloadPort,

                ssl,
                sslKey,
                sslCert
        )
    }

    override fun toCommandLineOptions(): Array<String> {
        return fields()
                .filter { optionsField ->
                    optionsField.value != optionsField.default &&
                            optionsField.value.toString().isNotEmpty()
                }
                .map { optionsField -> "--${optionsField.cmdlineOptionName}=${optionsField.value}" }
                .toTypedArray()
    }
}