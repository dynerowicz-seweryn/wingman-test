package me.dynerowicz.wtest.presenter

data class PostalCodeRow(val postalCode: Long, val extension: Long, val locality: String) {
    constructor(postalCodeWithExtension: Long, locality: String)
        : this(postalCodeWithExtension / 1000L, postalCodeWithExtension % 1000L, locality)

    companion object {
        fun postalCodeWithExtension(postalCode: Long, extension: Long): Long {
            if (postalCode < 0 || 9999L < postalCode)
                throw IllegalArgumentException("Invalid postal code provided: should be at most four digits")

            if (extension < 0L || 999L < extension)
                throw IllegalArgumentException("Invalid extension provided: should be at most three digits")

            return postalCode * 1000L + extension
        }
    }
}