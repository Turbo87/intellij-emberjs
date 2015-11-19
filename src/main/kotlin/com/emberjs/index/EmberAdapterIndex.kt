package com.emberjs.index

import com.intellij.util.indexing.ID

class EmberAdapterIndex : EmberIndexBase("adapters", "Adapter") {

    override fun getName() = NAME

    companion object {
        val NAME: ID<String, Void> = ID.create("ember.adapter")
    }
}
