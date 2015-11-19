package com.emberjs.index

import com.intellij.util.indexing.ID

class EmberSerializerIndex : EmberIndexBase("serializers", "Serializer") {

    override fun getName() = NAME

    companion object {
        val NAME: ID<String, Void> = ID.create("ember.serializer")
    }
}
