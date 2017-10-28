package me.dynerowicz.wtest.database

import android.provider.BaseColumns

// TODO: figure out how to handle properly the encoding used throughout the database ...
/** Contract describing the table used to encode the postal codes. */
object DatabaseContract {
    const val POSTAL_CODES_TABLE = "postalCodes"
    const val LOCALITIES_TABLE = "localities"
    const val LOCALITY_NORMALIZED_NAMES_TABLE = "localityNormalizedNames"

    const val COLUMN_ID = "rowid"
    const val COLUMN_POSTAL_CODE_WITH_EXTENSION = "postalCodeWithExtension"
    const val COLUMN_NAME = "name"

    const val COLUMN_NORMALIZED_NAME_IDENTIFIER = "normalizedNameIdentifier"
    const val COLUMN_LOCALITY_IDENTIFIER = "localityIdentifier"

    // Fields as they appear in the CSV to be imported
    const val CSV_NUMBER_OF_FIELDS = 14
    const val CSV_POSTAL_CODE = "cod_postal"
    const val CSV_EXTENSION = "extensao_cod_postal"
    const val CSV_LOCALITY = "localidade"

    const val CREATE_POSTAL_CODES_TABLE =
        "CREATE TABLE $POSTAL_CODES_TABLE" +
        "( $COLUMN_POSTAL_CODE_WITH_EXTENSION INTEGER NOT NULL" +
        ", $COLUMN_LOCALITY_IDENTIFIER INTEGER NOT NULL" +
        ", FOREIGN KEY ($COLUMN_LOCALITY_IDENTIFIER) REFERENCES $LOCALITIES_TABLE($COLUMN_ID)" +
        ")"

    const val CREATE_LOCALITIES_TABLE =
        "CREATE TABLE $LOCALITIES_TABLE" +
        "( $COLUMN_NAME TEXT NOT NULL UNIQUE" +
        ", $COLUMN_NORMALIZED_NAME_IDENTIFIER INTEGER NOT NULL" +
        ", FOREIGN KEY ($COLUMN_NORMALIZED_NAME_IDENTIFIER) REFERENCES $LOCALITY_NORMALIZED_NAMES_TABLE($COLUMN_ID)" +
        ")"

    const val CREATE_NORMALIZED_LOCALITY_NAMES_TABLE =
        "CREATE TABLE $LOCALITY_NORMALIZED_NAMES_TABLE ($COLUMN_NAME TEXT NOT NULL UNIQUE)"

    const val CREATE_POSTAL_CODES_INDEX =
        "CREATE INDEX IF NOT EXISTS IndexPostalCodes ON $POSTAL_CODES_TABLE($COLUMN_POSTAL_CODE_WITH_EXTENSION)"

    const val CREATE_LOCALITY_ID_INDEX =
        "CREATE INDEX IF NOT EXISTS IndexLocalityIdentifier ON $POSTAL_CODES_TABLE($COLUMN_LOCALITY_IDENTIFIER)"

    const val CREATE_LOCALITY_NAME_INDEX =
        "CREATE INDEX IF NOT EXISTS IndexLocalityName ON $LOCALITIES_TABLE($COLUMN_NAME)"

    const val CREATE_NORMALIZED_NAME_ID_INDEX =
        "CREATE INDEX IF NOT EXISTS IndexNormalizedNameIdentifier ON $LOCALITIES_TABLE($COLUMN_NORMALIZED_NAME_IDENTIFIER)"

    const val DROP_POSTAL_CODES_TABLE = "DROP TABLE IF EXISTS $POSTAL_CODES_TABLE"
    const val DROP_LOCALITIES_TABLE =  "DROP TABLE IF EXISTS $LOCALITIES_TABLE"
    const val DROP_NORMALIZED_NAMES_TABLE = "DROP TABLE IF EXISTS $LOCALITY_NORMALIZED_NAMES_TABLE"

    const val INSERT_LOCALITY =
        "INSERT into $LOCALITIES_TABLE ($COLUMN_NAME, $COLUMN_NORMALIZED_NAME_IDENTIFIER) VALUES (?,?)"

    const val INSERT_NORMALIZED_NAME =
        "INSERT into $LOCALITY_NORMALIZED_NAMES_TABLE ($COLUMN_NAME) VALUES (?)"

    const val INSERT_POSTAL_CODE =
        "INSERT into ${DatabaseContract.POSTAL_CODES_TABLE}" +
        "( ${DatabaseContract.COLUMN_POSTAL_CODE_WITH_EXTENSION}" +
        ", ${DatabaseContract.COLUMN_LOCALITY_IDENTIFIER}" +
        ") VALUES (?,?)"
}
