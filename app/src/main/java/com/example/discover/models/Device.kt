package com.example.discover.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "devices")
data class Device(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    var ip: String,
    var status: Boolean = false
)