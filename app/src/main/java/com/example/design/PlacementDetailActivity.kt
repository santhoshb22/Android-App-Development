package com.example.design

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import java.sql.ResultSet
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class PlacementDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.placement_detail_activity)
        val databaseTask = PlacementDatabaseTask(this)
        databaseTask.execute()


        // You can add any additional initialization code here
    }

    class PlacementDatabaseTask(private val activity: PlacementDetailActivity) {
        private val executor: Executor = Executors.newSingleThreadExecutor()

        fun execute() {
            executor.execute {
                val resultSet = doInBackground()
                if (resultSet != null) {
                    activity.updateUI(resultSet)
                }
            }
        }

        private fun doInBackground(vararg params: Void?): ResultSet? {
            val url = "jdbc:jtds:sqlserver://192.168.183.201/HRM_Personalinfo"
            val username = "sandy"
            val password = "s@ndy"
            val query =
                "SELECT [pcid], [cname], [jobrole], [jobdescription], [ten], [twelve], [cgpa], [Idate], [venue], [link], [batch], [pcode], [academic_year], [enterby], [enter_datetime] FROM [student].[dbo].[PlacementMaster]"

            return try {
                val connection =
                    SqlConnectionHelper2.connect(url, username, password) ?: return null
                val resultSet = SqlConnectionHelper2.executeQuery(connection, query)
                resultSet
            } catch (e: Exception) {
                Log.e("PlacementDatabaseTask", "Error executing query: ${e.message}")
                e.printStackTrace()
                null
            }
        }
    }

    // Update the updateUI method in YourActivity to accept ResultSet as parameter
    fun updateUI(resultSet: ResultSet) {
        if (resultSet.next()) {
            val jobTitle = resultSet.getString("jobrole") // Change "jobTitle" to the actual column name in your database
            val companyName = resultSet.getString("cname") // Change "companyName" to the actual column name in your database
            val jobDescription = resultSet.getString("jobdescription") // Change "jobDescription" to the actual column name in your database
            val criteria1 = resultSet.getString("ten")
            val criteria2 = resultSet.getString("twelve")
            val criteria3 = resultSet.getString("cgpa")
            val interviewDate = resultSet.getString("Idate") // Change "interviewDate" to the actual column name in your database
            val venue = resultSet.getString("venue")
            val applylink = resultSet.getString("link")

            findViewById<TextView>(R.id.jobTitleTextView)?.text = jobTitle
            findViewById<TextView>(R.id.companyNameTextView)?.text = companyName
            findViewById<TextView>(R.id.jdtext)?.text = jobDescription
            findViewById<TextView>(R.id.criteriatext)?.text = "10th Mark > $criteria1 || 12th Mark > $criteria2 || cgpa > $criteria3"
            findViewById<TextView>(R.id.interviewdte)?.text = interviewDate
            findViewById<TextView>(R.id.VenueName)?.text = venue
            val applyButton = findViewById<MaterialButton>(R.id.applyButton)
            applyButton.setOnClickListener {
                val applyLink = resultSet.getString("link")
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(applyLink))
                startActivity(intent)
            }
        }

    }
}
