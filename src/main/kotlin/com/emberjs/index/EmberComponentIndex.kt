package com.emberjs.index

import com.intellij.util.indexing.ID

class EmberComponentIndex : EmberIndexBase("components", "Component") {

    override fun getName() = NAME

    companion object {
        val NAME: ID<String, Void> = ID.create("ember.component")
    }
}
