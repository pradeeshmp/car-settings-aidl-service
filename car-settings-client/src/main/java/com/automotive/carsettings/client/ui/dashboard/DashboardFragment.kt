package com.automotive.carsettings.client.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.automotive.carsettings.client.databinding.FragmentDashboardBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Dashboard fragment showing connection status and quick settings overview.
 */
@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeState()
        setupListeners()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.dashboardState.collect { state ->
                binding.apply {
                    textConnectionStatus.text = "Connection: ${state.connectionStatus}"
                    textServiceStatus.text = "Service: ${state.serviceStatus}"
                    textPropertyCount.text = "Properties: ${state.propertyCount}"
                    textBrightness.text = "Brightness: ${state.brightness}"
                    textMediaVolume.text = "Media Volume: ${state.mediaVolume}"
                    textDriverTemp.text = "Driver Temp: ${state.driverTemperature}°C"
                }
            }
        }
    }

    private fun setupListeners() {
        binding.buttonRefresh.setOnClickListener {
            viewModel.refresh()
        }

        binding.buttonDumpState.setOnClickListener {
            viewModel.dumpServiceState()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
