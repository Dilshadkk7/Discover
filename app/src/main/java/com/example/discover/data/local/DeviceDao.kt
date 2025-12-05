package com.example.discover.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.discover.models.Device

@Dao
interface DeviceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: Device): Long

    @Query("SELECT * FROM devices")
    suspend fun getDevices(): List<Device>

    @Query("UPDATE devices SET status = :status")
    suspend fun updateAllDevicesStatus(status: Boolean): Int

    @Query("UPDATE devices SET status = :status WHERE name = :name")
    suspend fun updateDeviceStatus(name: String, status: Boolean): Int

    @Query("SELECT * FROM devices WHERE name = :name LIMIT 1")
    suspend fun findDeviceByName(name: String): Device?
}