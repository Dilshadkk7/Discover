package com.example.discover.ui.detail

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.example.discover.data.repository.DetailsRepository
import com.example.discover.databinding.ActivityDetailsBinding
import com.example.discover.utils.ViewModelFactory

class DetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailsBinding
    private lateinit var viewModel: DetailsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repository = DetailsRepository()
        viewModel = ViewModelProvider(this, ViewModelFactory(repository)).get(DetailsViewModel::class.java)

        viewModel.ipAddress.observe(this) {
            binding.ipAddress.text = it
        }

        viewModel.geoInfo.observe(this) {
            binding.geoInfo.text = it
        }

        viewModel.fetchPublicIp()
    }
}