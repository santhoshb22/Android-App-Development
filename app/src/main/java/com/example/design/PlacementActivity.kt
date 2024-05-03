package com.example.design

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.design.databinding.PlacementBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.DriverManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit
import java.io.Serializable


data class PlacementData(
    val jobRole: String,
    val companyName: String,
    val location: String,
    val postedDate: String,
    val pcode: String
):Serializable

class PlacementActivity : AppCompatActivity(), PlacementAdapter.OnItemClickListener {
    private lateinit var binding: PlacementBinding
    private val placementDataList = mutableListOf<PlacementData>()
    private lateinit var adapter: PlacementAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PlacementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve the studentPcode from intent extras
        val studentPcode = intent.getStringExtra("program")
        val dcode=intent.getStringExtra("dcode")
        Log.d("PlacementActivity", "dcode: $dcode")

        // Initialize RecyclerView
        adapter = PlacementAdapter(placementDataList)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        adapter.setOnItemClickListener(this@PlacementActivity)


        // Call the function to fetch data and populate RecyclerView
        dcode?.let {
            fetchDataAndPopulateRecyclerView(it)
        }
    }

    override fun onItemClick(placementData: PlacementData) {
        // Handle item click here
        // Start a new Activity or Fragment with detailed information
        val intent = Intent(this, PlacementDetailActivity::class.java)
        intent.putExtra("placementData", placementData)
        startActivity(intent)
    }

    private fun fetchDataAndPopulateRecyclerView(dcode: String) {
        // Inside fetchDataAndPopulateRecyclerView()

        lifecycleScope.launch(Dispatchers.IO) {
            val newPlacementDataList = mutableListOf<PlacementData>() // Create a new list for fetched data
            try {
                DriverManager.getConnection("jdbc:jtds:sqlserver://192.168.183.201/student", "sandy", "s@ndy").use { connection ->val query = "SELECT * FROM PlacementMaster WHERE ',' + pcode + ',' LIKE '%,' + ? + ',%'"
                    connection.prepareStatement(query).use { statement ->
                        statement.setString(1, dcode)
                        val resultSet = statement.executeQuery()
                        // Fetch data from database
                        var hasJobs = false // Flag to indicate if there are jobs for the studentPcode

                        while (resultSet.next()) {
                            hasJobs = true // Set flag to true if at least one job is found

                            val jobRole = resultSet.getString("jobrole")
                            val companyName = resultSet.getString("cname")
                            val location = resultSet.getString("venue")
                            val postedDate = resultSet.getString("enter_datetime")
                            val pcode = resultSet.getString("pcode")
                            val formattedPostedDate = formatPostedDate(postedDate)
                            val placementData = PlacementData(jobRole, companyName, location, formattedPostedDate, pcode)
                            newPlacementDataList.add(placementData)
                            Log.d("PlacementActivity", "Retrieved data: $placementData")
                        }
                        if (!hasJobs) {
                            Log.d("PlacementActivity", "No jobs found for studentPcode: $dcode")
                        }
                    }
                }
                // Update RecyclerView on the main thread
                withContext(Dispatchers.Main) {
                    placementDataList.clear() // Clear existing data
                    placementDataList.addAll(newPlacementDataList) // Update with new data
                    adapter.notifyDataSetChanged() // Notify RecyclerView of dataset change
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle exceptions
            }
        }

    }
    fun formatPostedDate(postedDate: String): String {
        // Parse the postedDate string to Date object
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
        val postedDateTime = dateFormat.parse(postedDate)

        // Get the current date
        val currentDate = Date()

        // Calculate the difference in milliseconds between current date and posted date
        val diffInMillis = currentDate.time - postedDateTime.time

        // Convert milliseconds to days
        val daysAgo = TimeUnit.MILLISECONDS.toDays(diffInMillis)

        // Format the result
        return when {
            daysAgo == 0L -> "posted today"
            daysAgo == 1L -> "posted 1 day ago"
            else -> "posted $daysAgo days ago"
        }
    }
}
