package com.example.discover.data.repository

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class DetailsRepository {

    suspend fun getPublicIp(): String {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("https://api.ipify.org?format=json")
                val connection = url.openConnection() as HttpURLConnection
                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    JSONObject(response).getString("ip")
                } else {
                    throw IOException("Failed to fetch public IP. Response code: ${connection.responseCode}")
                }
            } catch (e: Exception) {
                Log.e("DetailsRepository", "Error getting public IP", e)
                throw IOException("Could not get public IP.", e)
            }
        }
    }

    suspend fun getIpDetails(ip: String): String {
        return try {
            val ipInfoJson = getGeoInfo(ip)
            val jsonObject = JSONObject(ipInfoJson)
            if (jsonObject.has("error")) {
                val errorObject = jsonObject.getJSONObject("error")
                val errorTitle = errorObject.optString("title", "API Error")
                val errorMessage = errorObject.optString("message", "No details provided.")
                "Could not load details: $errorTitle - $errorMessage"
            } else {
                val city = jsonObject.optString("city", "N/A")
                val region = jsonObject.optString("region", "N/A")
                val country = jsonObject.optString("country", "N/A")
                "City: $city\nRegion: $region\nCountry: $country"
            }
        } catch (e: Exception) {
            Log.e("DetailsRepository", "Error getting IP details", e)
            val errorMessage = e.message ?: "An unknown error occurred while parsing the response."
            "Error: $errorMessage"
        }
    }

    private suspend fun getGeoInfo(ip: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("https://ipinfo.io/$ip/geo")
                val connection = url.openConnection() as HttpURLConnection
                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    connection.inputStream.bufferedReader().use { it.readText() }
                } else {
                    val errorStream = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "No error body"
                    throw IOException("Failed to fetch geo info. Response code: ${connection.responseCode}. Body: $errorStream")
                }
            } catch (e: Exception) {
                Log.e("DetailsRepository", "Error in getGeoInfo", e)
                throw e
            }
        }
    }
}