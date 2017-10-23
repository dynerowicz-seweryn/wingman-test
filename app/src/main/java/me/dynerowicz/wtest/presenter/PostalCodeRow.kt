package me.dynerowicz.wtest.presenter

data class PostalCodeRow(val postalCode: Long, val extension: Long, val locality: String, val localityNormalized: String? = null) {
    override fun toString(): String = StringBuilder().apply {
        append(postalCode)
        append('-')
        append(extension)
        append(", ")
        append(locality)
        if (localityNormalized != null) {
            append(':')
            append(localityNormalized)
        }
    }.toString()

    override fun equals(other: Any?): Boolean {
        other as PostalCodeRow
        return postalCode == other.postalCode && extension == other.extension && locality == other.locality
    }
}