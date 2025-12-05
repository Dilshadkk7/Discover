package com.example.discover.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.discover.data.repository.AuthRepository
import com.example.discover.data.repository.DetailsRepository
import com.example.discover.data.repository.HomeRepository
import com.example.discover.ui.auth.AuthViewModel
import com.example.discover.ui.detail.DetailsViewModel
import com.example.discover.ui.home.HomeViewModel

class ViewModelFactory(private vararg val repositories: Any) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repository = repositories.firstOrNull()
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                val homeRepository = repositories.find { it is HomeRepository } as HomeRepository
                val authRepository = repositories.find { it is AuthRepository } as AuthRepository
                HomeViewModel(homeRepository, authRepository) as T
            }
            modelClass.isAssignableFrom(DetailsViewModel::class.java) && repository is DetailsRepository -> {
                DetailsViewModel(repository) as T
            }
            modelClass.isAssignableFrom(AuthViewModel::class.java) && repository is AuthRepository -> {
                AuthViewModel(repository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class or repository")
        }
    }
}