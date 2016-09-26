package com.emberjs.utils

import kotlin.text.MatchResult
import kotlin.text.Regex

private val STRING_CLASSIFY_REGEXP_1 = Regex("^(\\-|_)+(.)?")
private val STRING_CLASSIFY_REGEXP_2 = Regex("(.)(\\-|\\_|\\.|\\s)+(.)?")
private val STRING_CLASSIFY_REGEXP_3 = Regex("(^|\\/|\\.)([a-z])")

public fun String.classify(): String {
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

public fun String.prepend(prefix: String) = "$prefix${this}"
public fun String.append(suffix: String) = "${this}$suffix"
