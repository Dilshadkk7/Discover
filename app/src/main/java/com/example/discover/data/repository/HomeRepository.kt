package com.example.discover.data.repository

import com.example.discover.data.local.DeviceDao
import com.example.discover.models.Device

class HomeRepository(private val deviceDao: DeviceDao) {

    suspend fun getStoredDevices() = deviceDao.getDevices()

    suspend fun insertDevice(device: Device) = deviceDao.insertDevice(device)

    suspend fun updateAllDevicesStatus(status: Boolean) = deviceDao.updateAllDevicesStatus(status)

    suspend fun updateDeviceStatus(name: String, status: Boolean) = deviceDao.updateDeviceStatus(name, status)

    suspend fun findDeviceByName(name: String) = deviceDao.findDeviceByName(name)
}