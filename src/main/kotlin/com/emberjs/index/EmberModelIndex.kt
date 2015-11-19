package com.emberjs.index

import com.intellij.util.indexing.ID

class EmberModelIndex : EmberIndexBase("models", "Model") {

    override fun getName() = NAME

    companion object {
        val NAME: ID<String, Void> = ID.create("ember.model")
    }
}
