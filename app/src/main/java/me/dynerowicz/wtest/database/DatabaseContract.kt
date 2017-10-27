package me.dynerowicz.wtest.database

import android.provider.BaseColumns

// TODO: figure out how to handle properly the encoding used throughout the database ...
/** Contract describing the table used to encode the postal codes. */
object DatabaseContract {
    const val POSTAL_CODES_TABLE = "postalCodes"
    const val LOCALITY_NAMES_TABLE = "localityNames"

    const val COLUMN_POSTAL_CODE_WITH_EXTENSION_ID = BaseColumns._ID
    const val COLUMN_POSTAL_CODE_WITH_EXTENSION = "postalCodeWithExtension"
    const val COLUMN_LOCALITY_NAME_ID = BaseColumns._ID
    const val COLUMN_LOCALITY_NAME = "localityName"
    const val COLUMN_LOCALITY_IDENTIFIER = "localityIdentifier"

    // Fields as they appear in the CSV to be imported
    const val CSV_NUMBER_OF_FIELDS = 14
    const val CSV_POSTAL_CODE = "cod_postal"
    const val CSV_EXTENSION = "extensao_cod_postal"
    const val CSV_LOCALITY = "localidade"

    const val CREATE_POSTAL_CODES_TABLE =
        "CREATE TABLE ${DatabaseContract.POSTAL_CODES_TABLE}" +
        "( ${DatabaseContract.COLUMN_POSTAL_CODE_WITH_EXTENSION_ID} INTEGER PRIMARY KEY" +
        ", ${DatabaseContract.COLUMN_POSTAL_CODE_WITH_EXTENSION} INTEGER NOT NULL" +
        ", ${DatabaseContract.COLUMN_LOCALITY_IDENTIFIER} INTEGER NOT NULL" +
        ", FOREIGN KEY (${DatabaseContract.COLUMN_LOCALITY_IDENTIFIER}) REFERENCES ${DatabaseContract.LOCALITY_NAMES_TABLE}(${BaseColumns._ID})" +
        ")"

    const val CREATE_LOCALITY_NAMES_TABLE =
        "CREATE TABLE ${DatabaseContract.LOCALITY_NAMES_TABLE}" +
        "( ${DatabaseContract.COLUMN_LOCALITY_NAME_ID} INTEGER PRIMARY KEY" +
        ", ${DatabaseContract.COLUMN_LOCALITY_NAME} TEXT NOT NULL UNIQUE" +
        ")"

    const val CREATE_POSTAL_CODES_INDEX =
        "CREATE INDEX IF NOT EXISTS IndexPostalCodes " +
        "ON ${DatabaseContract.POSTAL_CODES_TABLE}(${DatabaseContract.COLUMN_POSTAL_CODE_WITH_EXTENSION})"

    const val CREATE_LOCALITY_NAMES_INDEX =
        "CREATE INDEX IF NOT EXISTS IndexLocalityNames " +
        "ON ${DatabaseContract.LOCALITY_NAMES_TABLE}(${DatabaseContract.COLUMN_LOCALITY_NAME})"

    const val CREATE_LOCALITY_ID_INDEX =
        "CREATE INDEX IF NOT EXISTS IndexLocalityIdentifier " +
        "ON ${DatabaseContract.POSTAL_CODES_TABLE}(${DatabaseContract.COLUMN_LOCALITY_NAME_ID})"

    const val DROP_TABLE_STATEMENT =
        "DROP TABLE IF EXISTS ${DatabaseContract.POSTAL_CODES_TABLE}"

    const val INSERT_INTO_LOCALITY_NAMES_STATEMENT =
        "INSERT into ${DatabaseContract.LOCALITY_NAMES_TABLE}" +
        "( ${DatabaseContract.COLUMN_LOCALITY_NAME}" +
        ") VALUES (?)"

    const val INSERT_INTO_POSTAL_CODES_STATEMENT =
        "INSERT into ${DatabaseContract.POSTAL_CODES_TABLE}" +
        "( ${DatabaseContract.COLUMN_POSTAL_CODE_WITH_EXTENSION}" +
        ", ${DatabaseContract.COLUMN_LOCALITY_IDENTIFIER}" +
        ") VALUES (?,?)"
}
