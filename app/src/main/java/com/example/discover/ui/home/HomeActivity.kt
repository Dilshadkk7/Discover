package com.example.discover.ui.home

import android.content.Context
import android.content.Intent
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.discover.data.local.AppDatabase
import com.example.discover.data.repository.AuthRepository
import com.example.discover.data.repository.HomeRepository
import com.example.discover.databinding.ActivityHomeBinding
import com.example.discover.models.Device
import com.example.discover.ui.auth.AuthActivity
import com.example.discover.ui.detail.DetailsActivity
import com.example.discover.utils.TokenManager
import com.example.discover.utils.ViewModelFactory

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var deviceAdapter: DeviceAdapter
    private lateinit var nsdManager: NsdManager
    private val devices = mutableListOf<Device>()
    private val SERVICE_TYPE = "_airplay._tcp."

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val tokenManager = TokenManager(this)
        val homeRepository = HomeRepository(AppDatabase.getDatabase(this).deviceDao())
        val authRepository = AuthRepository(tokenManager)
        viewModel = ViewModelProvider(this, ViewModelFactory(homeRepository, authRepository)).get(HomeViewModel::class.java)

        setupRecyclerView()
        binding.progressBar.visibility = View.VISIBLE
        binding.emptyView.visibility = View.GONE
        binding.devicesRecyclerView.visibility = View.GONE


        viewModel.devices.observe(this) { deviceList ->
            binding.progressBar.visibility = View.GONE
            if (deviceList.isEmpty()) {
                binding.emptyView.visibility = View.VISIBLE
                binding.devicesRecyclerView.visibility = View.GONE
            } else {
                binding.emptyView.visibility = View.GONE
                binding.devicesRecyclerView.visibility = View.VISIBLE
            }
            devices.clear()
            devices.addAll(deviceList)
            deviceAdapter.notifyDataSetChanged()
        }

        viewModel.logoutComplete.observe(this) { isComplete ->
            if (isComplete) {
                val intent = Intent(this, AuthActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        nsdManager = getSystemService(Context.NSD_SERVICE) as NsdManager

        binding.logoutButton.setOnClickListener {
            viewModel.logout()
        }
    }

    private fun setupRecyclerView() {
        deviceAdapter = DeviceAdapter(devices) { device ->
            val intent = Intent(this, DetailsActivity::class.java)
            intent.putExtra("device_ip", device.ip)
            startActivity(intent)
        }
        binding.devicesRecyclerView.apply {
            adapter = deviceAdapter
            layoutManager = LinearLayoutManager(this@HomeActivity)
        }
    }

    private val discoveryListener = object : NsdManager.DiscoveryListener {
        override fun onDiscoveryStarted(regType: String) {
            Log.d("HomeActivity", "Service discovery started")
        }

        override fun onServiceFound(service: NsdServiceInfo) {
            Log.d("HomeActivity", "Service found: $service")
            nsdManager.resolveService(service, resolveListener)
        }

        override fun onServiceLost(service: NsdServiceInfo) {
            Log.e("HomeActivity", "Service lost: $service")
            viewModel.deviceLost(service.serviceName)
        }

        override fun onDiscoveryStopped(serviceType: String) {
            Log.i("HomeActivity", "Discovery stopped: $serviceType")
        }

        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e("HomeActivity", "Discovery failed: Error code:$errorCode")
            nsdManager.stopServiceDiscovery(this)
        }

        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e("HomeActivity", "Discovery failed: Error code:$errorCode")
            nsdManager.stopServiceDiscovery(this)
        }
    }

    private val resolveListener = object : NsdManager.ResolveListener {
        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            Log.e("HomeActivity", "Resolve failed: Error code: $errorCode")
        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
            Log.d("HomeActivity", "Resolve Succeeded. $serviceInfo")
            viewModel.findAndupdateDevice(serviceInfo.serviceName, serviceInfo.host.hostAddress)
        }
    }

    override fun onResume() {
        super.onResume()
        // Every time the app is resumed, mark all devices as offline and then
        // start a fresh discovery to find currently online devices.
        viewModel.updateAllDevicesToOffline()
        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
    }

    override fun onPause() {
        super.onPause()
        nsdManager.stopServiceDiscovery(discoveryListener)
    }
}