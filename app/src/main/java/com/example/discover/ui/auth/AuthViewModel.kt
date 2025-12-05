package com.example.discover.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.discover.data.repository.AuthRepository

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _authenticationState = MutableLiveData<AuthenticationState>()
    val authenticationState: LiveData<AuthenticationState> = _authenticationState

    fun checkSilentAuth(isNetworkAvailable: Boolean) {
        val token = repository.getToken()
        if (token != null) {
            if (isNetworkAvailable) {
                _authenticationState.value = AuthenticationState.Authenticated
            } else {
                repository.logout()
                _authenticationState.value = AuthenticationState.AuthenticationFailed("No network. You have been logged out.")
            }
        } else {
            _authenticationState.value = AuthenticationState.Unauthenticated
        }
    }

    fun login() {
        repository.login()
        _authenticationState.value = AuthenticationState.Authenticated
    }
}

sealed class AuthenticationState {
    object Authenticated : AuthenticationState()
    object Unauthenticated : AuthenticationState()
    data class AuthenticationFailed(val message: String) : AuthenticationState()
}