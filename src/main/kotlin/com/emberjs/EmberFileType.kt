package com.emberjs

enum class EmberFileType(val fileExtension: String = "js") {
    ADAPTER(),
    COMPONENT(),
    CONTROLLER(),
    HELPER(),
    INITIALIZER(),
    MIXIN(),
    MODEL(),
    ROUTE(),
    SERIALIZER(),
    SERVICE(),
    TEMPLATE("hbs"),
    TRANSFORM(),
    VIEW();

    val fileName = "${name.toLowerCase()}.$fileExtension"
    val folderName = "${name.toLowerCase()}s"

    companion object {
        val FILE_NAMES = values().toMap({ it.fileName }, { it })
        val FOLDER_NAMES = values().toMap({ it.folderName }, { it })
    }
}
