package com.example.carsettings.client

import android.os.Bundle
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.carsettings.client.databinding.ActivityMainBinding
import com.example.carsettings.constants.AreaConstants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: CarSettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        observeUiState()
    }

    private fun setupListeners() {
        binding.brightnessSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.brightnessValue.text = progress.toString()
                if (fromUser) {
                    viewModel.setBrightness(progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.tempUpDriver.setOnClickListener {
            val currentTemp = viewModel.uiState.value.hvacTempDriver
            viewModel.setHvacTemp(AreaConstants.SEAT_DRIVER, currentTemp + 0.5f)
        }

        binding.tempDownDriver.setOnClickListener {
            val currentTemp = viewModel.uiState.value.hvacTempDriver
            viewModel.setHvacTemp(AreaConstants.SEAT_DRIVER, currentTemp - 0.5f)
        }

        binding.nightModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (viewModel.uiState.value.isNightMode != isChecked) {
                viewModel.setNightMode(isChecked)
            }
        }
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.statusText.text = if (state.isConnected) "Status: Connected" else "Status: Disconnected"
                    binding.brightnessSeekBar.progress = state.brightness
                    binding.brightnessValue.text = state.brightness.toString()
                    binding.tempValueDriver.text = String.format("%.1f", state.hvacTempDriver)
                    binding.nightModeSwitch.isChecked = state.isNightMode
                }
            }
        }
    }
}
