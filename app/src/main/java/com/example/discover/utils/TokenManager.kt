package com.example.discover.utils

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString("oauth_token", token).apply()
    }

    fun getToken(): String? {
        return prefs.getString("oauth_token", null)
    }

    fun clearToken() {
        prefs.edit().remove("oauth_token").apply()
    }
}