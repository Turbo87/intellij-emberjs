package com.emberjs

enum class EmberFileType(val fileExtension: String = "js") {
    ADAPTER(),
    COMPONENT(),
    COMPONENT_TEMPLATE("hbs"),
    CONTROLLER(),
    HELPER(),
    MODEL(),
    ROUTE(),
    SERIALIZER(),
    SERVICE(),
    TEMPLATE("hbs"),
    TRANSFORM();

    val fileName = "${name.toLowerCase()}.$fileExtension"
    val folderName = "${name.toLowerCase()}s"

    companion object {
        val FOLDER_NAMES = values.map { it.folderName }
    }
}
