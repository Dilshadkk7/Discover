package com.example.discover.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.discover.databinding.ItemDeviceBinding
import com.example.discover.models.Device

class DeviceAdapter(private val devices: MutableList<Device>, private val listener: (Device) -> Unit) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
     DeviceViewHolder {
        val binding = ItemDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeviceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(devices[position])
    }

    override fun getItemCount() = devices.size

    inner class DeviceViewHolder(private val binding: ItemDeviceBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(device: Device) {
            binding.deviceName.text = device.name
            binding.deviceIp.text = device.ip
            binding.deviceStatus.text = if (device.status) "Online" else "Offline"
            itemView.setOnClickListener { listener(device) }
        }
    }
}