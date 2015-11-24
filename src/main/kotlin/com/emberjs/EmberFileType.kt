package com.emberjs

enum class EmberFileType(val fileExtension: String = "js") {
    ADAPTER(),
    COMPONENT(),
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
        val FILE_NAMES = values.toMap({ it.fileName }, { it })
        val FOLDER_NAMES = values.toMap({ it.folderName }, { it })
    }
}
