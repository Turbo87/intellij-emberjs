package com.emberjs.template

import com.intellij.codeInsight.template.impl.DefaultLiveTemplatesProvider

class EmberLiveTemplatesProvider : DefaultLiveTemplatesProvider {
    override fun getDefaultLiveTemplateFiles() = DEFAULT_TEMPLATES
    override fun getHiddenLiveTemplateFiles() = arrayOf<String>()

    companion object {
        val DEFAULT_TEMPLATES = arrayOf("/com/emberjs/template/Ember.js", "/com/emberjs/template/HTMLBars");
    }
}
