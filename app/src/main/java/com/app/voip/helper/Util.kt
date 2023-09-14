package com.app.voip.helper

import java.io.FileInputStream
import java.util.Properties

object Util {
    private const val PROPERTIES_FILE = "local.properties"

    //Get app id from local.properties file it is a safer way to get without
    //leaking it in public
    fun getAgoraAppID(): String {
        val properties = Properties()
        return try {
            val inputStream = FileInputStream(PROPERTIES_FILE)
            properties.load(inputStream)
            inputStream.close()
            properties.getProperty("agoraAppID") ?: ""
        } catch (e: Exception) {
            // Handle exceptions, e.g., file not found or property not found
            e.printStackTrace()
            "" // Return a default value or handle the error as needed
        }
    }

    //Get app certificate from local.properties file it is a safer way to get without
    //leaking it in public
    fun getAgoraAppCertificate(): String {
        val properties = Properties()
        return try {
            val inputStream = FileInputStream(PROPERTIES_FILE)
            properties.load(inputStream)
            inputStream.close()
            properties.getProperty("agoraAppCertificate") ?: ""
        } catch (e: Exception) {
            // Handle exceptions, e.g., file not found or property not found
            e.printStackTrace()
            "" // Return a default value or handle the error as needed
        }
    }
}