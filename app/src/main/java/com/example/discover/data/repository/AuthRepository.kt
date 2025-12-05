package com.example.discover.data.repository

import com.example.discover.utils.TokenManager
import java.util.UUID

class AuthRepository(private val tokenManager: TokenManager) {

    fun login(): String {
        val fakeToken = UUID.randomUUID().toString()
        tokenManager.saveToken(fakeToken)
        return fakeToken
    }

    fun getToken(): String? {
        return tokenManager.getToken()
    }

    fun logout() {
        tokenManager.clearToken()
    }
}