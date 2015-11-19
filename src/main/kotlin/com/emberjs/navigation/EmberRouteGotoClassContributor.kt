package com.emberjs.navigation

import com.emberjs.index.*
import com.intellij.openapi.util.IconLoader

class EmberAdapterGotoClassContributor : EmberGotoClassContributorBase(
        EmberAdapterIndex.NAME, IconLoader.getIcon("/com/emberjs/icons/adapter16.png"))

class EmberComponentGotoClassContributor : EmberGotoClassContributorBase(
        EmberComponentIndex.NAME, IconLoader.getIcon("/com/emberjs/icons/component16.png"))

class EmberControllerGotoClassContributor : EmberGotoClassContributorBase(
        EmberControllerIndex.NAME, IconLoader.getIcon("/com/emberjs/icons/controller16.png"))

class EmberHelperGotoClassContributor : EmberGotoClassContributorBase(EmberHelperIndex.NAME)

class EmberModelGotoClassContributor : EmberGotoClassContributorBase(
        EmberModelIndex.NAME, IconLoader.getIcon("/com/emberjs/icons/model16.png"))

class EmberRouteGotoClassContributor : EmberGotoClassContributorBase(
        EmberRouteIndex.NAME, IconLoader.getIcon("/com/emberjs/icons/route16.png"))

class EmberSerializerGotoClassContributor : EmberGotoClassContributorBase(EmberSerializerIndex.NAME)

class EmberServiceGotoClassContributor : EmberGotoClassContributorBase(
        EmberServiceIndex.NAME, IconLoader.getIcon("/com/emberjs/icons/service16.png"))

class EmberTransformGotoClassContributor : EmberGotoClassContributorBase(EmberTransformIndex.NAME)
