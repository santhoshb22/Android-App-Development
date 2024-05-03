package com.example.design

import android.os.AsyncTask
import android.os.StrictMode
import android.util.Log
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement

object SqlConnectionHelper {

    private var connection: Connection? = null

    fun connect(url: String, username: String, password: String): Connection? {
        if (connection != null) {
            return connection
        }

        try {
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().permitAll().build())

            Class.forName("net.sourceforge.jtds.jdbc.Driver")
            connection = DriverManager.getConnection(url, username, password)
        } catch (e: Exception) {
            e.printStackTrace()

            Log.e("SqlConnectionHelper", "Error connecting to the database", e)
        }
        return connection
    }

    fun executeQuery(connection: Connection?, query: String): ResultSet? {
        if (connection == null) {
            Log.e("SqlConnectionHelper", "Connection is null")
            return null
        }

        Log.d("SqlConnectionHelper", "Executing query: $query")

        return try {
            val statement: Statement = connection.createStatement()
            statement.executeQuery(query)
        } catch (e: SQLException) {
            Log.e("SqlConnectionHelper", "Error executing query", e)
            null
        }
    }


    fun closeConnection() {
        try {
            connection?.close()
        } catch (e: SQLException) {
            Log.e("SqlConnectionHelper", "Error closing connection", e)
        }
    }


}
