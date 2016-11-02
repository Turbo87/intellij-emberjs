package com.emberjs.utils

import java.util.Locale
import kotlin.text.MatchResult
import kotlin.text.Regex

private val STRING_CLASSIFY_REGEXP_1 = Regex("^(\\-|_)+(.)?")
private val STRING_CLASSIFY_REGEXP_2 = Regex("(.)(\\-|\\_|\\.|\\s)+(.)?")
private val STRING_CLASSIFY_REGEXP_3 = Regex("(^|\\/|\\.)([a-z])")

fun String.classify(): String {
    val replace1: (MatchResult) -> String = {
        it.groups[2]?.value?.toUpperCase()?.prepend("_").orEmpty()
    }

    val replace2: (MatchResult) -> String = {
        it.groups[3]?.value?.toUpperCase().orEmpty().prepend(it.groups[1]?.value.orEmpty())
    }

    return this.splitToSequence("/")
            .map { part -> part.replace(STRING_CLASSIFY_REGEXP_1, replace1) }
            .map { part -> part.replace(STRING_CLASSIFY_REGEXP_2, replace2) }
            .joinToString("/")
            .replace(STRING_CLASSIFY_REGEXP_3) { it.value.toUpperCase() }
}

private val STRING_DECAMELIZE_REGEXP = Regex("([a-z\\d])([A-Z])")

fun String.decamelize(): String = replace(STRING_DECAMELIZE_REGEXP, "$1_$2").toLowerCase(Locale.ROOT)

private val STRING_DASHERIZE_REGEXP = Regex("[ _]")

fun String.dasherize(): String = decamelize().replace(STRING_DASHERIZE_REGEXP, "-")

fun String.prepend(prefix: String) = "$prefix${this}"
fun String.append(suffix: String) = "${this}$suffix"
