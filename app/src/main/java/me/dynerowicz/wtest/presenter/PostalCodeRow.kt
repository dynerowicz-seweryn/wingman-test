package me.dynerowicz.wtest.presenter

data class PostalCodeRow(private val postalCode: Long, private val extension: Long, private val locality: String) {
    override fun toString(): String =
        StringBuilder().append(postalCode)
                .append('-')
                .append(extension)
                .append(locality)
                .toString()
}