package com.emberjs.configuration

interface EmberOptions {
    fun fields(): Array<OptionsField<out Any>>
    fun toCommandLineOptions(): Array<String>
}