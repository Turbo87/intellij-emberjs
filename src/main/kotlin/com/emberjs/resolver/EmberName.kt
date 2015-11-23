package com.emberjs.resolver

data class EmberName private constructor(val type: String, val name: String) {
    companion object {
        fun from(fullName: String): EmberName? {
            val parts = fullName.split(":")
            return when {
                parts.count() == 2 -> EmberName(parts[0], parts[1])
                else -> null
            }
        }
    }
}
