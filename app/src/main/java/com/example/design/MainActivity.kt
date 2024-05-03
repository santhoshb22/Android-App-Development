package com.example.design
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*
import android.os.AsyncTask
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.Toast
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Timestamp
import java.util.concurrent.Executor
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {
    private lateinit var startDatePicker: DatePicker
    private lateinit var txtStartDate: EditText
    private lateinit var endDatePicker: DatePicker
    private lateinit var txtEndDate: EditText
    private lateinit var numberOfDaysTextView: TextView
    private var regNo: String = ""
    private var academicYear: String = ""
    private var program: String = ""
    private var selectedCid: String? = null // Declare selectedCid as a class-level variable



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        regNo = intent.getStringExtra("regNo") ?: ""
        startDatePicker = findViewById(R.id.startdatePicker)
        txtStartDate = findViewById(R.id.txtstartDate)
        endDatePicker = findViewById(R.id.lastdatePicker)
        txtEndDate = findViewById(R.id.txtlastDate)
        numberOfDaysTextView = findViewById(R.id.date)
        val databaseTask = DatabaseTask(this)
        databaseTask.execute()
        val submitButton: Button =findViewById(R.id.SubmitBtn)
        val yearSpinner: Spinner = findViewById(R.id.yearSpinner)
        val adapter1: ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(
            this,
            R.array.years_array,
            android.R.layout.simple_spinner_item
        )
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        yearSpinner.adapter = adapter1
        yearSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedYear = parent?.getItemAtPosition(position).toString()

                 academicYear = selectedYear
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle case when nothing is selected
            }
        }

        txtStartDate.setOnClickListener {
            if (startDatePicker.visibility == DatePicker.GONE) {
                startDatePicker.visibility = DatePicker.VISIBLE
            } else {
                startDatePicker.visibility = DatePicker.GONE
            }
        }

        startDatePicker.init(
            /* initialDate */ 2024, 2, 4,
            DatePicker.OnDateChangedListener { view, year, monthOfYear, dayOfMonth ->
                updateStartDate()
                updateNumberOfDays()
            }
        )

        txtEndDate.setOnClickListener {
            if (endDatePicker.visibility == DatePicker.GONE) {
                endDatePicker.visibility = DatePicker.VISIBLE
            } else {
                endDatePicker.visibility = DatePicker.GONE
            }
        }

        endDatePicker.init(
            /* initialDate */ 2024, 2, 4,
            DatePicker.OnDateChangedListener { view, year, monthOfYear, dayOfMonth ->
                updateEndDate()
                updateNumberOfDays()
            }
        )
        val cnameSpinner: Spinner = findViewById(R.id.cname)
        val retrieveDataTask = RetrieveDataFromTableTask(cnameSpinner)
        retrieveDataTask.setOnItemSelectedListener { selectedCid ->
            // Handle the selected CID here
            // For example:
            Log.d("SelectedCID", "Selected CID: $selectedCid")
        }
        // Start the coroutine
        CoroutineScope(Dispatchers.Main).launch {
            retrieveDataTask.execute()
        }
        submitButton.setOnClickListener {
            // Call the setupSubmitButton function when the button is clicked
            setupSubmitButton()
        }

    }



    private fun getCurrentDateTime(): Date {
        return Calendar.getInstance().time
    }

    private fun formatDateTime(dateTime: Date): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(dateTime)
    }



    private fun updateStartDate() {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val startDateString =
            "${startDatePicker.dayOfMonth}/${startDatePicker.month + 1}/${startDatePicker.year}"
        txtStartDate.setText(startDateString)
    }

    private fun updateEndDate() {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val endDateString =
            "${endDatePicker.dayOfMonth}/${endDatePicker.month + 1}/${endDatePicker.year}"
        txtEndDate.setText(endDateString)
    }

    private fun updateNumberOfDays() {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val startDateString =
            "${startDatePicker.dayOfMonth}/${startDatePicker.month + 1}/${startDatePicker.year}"
        val endDateString =
            "${endDatePicker.dayOfMonth}/${endDatePicker.month + 1}/${endDatePicker.year}"
        val startDate = sdf.parse(startDateString)
        val endDate = sdf.parse(endDateString)
        val difference = endDate.time - startDate.time
        val daysDifference = (difference / (1000 * 60 * 60 * 24)).toInt()
        numberOfDaysTextView.text = "$daysDifference"
    }


    // AsyncTask to execute database operation in background
    // Inside your MainActivity class
// AsyncTask to execute database operation in the background
    // Inside your MainActivity class
