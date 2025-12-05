package com.example.discover.ui.auth

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.discover.data.repository.AuthRepository
import com.example.discover.databinding.ActivityAuthBinding
import com.example.discover.ui.home.HomeActivity
import com.example.discover.utils.TokenManager
import com.example.discover.utils.ViewModelFactory

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val tokenManager = TokenManager(this)
        val repository = AuthRepository(tokenManager)
        viewModel = ViewModelProvider(this, ViewModelFactory(repository)).get(AuthViewModel::class.java)

        viewModel.authenticationState.observe(this) { state ->
            when (state) {
                is AuthenticationState.Authenticated -> navigateToHome()
                is AuthenticationState.Unauthenticated -> { /* Stay on login screen */ }
                is AuthenticationState.AuthenticationFailed -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.loginButton.setOnClickListener {
            viewModel.login()
        }

        // Check for silent auth on start
        viewModel.checkSilentAuth(isNetworkAvailable())
    }

    private fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
}