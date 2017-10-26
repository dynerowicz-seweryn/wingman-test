package me.dynerowicz.wtest.database

import android.provider.BaseColumns

// TODO: figure out how to handle properly the encoding used throughout the database ...
/** Contract describing the table used to encode the postal codes. */
object DatabaseContract {
    const val TABLE_NAME = "postalCodes"
    const val COLUMN_POSTAL_CODE_WITH_EXTENSION = "postalCodeWithExtension"
    const val COLUMN_LOCALITY = "locality"

    private const val INDEX_POSTAL_CODE = "IDX_POSTAL_CODE"

    // Fields as they appear in the CSV to be imported
    const val CSV_NUMBER_OF_FIELDS = 14
    const val CSV_POSTAL_CODE = "cod_postal"
    const val CSV_EXTENSION = "extensao_cod_postal"
    const val CSV_LOCALITY = "localidade"

    const val CREATE_TABLE_STATEMENT =
        "CREATE TABLE ${DatabaseContract.TABLE_NAME} (" +
        "${BaseColumns._ID} INTEGER PRIMARY KEY" +
        ", ${DatabaseContract.COLUMN_POSTAL_CODE_WITH_EXTENSION} INTEGER NOT NULL" +
        ", ${DatabaseContract.COLUMN_LOCALITY} TEXT NOT NULL COLLATE UNICODE" +
        ")"

    const val CREATE_INDEX_STATEMENT =
        "CREATE INDEX IF NOT EXISTS ${DatabaseContract.INDEX_POSTAL_CODE} " +
        "ON ${DatabaseContract.TABLE_NAME}(${DatabaseContract.COLUMN_POSTAL_CODE_WITH_EXTENSION})"

    const val DROP_TABLE_STATEMENT =
        "DROP TABLE IF EXISTS ${DatabaseContract.TABLE_NAME}"

    const val INSERT_QUERY =
        "INSERT into ${DatabaseContract.TABLE_NAME} (" +
        "${DatabaseContract.COLUMN_POSTAL_CODE_WITH_EXTENSION}" +
        ",${DatabaseContract.COLUMN_LOCALITY}" +
        ") VALUES (?,?)"
}