// AsyncTask to execute database operation in the background
    inner class RetrieveDataFromTableTask(private val spinner: Spinner) {
        private var onItemSelectedListener: ((String?) -> Unit)? = null

        fun setOnItemSelectedListener(listener: (String?) -> Unit) {
            this.onItemSelectedListener = listener
        }

        suspend fun execute() {
            withContext(Dispatchers.IO) {
                var connection: Connection? = null
                val data = mutableListOf<Pair<String, String>>()

                try {
                    Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance()
                    connection = DriverManager.getConnection(
                        "jdbc:jtds:sqlserver://192.168.183.201/student",
                        "sandy",
                        "s@ndy"
                    )

                    val statement = connection.createStatement()
                    val resultSet =
                        statement.executeQuery("SELECT cname, cid FROM [student].[dbo].[ipt_master]")

                    while (resultSet.next()) {
                        val cname = resultSet.getString("cname")
                        val cid = resultSet.getString("cid")
                        data.add(Pair(cname, cid))
                    }
                } catch (e: SQLException) {
                    e.printStackTrace()
                } finally {
                    try {
                        connection?.close()
                    } catch (e: SQLException) {
                        e.printStackTrace()
                    }
                }

                withContext(Dispatchers.Main) {
                    // Populate the Spinner with retrieved cnameList
                    val cnameList = data.map { pair -> pair.first }
                    val adapter =
                        ArrayAdapter(spinner.context, android.R.layout.simple_spinner_item, cnameList)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner.adapter = adapter

                    // Handle Spinner item selection to retrieve cid
                    spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            this@MainActivity.selectedCid = data[position].second
                            onItemSelectedListener?.invoke(selectedCid)
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {
                            // Handle case when nothing is selected
                        }
                    }
                }
            }
        }
    }



    // Inside your MainActivity class

    // Define a function to insert values into the iptstudent table
    private fun insertIntoIptStudentTable(regNo: String, fromDate: String, toDate: String, numberOfDays: Int, academicYear: String, enterBy: String , selectedCid: String) {
        Log.d("Insertion", "Attempting to insert data into the database.")

        AsyncTask.execute {

            var connection: Connection? = null

            try {
                // Connect to your database
                Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance()
                connection = DriverManager.getConnection(
                    "jdbc:jtds:sqlserver://192.168.183.201/student",
                    "sandy",
                    "s@ndy"
                )

                // Prepare the SQL INSERT query
                val query = "INSERT INTO [student].[dbo].[iptstudent] " +
                        "(iptcid, regno, from_date, to_date, noofdays, academic_year, status, enterby, enter_datetime) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"

                // Generate iptcid (assuming it's an auto-generated column)

                // Create a PreparedStatement
                val preparedStatement = connection.prepareStatement(query)
                preparedStatement.setString(1, selectedCid)
                preparedStatement.setString(2, regNo)
                preparedStatement.setDate(3, java.sql.Date.valueOf(formatDateForSQL(fromDate)))
                preparedStatement.setDate(4, java.sql.Date.valueOf(formatDateForSQL(toDate)))
                preparedStatement.setInt(5, numberOfDays)
                preparedStatement.setString(6, academicYear)
                preparedStatement.setInt(7, 0) // Default status as 0
                preparedStatement.setString(8, enterBy)
                preparedStatement.setTimestamp(9, java.sql.Timestamp.valueOf(formatDateTime(getCurrentDateTime()))) // Assuming you want to enter the current date and time

                // Execute the INSERT query
                val rowsAffected = preparedStatement.executeUpdate()

                if (rowsAffected > 0) {
                    Log.d("Insertion", "Data inserted succesfully.")
                } else {
                    Log.d("Insertion", "Failed to insert data.")
                }


                // Close the PreparedStatement
                preparedStatement.close()
            } catch (e: SQLException) {
                e.printStackTrace()
            } finally {
                try {
                    connection?.close()
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun formatDateForSQL(dateString: String): String {
        val parts = dateString.split("/")
        return "${parts[2]}-${parts[1]}-${parts[0]}"
    }



    private fun setupSubmitButton() {
        Log.d("SubmitButton", "Setting up submit button.")

        // Check if selectedCid is not null or empty
        if (!selectedCid.isNullOrEmpty()) {
            // Get the current date and time
            val currentDateTime = getCurrentDateTime()

            // Format the date and time as needed
            val formattedDateTime = formatDateTime(currentDateTime)

            // Log the formatted date and time
            Log.d("DateTime", "Current Date and Time: $formattedDateTime")

            val fromDate = txtStartDate.text.toString()
            val toDate = txtEndDate.text.toString()
            val numberOfDays = numberOfDaysTextView.text.toString().toInt()

            // Insert values into iptstudent table
            insertIntoIptStudentTable(regNo, fromDate, toDate, numberOfDays, academicYear, regNo,
                selectedCid!!
            )

            val intent = Intent(this, MainActivity2::class.java)
            startActivity(intent)
        } else {
            // Handle the case when selectedCid is null or empty
            Log.d("CID", "Selected CID is null or empty")
            // For example, show a message to the user or take appropriate action
        }
    }

    inner class DatabaseTask(private val activity: MainActivity) {

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



















