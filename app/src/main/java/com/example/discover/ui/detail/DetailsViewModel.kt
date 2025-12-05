package com.example.discover.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.discover.data.repository.DetailsRepository
import kotlinx.coroutines.launch

class DetailsViewModel(private val repository: DetailsRepository) : ViewModel() {

    private val _ipAddress = MutableLiveData<String>()
    val ipAddress: LiveData<String> = _ipAddress

    private val _geoInfo = MutableLiveData<String>()
    val geoInfo: LiveData<String> = _geoInfo

    fun fetchPublicIp() {
        viewModelScope.launch {
            try {
                val ip = repository.getPublicIp()
                _ipAddress.postValue(ip)
                fetchGeoInfo(ip)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun fetchGeoInfo(ip: String) {
        viewModelScope.launch {
            try {
                val info = repository.getGeoInfo(ip)
                _geoInfo.postValue(info)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}