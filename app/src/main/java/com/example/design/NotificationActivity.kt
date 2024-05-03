package com.example.design

import android.graphics.Typeface
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException

class NotificationActivity : AppCompatActivity() {

    private lateinit var notificationContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)
        notificationContainer = findViewById(R.id.notificationContainer)

        // Get the registration number from intent
        val regNo = intent.getStringExtra("regNo") ?: ""

        // Call the function to retrieve placement data
        retrievePlacementData(regNo)
    }

    private fun retrievePlacementData(currentRegNo: String) {
        // Use Kotlin coroutines to execute the database query in a background thread
        GlobalScope.launch(Dispatchers.IO) {
            var connection: Connection? = null
            var resultSet: ResultSet? = null

            try {
                // Connect to the database
                Class.forName("net.sourceforge.jtds.jdbc.Driver")
                val url = "jdbc:jtds:sqlserver://192.168.183.201/student"
                val username = "sandy"
                val password = "s@ndy"
                connection = DriverManager.getConnection(url, username, password)

                // Execute SQL query to retrieve placement data
                val query = "SELECT pcid, regno, placeddate " +
                        "FROM PlacementStudent " +
                        "WHERE regno = '$currentRegNo'"
                resultSet = connection.createStatement().executeQuery(query)

                // Process the results
                while (resultSet.next()) {
                    val pcid = resultSet.getInt("pcid")

                    // Retrieve data from PlacementMaster table based on pcid
                    val masterQuery = "SELECT  cname, jobrole " +
                            "FROM PlacementMaster " +
                            "WHERE pcid = $pcid"
                    val statement = connection.createStatement()
                    val masterResultSet = statement.executeQuery(masterQuery)

                    // Process the master results
                    val notificationMessage = StringBuilder()
                    while (masterResultSet.next()) {
                        // Retrieve data from PlacementMaster table
                        val companyName = masterResultSet.getString("cname")
                        val jobTitle = masterResultSet.getString("jobrole")

                        // Append notification message to StringBuilder
                        notificationMessage.append("Congratulations! You've been selected for the position of $jobTitle at $companyName\n")
                    }
                    masterResultSet.close()
                    statement.close()

                    // Create a new TextView for each notification
                    val notificationTextView = TextView(this@NotificationActivity)
                    notificationTextView.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    notificationTextView.text = notificationMessage.toString()
                    notificationTextView.setTextColor(ContextCompat.getColor(this@NotificationActivity, android.R.color.black))
                    notificationTextView.textSize = resources.getDimension(R.dimen.notification_text_size)
                    notificationTextView.setTypeface(null, Typeface.NORMAL)

                    val paddingValue = resources.getDimensionPixelSize(R.dimen.notification_text_padding)
                    notificationTextView.setPadding(paddingValue, paddingValue, paddingValue, paddingValue)

                    // Add the TextView to the container layout
                    runOnUiThread {
                        notificationContainer.addView(notificationTextView)
                    }
                }
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            } catch (e: SQLException) {
                e.printStackTrace()
            } finally {
                // Close JDBC objects
                resultSet?.close()
                connection?.close()
            }
        }
    }
}
