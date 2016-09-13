package com.emberjs.icons

import com.intellij.openapi.util.IconLoader

object EmberIcons {
    val ICON_16 = IconLoader.getIcon("/com/emberjs/icons/icon16.png")
    val ICON_24 = IconLoader.getIcon("/com/emberjs/icons/icon24.png")

    val EMPTY_16 = IconLoader.getIcon("/com/emberjs/icons/empty16.png")
    val ADAPTER_16 = IconLoader.getIcon("/com/emberjs/icons/adapter16.png")
    val COMPONENT_16 = IconLoader.getIcon("/com/emberjs/icons/component16.png")
    val CONTROLLER_16 = IconLoader.getIcon("/com/emberjs/icons/controller16.png")
    val MODEL_16 = IconLoader.getIcon("/com/emberjs/icons/model16.png")
    val ROUTE_16 = IconLoader.getIcon("/com/emberjs/icons/route16.png")
    val SERVICE_16 = IconLoader.getIcon("/com/emberjs/icons/service16.png")

    val FILE_TYPE_ICONS = mapOf(
            "adapter" to ADAPTER_16,
            "component" to COMPONENT_16,
            "controller" to CONTROLLER_16,
            "model" to MODEL_16,
            "route" to ROUTE_16,
            "service" to SERVICE_16
    )
}
