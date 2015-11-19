package com.emberjs.index

import com.intellij.util.indexing.ID

class EmberHelperIndex : EmberIndexBase("helpers", "Helper") {

    override fun getName() = NAME

    companion object {
        val NAME: ID<String, Void> = ID.create("ember.helper")
    }
}
