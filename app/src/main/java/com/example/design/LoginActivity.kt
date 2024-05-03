package com.example.design

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*

class LoginActivity : AppCompatActivity() {

    private lateinit var regNoEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize UI elements
        regNoEditText = findViewById(R.id.regNoEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)

        // Set click listener for login button
        loginButton.setOnClickListener {
            // Retrieve entered registration number and password
            val regNo = regNoEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Start a coroutine to perform database operation asynchronously
            CoroutineScope(Dispatchers.Main).launch {
                val isValid = withContext(Dispatchers.IO) {
                    // Validate login using com.example.design.DatabaseHandler
                    val dbHandler = DatabaseHandler()
                    dbHandler.validateLogin(regNo, password)
                }

                // Check if login is valid
                if (isValid) {
                    // Login successful, navigate to next activity (e.g., HomeActivity)
                    val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                    intent.putExtra("regNo", regNoEditText.text.toString())
                    startActivity(intent)
                    finish() // Optional: Close the com.example.design.LoginActivity to prevent user from navigating back
                } else {
                    // Invalid login, display error message
                    Toast.makeText(this@LoginActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
