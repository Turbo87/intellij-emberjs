package com.emberjs.index

import com.intellij.util.indexing.ID

class EmberServiceIndex : EmberIndexBase("services", "Service") {

    override fun getName() = NAME

    companion object {
        val NAME: ID<String, Void> = ID.create("ember.service")
    }
}
