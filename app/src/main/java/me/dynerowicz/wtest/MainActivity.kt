package me.dynerowicz.wtest

import android.app.ProgressDialog
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import me.dynerowicz.wtest.download.FileDownloaderTask
import java.io.File

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    val progressDialog: ProgressDialog? by lazy { ProgressDialog(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigation.setOnNavigationItemSelectedListener(this)

        progressDialog?.setMessage("Downloading file")
        progressDialog?.isIndeterminate = true
        progressDialog?.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        progressDialog?.setCancelable(true)

        FileDownloaderTask(this).execute()
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

    fun onDownloadSucceeded(outputFile: File) {
        Toast.makeText(this, "Download successful: ${outputFile.name}", Toast.LENGTH_LONG).show()
        // Process the file into the database, then delete the temporary file
        outputFile.delete()
    }

    fun onDownloadFailed() {
        Toast.makeText(this, "Download FAILED", Toast.LENGTH_LONG).show()
    }
}
