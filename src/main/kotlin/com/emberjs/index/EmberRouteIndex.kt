package com.emberjs.index

import com.intellij.util.indexing.ID

class EmberRouteIndex : EmberIndexBase("routes", "Route") {

    override fun getName() = NAME

    companion object {
        val NAME: ID<String, Void> = ID.create("ember.route")
    }
}
