package com.emberjs.configuration.test

import com.emberjs.configuration.EmberOptions
import com.emberjs.configuration.OptionsField
import com.emberjs.configuration.StringOptionsField

enum class LaunchType(val value: String) {
    DEFAULT("DEFAULT"),
    CUSTOM("CUSTOM"),
}

enum class FilterType(val value: String) {
    ALL("ALL"),
    MODULE("MODULE"),
    FILTER("FILTER")
}

class EmberTestOptions : EmberOptions {
    val environment = StringOptionsField("test", "ENVIRONMENT", "environment")
    val configFile = StringOptionsField("", "CONFIG_FILE", "config-file")
    val host = StringOptionsField("", "HOST", "host")
    val testPort = StringOptionsField("7357", "TEST_PORT", "test-port")

    val filterOption = StringOptionsField(FilterType.ALL.value, "FILTER_OPTION", "")
    val filter = StringOptionsField("", "FILTER", "filter")
    val module = StringOptionsField("", "MODULE", "module")

    val watcher = StringOptionsField("events", "WATCHER", "watcher")
    val testemDebug = StringOptionsField("", "TESTEM_DEBUG", "testem-debug")
    val testPage = StringOptionsField("", "TEST_PAGE", "test-page")
    val path = StringOptionsField("", "PATH", "path")
    val query = StringOptionsField("", "QUERY", "query")
    val reporter = StringOptionsField("tap", "REPORTER", "reporter")

    val launchOption = StringOptionsField(LaunchType.DEFAULT.value, "LAUNCH_OPTION", "")
    val launch = StringOptionsField("", "LAUNCH", "launch")

    override fun fields(): Array<OptionsField<out Any>> {
        return arrayOf(
                environment,
                configFile,
                host,
                testPort,
                filter,
                filterOption,
                module,
                watcher,
                testemDebug,
                testPage,
                path,
                query,
                reporter,
                launch,
                launchOption
        )
    }

    override fun toCommandLineOptions(): Array<String> {
        val options = fields()
                .filter {
                    // allow any fields that aren't LAUNCH, FILTER, MODULE
                    !arrayOf("LAUNCH", "FILTER", "MODULE").contains(it.field) ||
                    // allow any LAUNCH if LAUNCH_OPTION is CUSTOM
                    it.field == "LAUNCH" && launchOption.value == LaunchType.CUSTOM.value ||
                    // allow any FILTER if FILTER_OPTION is FILTER
                    it.field =="FILTER" && filterOption.value == FilterType.FILTER.value ||
                    // allow any MODULE if FILTER_OPTION is MODULE
                    it.field =="MODULE" && filterOption.value == FilterType.MODULE.value
                }
                .filter { optionsField ->
                    optionsField.cmdlineOptionName.isNotEmpty() &&
                    optionsField.value != optionsField.default &&
                    optionsField.value.toString().isNotEmpty()
                }
                .map { "--${it.cmdlineOptionName}=${it.value}" }
                .toTypedArray()

        return options
    }
}