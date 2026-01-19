package com.automotive.carsettings.client.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.automotive.carsettings.client.R
import com.automotive.carsettings.client.databinding.ActivityMainBinding
import com.automotive.carsettings.client.manager.CarSettingsManager
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Main activity for the Car Settings Client application.
 * Handles service connection and navigation.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var carSettingsManager: CarSettingsManager

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        observeConnectionState()

        // Connect to service
        carSettingsManager.connect()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)
    }

    private fun observeConnectionState() {
        lifecycleScope.launch {
            carSettingsManager.connectionState.collect { state ->
                binding.connectionStatusBar.setState(state)

                when (state) {
                    is CarSettingsManager.ConnectionState.Connected -> {
                        Timber.d("Connected to service")
                    }
                    is CarSettingsManager.ConnectionState.Error -> {
                        Snackbar.make(
                            binding.root,
                            "Service connection error: ${state.message}",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                    else -> {
                        // Handle other states
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            carSettingsManager.disconnect()
        }
    }
}
