package com.example.discover.ui.home

import android.content.Intent
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
    private val serviceType = "_airplay._tcp."

    // Periodic Refresh Logic
    private val handler = Handler(Looper.getMainLooper())
    private val stopDiscoveryRunnable = Runnable { stopDiscovery() }
    private val refreshInterval = 15000L // 15 seconds
    private var isPausing = false
    private val foundDevicesInCycle = mutableMapOf<String, String>()

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

        nsdManager = getSystemService(NsdManager::class.java)

        binding.logoutButton.setOnClickListener {
            viewModel.logout()
        }
    }

    private fun startDiscovery() {
        Log.d("HomeActivity", "Starting new discovery cycle.")
        foundDevicesInCycle.clear()
        try {
            nsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        } catch (e: Exception) {
            Log.e("HomeActivity", "Error starting discovery.", e)
        }
    }

    private fun stopDiscovery() {
        Log.d("HomeActivity", "Stopping discovery cycle.")
        handler.removeCallbacks(stopDiscoveryRunnable)
        try {
            nsdManager.stopServiceDiscovery(discoveryListener)
        } catch (e: Exception) {
            Log.e("HomeActivity", "Error stopping discovery.", e)
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
            Log.d("HomeActivity", "Service discovery started.")
            handler.postDelayed(stopDiscoveryRunnable, refreshInterval)
        }

        override fun onServiceFound(service: NsdServiceInfo) {
            Log.d("HomeActivity", "Service found: $service")
            nsdManager.resolveService(service, resolveListener)
        }

        override fun onServiceLost(service: NsdServiceInfo) {
            // This is unreliable, but still log it.
            Log.d("HomeActivity", "Service lost event (unreliable): $service")
        }

        override fun onDiscoveryStopped(serviceType: String) {
            Log.i("HomeActivity", "Discovery stopped. Processing ${foundDevicesInCycle.size} found devices.")
            viewModel.processFoundDevices(foundDevicesInCycle) {
                if (!isPausing) {
                    startDiscovery()
                }
            }
        }

        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e("HomeActivity", "Start Discovery failed: Error code:$errorCode")
            stopDiscovery()
        }

        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e("HomeActivity", "Stop Discovery failed: Error code:$errorCode")
        }
    }

    private val resolveListener = object : NsdManager.ResolveListener {
        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            Log.e("HomeActivity", "Resolve failed: Error code: $errorCode")
        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
            Log.d("HomeActivity", "Resolve Succeeded for ${serviceInfo.serviceName}")
            val hostAddress = if (Build.VERSION.SDK_INT >= 34) {
                serviceInfo.hostAddresses.firstOrNull()?.hostAddress
            } else {
                @Suppress("DEPRECATION")
                serviceInfo.host?.hostAddress
            }

            if (hostAddress != null) {
                foundDevicesInCycle[serviceInfo.serviceName] = hostAddress
            } else {
                Log.e("HomeActivity", "Could not get host address for ${serviceInfo.serviceName}")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("HomeActivity", "onResume: Starting discovery cycle.")
        isPausing = false
        startDiscovery()
    }

    override fun onPause() {
        super.onPause()
        Log.d("HomeActivity", "onPause: Stopping discovery cycle.")
        isPausing = true
        stopDiscovery()
    }
}