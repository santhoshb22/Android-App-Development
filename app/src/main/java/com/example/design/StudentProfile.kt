
package com.example.design

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import de.hdodenhof.circleimageview.CircleImageView
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.lang.ref.WeakReference
import java.net.URL
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.target.CustomTarget
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class StudentProfile : AppCompatActivity() {

    private lateinit var duplicatedHorizontalItemsContainer: LinearLayout
    private lateinit var duplicatedRequiredSkillsContainer: LinearLayout
    private lateinit var activityReference: WeakReference<StudentProfile>

    private var newButtonId: Int = 0
    private var acquiredSkillsString: String = ""
    private var requiredSkillString: String = ""
    private var selectedOptions: String = ""
    private var mark10: Int = 0
    private var regNo: String = ""
    private var program: String = ""
    private var newAcquiredButtonClicked = false
    private var newRequiredSkillButtonClicked = false
    private var AcquiredButtonClicked = false
    private var RequiredButtonClicked = false

    private var submitButtonClicked = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_profie)
        regNo = intent.getStringExtra("regNo") ?: ""
        val databaseTask = DatabaseTask(this)
        databaseTask.execute()


        val initialSkillEditText: EditText = findViewById(R.id.skillEditText)
        val initialButton: Button = findViewById(R.id.initialButton)
        val initialSpinner: Spinner = findViewById(R.id.InitialSpinner)
        val requiredSkillEditText: EditText = findViewById(R.id.ReqSkill)
        activityReference = WeakReference(this)

        val addButton2: Button = findViewById(R.id.addButton2)

        duplicatedHorizontalItemsContainer = findViewById(R.id.duplicatedHorizontalItemsContainer)
        duplicatedRequiredSkillsContainer = findViewById(R.id.duplicatedRequiredSkillsContainer)

        val placementCheckBox: RadioButton = findViewById(R.id.placementRadioButton)
        val higherStudiesCheckBox: RadioButton = findViewById(R.id.higherStudiesRadioButton)
        val entrepreneurCheckBox: RadioButton = findViewById(R.id.entrepreneurRadioButton)

        val adapter: ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(
            this,
            R.array.skill_levels,
            android.R.layout.simple_spinner_item
        )


        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        initialSpinner.adapter = adapter

        initialButton.setOnClickListener {
            if (!AcquiredButtonClicked) {
                val initialSkillText = initialSkillEditText.text?.toString()?.trim()

                if (initialSkillText.isNullOrEmpty()) {
                    Toast.makeText(
                        this,
                        "Enter the initial skill before adding new items",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    addNewSkillSet()
                    AcquiredButtonClicked = true
                    initialButton.isEnabled = false
                }
            }
        }

        addButton2.setOnClickListener {

            if (!RequiredButtonClicked) {
                val requiredSkillText = requiredSkillEditText.text?.toString()?.trim()

                if (requiredSkillText.isNullOrEmpty()) {
                    Toast.makeText(
                        this,
                        "Enter a skill before adding new items",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {

                    addNewRequiredSkillSet()
                    RequiredButtonClicked = true
                    addButton2.isEnabled = false

                }
            }
        }
        placementCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(this, "Placement selected", Toast.LENGTH_SHORT).show()
            }
        }

        higherStudiesCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(this, "Higher Studies selected", Toast.LENGTH_SHORT).show()
            }
        }

        entrepreneurCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(this, "Entrepreneur selected", Toast.LENGTH_SHORT).show()
            }
        }

        val submitButton: Button = findViewById(R.id.SubmitBtn)
        submitButton.setOnClickListener {
            if (!submitButtonClicked) {

                handleSubmitButtonClick()
                submitButtonClicked = true
            }
        }
    }

    private fun addNewSkillSet() {
        val newHorizontalItems =
            LayoutInflater.from(this).inflate(R.layout.horizontal_item_layout, null)

        val skillSpinner: Spinner = newHorizontalItems.findViewById(R.id.YourSpinner)
        val adapter: ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(
            this,
            R.array.skill_levels,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        skillSpinner.adapter = adapter

        val newSkillEditText: EditText = newHorizontalItems.findViewById(R.id.skillEditText)

        newButtonId = View.generateViewId()

        val newAddButton: Button = newHorizontalItems.findViewById(R.id.addButton)
        newAddButton.id = newButtonId
        newAddButton.setBackgroundResource(R.drawable.custom_button)

        newAddButton.setOnClickListener {
            if (!newAddButton.isEnabled) {
                // This button has already been clicked, return
                return@setOnClickListener
            }

            if (newSkillEditText.text.isNullOrBlank()) {
                Toast.makeText(
                    this,
                    "Enter a skill before adding new items",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            } else {
                addNewSkillSet()
                newAddButton.isEnabled = false // Disable the button after it's clicked once
            }
        }

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.topMargin = resources.getDimensionPixelSize(R.dimen.gap_between_items)
        duplicatedHorizontalItemsContainer.addView(newHorizontalItems, layoutParams)
    }


    private fun handleSubmitButtonClick() {
        val mark10EditText: EditText = findViewById(R.id.Mark10)
        val mark10String: String = mark10EditText.text.toString().trim()

        try {
            mark10 = mark10String.toInt()

            if (mark10 > 500) {
                Toast.makeText(this, "10th Mark should be <= 500", Toast.LENGTH_SHORT).show()
                return
            }
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Invalid input for 10th Mark", Toast.LENGTH_SHORT).show()
            return
        }


        val initialSkillEditText: EditText = findViewById(R.id.skillEditText)
        val initialSkill: String = initialSkillEditText.text.toString().trim()
        if (TextUtils.isEmpty(initialSkill)) {
            Toast.makeText(this, "Enter Initial Skill", Toast.LENGTH_SHORT).show()
            return
        }

        val requiredSkillEditText: EditText = findViewById(R.id.ReqSkill)
        val requiredSkill: String = requiredSkillEditText.text.toString().trim()
        if (TextUtils.isEmpty(requiredSkill)) {
            Toast.makeText(this, "Enter Required Skill", Toast.LENGTH_SHORT).show()
            return
        }


        val optionsRadioGroup: RadioGroup = findViewById(R.id.optionsRadioGroup)

        if (optionsRadioGroup.checkedRadioButtonId == -1) {
            Toast.makeText(this, "Select at least one preference", Toast.LENGTH_SHORT).show()
            return
        }


        val initialSpinner: Spinner = findViewById(R.id.InitialSpinner)
        val initialSkillLevel: String = initialSpinner.selectedItem.toString()
        val mappedInitialSkillLevel: Int = mapSkillLevelToInt(initialSkillLevel)


        val initialSkillText = initialSkillEditText.text?.toString()?.trim()
        val requiredSkillText = requiredSkillEditText.text?.toString()?.trim()

        acquiredSkillsString += "$initialSkillText$mappedInitialSkillLevel, "
        requiredSkillString += "$requiredSkillText,"

        processDuplicatedSkills()
        processDuplicatedRequiredSkills()
        selectedOptions = processCheckBoxOptions()


        Log.d("SubmitButton", " mark10: $mark10")
        Log.d("SubmitButton", "Acquired Skills: $acquiredSkillsString")
        Log.d("SubmitButton", " requiredSkill: $requiredSkillString")
        Log.d("SubmitButton", " focus: $selectedOptions")


        val regNoFromStTable = regNo
        Log.d("SubmitButton", " regno: $regNoFromStTable")
        val insertQuery =
            "INSERT INTO student.dbo.st_data (regno, mark10, acquired_skills, required_skills, focus) VALUES ('$regNoFromStTable', $mark10, '$acquiredSkillsString', '$requiredSkillString', '$selectedOptions');"


        InsertTask(this, insertQuery).execute()


        openNewPage()

    }

    private fun processDuplicatedSkills() {

        for (i in 0 until duplicatedHorizontalItemsContainer.childCount) {
            val newHorizontalItems =
                duplicatedHorizontalItemsContainer.getChildAt(i) as LinearLayout

            val skillSpinner: Spinner = newHorizontalItems.findViewById(R.id.YourSpinner)
            val newSkillEditText: EditText = newHorizontalItems.findViewById(R.id.skillEditText)

            val skillLevel: String = skillSpinner.selectedItem.toString()
            val mappedSkillLevel: Int = mapSkillLevelToInt(skillLevel)

            val newSkillText = newSkillEditText.text?.toString()?.trim()

            acquiredSkillsString += "$newSkillText$mappedSkillLevel, "
        }
    }

    private fun processDuplicatedRequiredSkills() {

        for (i in 0 until duplicatedRequiredSkillsContainer.childCount) {
            val newRequiredSkillItems =
                duplicatedRequiredSkillsContainer.getChildAt(i) as LinearLayout

            val newRequiredSkillEditText: EditText =
                newRequiredSkillItems.findViewById(R.id.requiredSkillEditText)

            val newRequiredSkillText = newRequiredSkillEditText.text?.toString()?.trim()

            if (!newRequiredSkillText.isNullOrEmpty()) {
                requiredSkillString += "$newRequiredSkillText, "
            }
        }
    }

    private fun processCheckBoxOptions(): String {
        val selectedOptions = StringBuilder()

        val optionsRadioGroup: RadioGroup = findViewById(R.id.optionsRadioGroup)

        when (optionsRadioGroup.checkedRadioButtonId) {
            R.id.placementRadioButton -> selectedOptions.append("1")
            R.id.higherStudiesRadioButton -> selectedOptions.append("2")
            R.id.entrepreneurRadioButton -> selectedOptions.append("3")
        }

        return selectedOptions.toString()

    }


    private fun openNewPage() {

        val intent = Intent(this, MainActivity2::class.java)
        startActivity(intent)
    }

    private fun mapSkillLevelToInt(skillLevel: String): Int {
        return when (skillLevel) {
            "Beginner" -> 1
            "Intermediate" -> 2
            "Advanced" -> 3
            else -> 1
        }
    }

    private fun addNewRequiredSkillSet() {
        val newRequiredSkillItems =
            LayoutInflater.from(this).inflate(R.layout.required_horizontal_item_layout, null)

        val newRequiredSkillEditText: EditText =
            newRequiredSkillItems.findViewById(R.id.requiredSkillEditText)
        val newAddButton: Button = newRequiredSkillItems.findViewById(R.id.ReqButton)

        newAddButton.setOnClickListener {
            if (!newAddButton.isEnabled) {
                // This button has already been clicked, return
                return@setOnClickListener
            }

            val newRequiredSkillText = newRequiredSkillEditText.text?.toString()?.trim()
            if (!newRequiredSkillText.isNullOrEmpty()) {
                // Perform your actions here
                addNewRequiredSkillSet()
                newAddButton.isEnabled = false // Disable the button after it's clicked once
            } else {
                Toast.makeText(
                    this,
                    "Enter a required skill before adding new items",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.topMargin = resources.getDimensionPixelSize(R.dimen.gap_between_items)
        duplicatedRequiredSkillsContainer.addView(newRequiredSkillItems, layoutParams)
    }


    inner class DatabaseTask(private val activity: StudentProfile) {

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











    private class InsertTask(activity: StudentProfile, private val query: String) : AsyncTask<Void, Void, Boolean>() {
        private val activityReference: WeakReference<StudentProfile> = WeakReference(activity)
        private var successCallback: ((Boolean) -> Unit)? = null

        fun setSuccessCallback(callback: (Boolean) -> Unit) {
            successCallback = callback
        }

        override fun doInBackground(vararg params: Void?): Boolean {
            val activity = activityReference.get() ?: return false

            val url = "jdbc:jtds:sqlserver://192.168.183.201/student"
            val username = "sandy"
            val password = "s@ndy"
            var connection: Connection? = null

            return try {
                connection = SqlConnectionHelper.connect(url, username, password)
                if (connection != null) {
                    val statement = connection.createStatement()
                    val rowsAffected = statement.executeUpdate(query)
                    rowsAffected > 0
                } else {
                    false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                false
            } finally {
                SqlConnectionHelper.closeConnection()
            }
        }

        override fun onPostExecute(success: Boolean) {
            successCallback?.invoke(success)
        }
    }
}








