package com.example.design
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.sql.ResultSet

class HomeActivity : AppCompatActivity() {
    private var regNo: String = ""
    private var program: String = ""
    private var dcode: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        regNo = intent.getStringExtra("regNo") ?: ""
        val databaseTask = DatabaseTask(this)
        databaseTask.execute()
    }

    fun onProfileCardClicked(view: View) {
        // Handle click event for profile card view
        // For example, start a new activity
        val intent = Intent(this, StudentProfile::class.java)
        intent.putExtra("regNo", regNo)
        startActivity(intent)
    }

    fun onIPTCardClicked(view: View) {
        // Handle click event for profile card view
        // For example, start a new activity
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("regNo", regNo)
        startActivity(intent)
    }

    fun onPlacementCardClicked(view: View) {
        // Handle click event for profile card view
        // For example, start a new activity
        val intent = Intent(this, PlacementActivity::class.java)
        intent.putExtra("program", program)
        intent.putExtra("dcode", dcode)
        startActivity(intent)
    }

    fun onNotificationCardClicked(view: View) {
        // Handle click event for profile card view
        // For example, start a new activity
        val intent = Intent(this, NotificationActivity::class.java)
        intent.putExtra("regNo", regNo)
        startActivity(intent)
    }

    fun onLogoutCardClicked(view: View) {
        // Perform logout action here
        // For example, clear user session or perform logout API call

        // Navigate to the login page
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    inner class DatabaseTask(private val activity: HomeActivity) {

        fun execute() {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val resultSet = doInBackground()
                    activity.updateUI(resultSet)
                } catch (e: Exception) {
                    Log.e("DatabaseTask", "Error executing query: ${e.message}")
                    e.printStackTrace()
                }
            }
        }

        private fun doInBackground(): ResultSet? {
            val url = "jdbc:jtds:sqlserver://192.168.183.201/HRM_Personalinfo"
            val username = "sandy"
            val password = "s@ndy"
            val regNo = activity.regNo
            val query = """
                SELECT 
                    S.[RegNo], 
                    S.[StudentName], 
                    S.[DCode], 
                    S.[Batch], 
                    S.[Section], 
                    S.[Image],
                    C.[DeptName]
                FROM 
                    [dbo].[SIS_StudentInfo] S
                INNER JOIN 
                    (SELECT DISTINCT [CourseCode], [DeptName] FROM [dbo].[ADMIN_Course]) C 
                ON 
                    S.[DCode] = C.[CourseCode]
                WHERE 
                    S.[RegNo] = '$regNo'
            """.trimIndent()

            val connection = SqlConnectionHelper.connect(url, username, password) ?: return null
            return SqlConnectionHelper.executeQuery(connection, query)
        }
    }

    fun updateUI(resultSet: ResultSet?) {
        resultSet?.let {
            if (it.next()) {
                val regNo = it.getString("RegNo")
                val name = it.getString("StudentName")
                val sec = it.getString("Section")
                program = it.getString("DeptName")
                val batch = it.getString("Batch")
                val imageData = it.getBytes("Image") // Retrieve the image path from the database
                dcode=it.getString("Dcode")


                runOnUiThread {
                    // Set UI elements
                    findViewById<TextView>(R.id.Regno)?.text = regNo
                    findViewById<TextView>(R.id.Name)?.text = "Hi $name"
                    findViewById<TextView>(R.id.Prg)?.text = "$program - $sec"
                    findViewById<TextView>(R.id.Batch)?.text = batch

                    val profileImage: CircleImageView = findViewById(R.id.profile_image)
                    val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
                    profileImage.setImageBitmap(bitmap)
                }
            } else {
                // Handle case when no rows are returned
                Log.e("updateUI", "No rows in the ResultSet")
            }
        }
    }
}
