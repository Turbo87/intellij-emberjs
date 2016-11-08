package com.emberjs.cli

import com.intellij.openapi.util.text.StringUtil

class EmberCliBlueprintListParser {

    fun parse(output: String): Collection<EmberCliBlueprint> {
        if (output.isEmpty()) return emptyList()
        val result: MutableList<EmberCliBlueprint> = mutableListOf()
        val converted = StringUtil.convertLineSeparators(output)
        var name: String? = null
        var description: String? = null
        var arguments: MutableList<String> = mutableListOf()
        for (line in converted.split('\n')) {
            if (line.startsWith("      ")) {
                var text = line.substring(6)
                if (text.isBlank()) continue

                val nameCandidate = firstWord(text)
                if (nameCandidate.isNotBlank()) {
                    if (name != null) {
                        result.add(EmberCliBlueprint(name, description, arguments))
                        description = null
                        arguments = mutableListOf()
                    }

                    name = nameCandidate
                    continue
                }
                if (!text.startsWith("  ")) continue
                text = text.substring(2)

                if (text.startsWith("-")) arguments.add(firstWord(text))
                else if (!text.isBlank() && !text[0].isWhitespace()) description = text
            }
        }
        return result
    }

    private fun firstWord(text: String) = text.takeWhile { !it.isWhitespace() }
}

