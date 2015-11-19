package com.emberjs.navigation

import com.emberjs.index.*
import com.intellij.openapi.util.IconLoader

class EmberAdapterGotoClassContributor : EmberGotoClassContributorBase(
        EmberIndices.ADAPTER, IconLoader.getIcon("/com/emberjs/icons/adapter16.png"))

class EmberComponentGotoClassContributor : EmberGotoClassContributorBase(
        EmberIndices.COMPONENT, IconLoader.getIcon("/com/emberjs/icons/component16.png"))

class EmberControllerGotoClassContributor : EmberGotoClassContributorBase(
        EmberIndices.CONTROLLER, IconLoader.getIcon("/com/emberjs/icons/controller16.png"))

class EmberHelperGotoClassContributor : EmberGotoClassContributorBase(EmberIndices.HELPER)

class EmberModelGotoClassContributor : EmberGotoClassContributorBase(
        EmberIndices.MODEL, IconLoader.getIcon("/com/emberjs/icons/model16.png"))

class EmberRouteGotoClassContributor : EmberGotoClassContributorBase(
        EmberIndices.ROUTE, IconLoader.getIcon("/com/emberjs/icons/route16.png"))

class EmberSerializerGotoClassContributor : EmberGotoClassContributorBase(EmberIndices.SERIALIZER)

class EmberServiceGotoClassContributor : EmberGotoClassContributorBase(
        EmberIndices.SERVICE, IconLoader.getIcon("/com/emberjs/icons/service16.png"))

class EmberTransformGotoClassContributor : EmberGotoClassContributorBase(EmberIndices.TRANSFORM)
