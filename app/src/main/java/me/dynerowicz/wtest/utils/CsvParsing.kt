package me.dynerowicz.wtest.utils

import java.security.InvalidParameterException

fun Array<StringBuilder>.parseCsvLine(line: String, vararg fieldIndicesToExtract: Int): Int {
    if (size != fieldIndicesToExtract.size)
        throw InvalidParameterException("Array<StringBuild> and fieldIndicesToExtract must have the same size")

    forEach { builder -> builder.setLength(0) }

    var numberOfCommas = 0
    var betweenDoubleQuotes = false

    var currentFieldToExtract = 0

    for (idx in 0 until line.length) {
        val character = line[idx]
        if(character == ',' && !betweenDoubleQuotes) {
            if (currentFieldToExtract < fieldIndicesToExtract.size && numberOfCommas == fieldIndicesToExtract[currentFieldToExtract])
                currentFieldToExtract += 1
            numberOfCommas += 1
        } else {
            if(currentFieldToExtract < fieldIndicesToExtract.size && numberOfCommas == fieldIndicesToExtract[currentFieldToExtract])
                get(currentFieldToExtract).append(character)

            if (character == '"')
                betweenDoubleQuotes = !betweenDoubleQuotes
        }
    }

    return numberOfCommas + 1
}