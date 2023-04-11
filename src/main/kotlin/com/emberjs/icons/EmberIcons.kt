package com.emberjs.icons

import com.intellij.openapi.util.IconLoader

object EmberIcons {
    val ICON_16 = IconLoader.getIcon("/com/emberjs/icons/icon16.png", EmberIcons.javaClass.classLoader)
    val ICON_24 = IconLoader.getIcon("/com/emberjs/icons/icon24.png", EmberIcons.javaClass.classLoader)

    val EMPTY_16 = IconLoader.getIcon("/com/emberjs/icons/empty16.png", EmberIcons.javaClass.classLoader)
    val ADAPTER_16 = IconLoader.getIcon("/com/emberjs/icons/adapter16.png", EmberIcons.javaClass.classLoader)
    val COMPONENT_16 = IconLoader.getIcon("/com/emberjs/icons/component16.png", EmberIcons.javaClass.classLoader)
    val CONTROLLER_16 = IconLoader.getIcon("/com/emberjs/icons/controller16.png", EmberIcons.javaClass.classLoader)
    val MODEL_16 = IconLoader.getIcon("/com/emberjs/icons/model16.png", EmberIcons.javaClass.classLoader)
    val ROUTE_16 = IconLoader.getIcon("/com/emberjs/icons/route16.png", EmberIcons.javaClass.classLoader)
    val SERVICE_16 = IconLoader.getIcon("/com/emberjs/icons/service16.png", EmberIcons.javaClass.classLoader)

    val TEMPLATE_LINT_16 = IconLoader.getIcon("/com/emberjs/icons/template-lint16.png", EmberIcons.javaClass.classLoader)

    val FILE_TYPE_ICONS = mapOf(
            "adapter" to ADAPTER_16,
            "component" to COMPONENT_16,
            "controller" to CONTROLLER_16,
            "model" to MODEL_16,
            "route" to ROUTE_16,
            "service" to SERVICE_16
    )
}
