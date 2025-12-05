package com.example.discover.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.discover.data.repository.AuthRepository
import com.example.discover.data.repository.HomeRepository
import com.example.discover.models.Device
import kotlinx.coroutines.launch

class HomeViewModel(private val homeRepository: HomeRepository, private val authRepository: AuthRepository) : ViewModel() {

    private val _devices = MutableLiveData<List<Device>>()
    val devices: LiveData<List<Device>> = _devices

    private val _logoutComplete = MutableLiveData<Boolean>()
    val logoutComplete: LiveData<Boolean> = _logoutComplete

    fun getStoredDevices() {
        viewModelScope.launch {
            _devices.postValue(homeRepository.getStoredDevices())
        }
    }

    fun saveDevice(device: Device) {
        viewModelScope.launch {
            homeRepository.insertDevice(device)
            getStoredDevices()
        }
    }

    fun updateAllDevicesToOffline() {
        viewModelScope.launch {
            homeRepository.updateAllDevicesStatus(false)
            getStoredDevices()
        }
    }

    fun findAndupdateDevice(serviceName: String, hostAddress: String) {
        viewModelScope.launch {
            val device = homeRepository.findDeviceByName(serviceName)
            if (device != null) {
                device.status = true
                device.ip = hostAddress
                homeRepository.insertDevice(device)
            } else {
                homeRepository.insertDevice(Device(name = serviceName, ip = hostAddress, status = true))
            }
            getStoredDevices()
        }
    }

    fun deviceLost(serviceName: String) {
        viewModelScope.launch {
            homeRepository.updateDeviceStatus(serviceName, false)
            getStoredDevices()
        }
    }

    fun logout() {
        authRepository.logout()
        _logoutComplete.value = true
    }
}