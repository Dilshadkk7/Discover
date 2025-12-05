package com.example.discover.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.discover.data.repository.AuthRepository
import com.example.discover.data.repository.HomeRepository
import com.example.discover.models.Device
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(private val homeRepository: HomeRepository, private val authRepository: AuthRepository) : ViewModel() {

    private val _devices = MutableLiveData<List<Device>>()
    val devices: LiveData<List<Device>> = _devices

    private val _logoutComplete = MutableLiveData<Boolean>()
    val logoutComplete: LiveData<Boolean> = _logoutComplete

    /**
     * Processes the list of devices found during a discovery cycle.
     * It now accepts an onComplete callback to signal when processing is finished.
     */
    fun processFoundDevices(foundDevices: Map<String, String>, onComplete: () -> Unit) {
        viewModelScope.launch {
            // Step 1: Mark all devices as offline
            homeRepository.updateAllDevicesStatus(false)

            // Step 2: Update or insert the devices that were found, marking them as online
            for ((name, ip) in foundDevices) {
                val device = homeRepository.findDeviceByName(name)
                if (device != null) {
                    device.status = true
                    device.ip = ip
                    homeRepository.insertDevice(device)
                } else {
                    homeRepository.insertDevice(Device(name = name, ip = ip, status = true))
                }
            }

            // Step 3: AFTER all updates are done, get the final list and update the UI
            val updatedDevices = homeRepository.getStoredDevices()
            _devices.postValue(updatedDevices)

            // Step 4: Signal that processing is complete
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    fun deviceLost(serviceName: String) {
        viewModelScope.launch {
            homeRepository.updateDeviceStatus(serviceName, false)
            val updatedDevices = homeRepository.getStoredDevices()
            _devices.postValue(updatedDevices)
        }
    }

    fun logout() {
        viewModelScope.launch {
            homeRepository.updateAllDevicesStatus(false)
            authRepository.logout()
            _logoutComplete.postValue(true)
        }
    }
}