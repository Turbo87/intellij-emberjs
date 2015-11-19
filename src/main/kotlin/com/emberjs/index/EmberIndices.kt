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

class EmberAdapterIndex : EmberIndexBase(EmberIndices.ADAPTER, "adapters", "Adapter")
class EmberComponentIndex: EmberIndexBase(EmberIndices.COMPONENT, "components", "Component")
class EmberControllerIndex: EmberIndexBase(EmberIndices.CONTROLLER, "controllers", "Controller")
class EmberHelperIndex: EmberIndexBase(EmberIndices.HELPER, "helpers", "Helper")
class EmberModelIndex: EmberIndexBase(EmberIndices.MODEL, "models", "Model")
class EmberRouteIndex: EmberIndexBase(EmberIndices.ROUTE, "routes", "Route")
class EmberSerializerIndex: EmberIndexBase(EmberIndices.SERIALIZER, "serializers", "Serializer")
class EmberServiceIndex: EmberIndexBase(EmberIndices.SERVICE, "services", "Service")
class EmberTransformIndex: EmberIndexBase(EmberIndices.TRANSFORM, "transforms", "Transform")
