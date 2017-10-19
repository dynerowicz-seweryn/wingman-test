package me.dynerowicz.wtest.database

import android.provider.BaseColumns

/** Contract describing the table used to encode the postal codes. */
object DatabaseContract {
    private const val TABLE_NAME = "postalCodes"
    private const val COLUMN_CODIGO_POSTAL = "postalCode"
    private const val COLUMN_EXTENSAO_CODIGO_POSTAL = "extension"
    private const val COLUMN_LOCALIDADE = "localidade"

    const val CREATE_TABLE_STATEMENT = "CREATE TABLE ${DatabaseContract.TABLE_NAME} (" +
            "${BaseColumns._ID} INTEGER PRIMARY KEY, " +
            "${DatabaseContract.COLUMN_CODIGO_POSTAL} TEXT, " +
            "${DatabaseContract.COLUMN_EXTENSAO_CODIGO_POSTAL} TEXT, " +
            "${DatabaseContract.COLUMN_LOCALIDADE} TEXT " +
            ")"

    const val DROP_TABLE_STATEMENT = "DROP TABLE IF EXISTS ${DatabaseContract.TABLE_NAME}"
}

