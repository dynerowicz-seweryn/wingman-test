package me.dynerowicz.wtest

import android.app.ProgressDialog
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import com.opencsv.CSVReader
import kotlinx.android.synthetic.main.activity_main.*
import me.dynerowicz.wtest.database.DatabaseHelper
import me.dynerowicz.wtest.database.importFromCsv
import me.dynerowicz.wtest.database.insertPostalCode
import me.dynerowicz.wtest.download.FileDownloaderTask
import java.io.File

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    val TAG = MainActivity::class.java.simpleName

    lateinit var progressDialog: ProgressDialog

    lateinit var database: SQLiteDatabase
    lateinit var fileDownloader: FileDownloaderTask

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigation.setOnNavigationItemSelectedListener(this)

        progressDialog = ProgressDialog(this).apply {
            setMessage("Downloading file")
            isIndeterminate = true
            setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            setCancelable(true)
        }

        database = DatabaseHelper(this).writableDatabase

        val existingFile = cacheDir.listFiles().find { it.name.startsWith("codigos_postais.csv") }

        if(existingFile == null) {
            fileDownloader = FileDownloaderTask(this)
            fileDownloader.execute()
        } else
            onFileAvailable(existingFile)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val resourceId =
            when (item.itemId) {
                R.id.navigation_home -> R.string.title_home
                R.id.navigation_dashboard -> R.string.title_dashboard
                R.id.navigation_notifications -> R.string.title_notifications
                else -> -1
            }

        if (resourceId == -1)
            return false

        fieldMessage.setText(resourceId)
        return true
    }

    fun onFileAvailable(csvFile: File) {
        Toast.makeText(this, "Download successful: ${csvFile.name}", Toast.LENGTH_LONG).show()
        // Import the entries from the CSV file into the database
        database.importFromCsv(csvFile)
        // Delete the temporary file
        csvFile.delete()
    }

    fun onDownloadFailed() {
        Toast.makeText(this, "Download FAILED", Toast.LENGTH_LONG).show()
    }
}
