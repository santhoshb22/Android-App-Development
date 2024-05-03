package com.example.design

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement

class DatabaseHandler {
    suspend fun validateLogin(regNo: String, password: String): Boolean {
        return withContext(Dispatchers.IO) {
            var conn: Connection? = null
            var stmt: Statement? = null
            var isValid = false
            try {
                // Register JDBC driver
                Class.forName("net.sourceforge.jtds.jdbc.Driver")

                // Open a connection
                conn = DriverManager.getConnection(DB_URL, USER, PASS)

                // Execute a query to retrieve the password for the given registration number
                stmt = conn.createStatement()
                val sql = "SELECT Password FROM SIS_StudentInfo WHERE regno = '$regNo'"
                val rs = stmt.executeQuery(sql)

                // If the query returns a row, retrieve the password and compare with the entered password
                if (rs.next()) {
                    val storedPassword = rs.getString("Password")
                    isValid = password == storedPassword
                }

                // Clean-up environment
                rs.close()
                stmt.close()
                conn.close()
            } catch (se: SQLException) {
                // Handle errors for JDBC
                se.printStackTrace()
            } catch (e: Exception) {
                // Handle other errors
                e.printStackTrace()
            } finally {
                // Finally block used to close resources
                try {
                    stmt?.close()
                } catch (se2: SQLException) {
                    // Ignore
                }
                try {
                    conn?.close()
                } catch (se: SQLException) {
                    se.printStackTrace()
                }
            }
            isValid
        }
    }

    companion object {

        private const val DB_URL = "jdbc:jtds:sqlserver://192.168.183.201/HRM_Personalinfo"
        private const val USER = "sandy"
        private const val PASS = "s@ndy"
    }
}
