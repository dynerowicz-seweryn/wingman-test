package me.dynerowicz.wtest.presenter

data class PostalCodeRow(val postalCode: Long, val extension: Long, val locality: String) {
    override fun toString(): String =
        StringBuilder().append(postalCode)
                .append('-')
                .append(extension)
                .append(locality)
                .toString()
}