package com.emberjs.index

import com.intellij.util.indexing.ID

class EmberControllerIndex : EmberIndexBase("controllers", "Controller") {

    override fun getName() = NAME

    companion object {
        val NAME: ID<String, Void> = ID.create("ember.controller")
    }
}
