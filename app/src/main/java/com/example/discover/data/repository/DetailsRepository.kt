package com.example.discover.data.repository

import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class DetailsRepository {

    suspend fun getPublicIp(): String {
        val url = URL("https://api.ipify.org?format=json")
        val connection = url.openConnection() as HttpURLConnection
        try {
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                return JSONObject(response).getString("ip")
            } else {
                throw IOException("Failed to fetch public IP. Response code: ${connection.responseCode}")
            }
        } finally {
            connection.disconnect()
        }
    }

    suspend fun getGeoInfo(ip: String): String {
        val url = URL("https://ipinfo.io/$ip/geo")
        val connection = url.openConnection() as HttpURLConnection
        try {
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                return connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                throw IOException("Failed to fetch geo info. Response code: ${connection.responseCode}")
            }
        } finally {
            connection.disconnect()
        }
    }
}