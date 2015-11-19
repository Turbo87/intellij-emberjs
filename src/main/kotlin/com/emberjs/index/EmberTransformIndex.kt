package com.emberjs.index

import com.intellij.util.indexing.ID

class EmberTransformIndex : EmberIndexBase("transforms", "Transform") {

    override fun getName() = NAME

    companion object {
        val NAME: ID<String, Void> = ID.create("ember.transform")
    }
}
