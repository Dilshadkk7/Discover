package com.example.discover.ui.detail

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.discover.data.repository.DetailsRepository
import kotlinx.coroutines.launch

class DetailsViewModel(private val repository: DetailsRepository) : ViewModel() {

    private val _ipDetails = MutableLiveData<String>()
    val ipDetails: LiveData<String> = _ipDetails

    fun fetchIpDetails() {
        viewModelScope.launch {
            _ipDetails.postValue("Loading details...")
            try {
                val ip = repository.getPublicIp()
                val details = repository.getIpDetails(ip)
                _ipDetails.postValue(details)
            } catch (e: Exception) {
                Log.e("DetailsViewModel", "Full exception details:", e)
                val rootCause = e.cause ?: e
                val errorType = rootCause.javaClass.simpleName
                val errorMessage = rootCause.message ?: "No specific error message available."
                val fullMessage = "Could not load details.\n\nReason: $errorType\nMessage: $errorMessage"
                _ipDetails.postValue(fullMessage)
            }
        }
    }
}