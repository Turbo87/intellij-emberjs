package com.emberjs.index

import com.intellij.util.indexing.ID

object EmberIndices {
    val ADAPTER: ID<String, Void> = ID.create("ember.adapter")
    val COMPONENT: ID<String, Void> = ID.create("ember.component")
    val CONTROLLER: ID<String, Void> = ID.create("ember.controller")
    val HELPER: ID<String, Void> = ID.create("ember.helper")
    val MODEL: ID<String, Void> = ID.create("ember.model")
    val ROUTE: ID<String, Void> = ID.create("ember.route")
    val SERIALIZER: ID<String, Void> = ID.create("ember.serializer")
    val SERVICE: ID<String, Void> = ID.create("ember.service")
    val TRANSFORM: ID<String, Void> = ID.create("ember.transform")
}

class EmberAdapterIndex : EmberIndexBase(EmberIndices.ADAPTER)
class EmberComponentIndex: EmberIndexBase(EmberIndices.COMPONENT)
class EmberControllerIndex: EmberIndexBase(EmberIndices.CONTROLLER)
class EmberHelperIndex: EmberIndexBase(EmberIndices.HELPER)
class EmberModelIndex: EmberIndexBase(EmberIndices.MODEL)
class EmberRouteIndex: EmberIndexBase(EmberIndices.ROUTE)
class EmberSerializerIndex: EmberIndexBase(EmberIndices.SERIALIZER)
class EmberServiceIndex: EmberIndexBase(EmberIndices.SERVICE)
class EmberTransformIndex: EmberIndexBase(EmberIndices.TRANSFORM)
