package com.automotive.carsettings.client.ui.display

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.automotive.carsettings.client.databinding.FragmentDisplaySettingsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Display settings fragment for brightness, theme, and other display options.
 */
@AndroidEntryPoint
class DisplaySettingsFragment : Fragment() {

    private var _binding: FragmentDisplaySettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DisplaySettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDisplaySettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeState()
        setupListeners()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.displayState.collect { state ->
                binding.apply {
                    // Update brightness slider (without triggering listener)
                    if (!seekbarBrightness.isPressed) {
                        seekbarBrightness.progress = state.brightness
                    }
                    textBrightnessValue.text = "${state.brightness}"

                    // Update auto brightness switch
                    switchAutoBrightness.isChecked = state.autoBrightness

                    // Update night mode switch
                    switchNightMode.isChecked = state.nightMode

                    // Update theme radio buttons
                    when (state.theme) {
                        0 -> radiobuttonLight.isChecked = true
                        1 -> radiobuttonDark.isChecked = true
                        2 -> radiobuttonAuto.isChecked = true
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.seekbarBrightness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    binding.textBrightnessValue.text = "$progress"
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    viewModel.setBrightness(it.progress)
                }
            }
        })

        binding.switchAutoBrightness.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setAutoBrightness(isChecked)
        }

        binding.switchNightMode.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setNightMode(isChecked)
        }

        binding.radiogroupTheme.setOnCheckedChangeListener { _, checkedId ->
            val theme = when (checkedId) {
                binding.radiobuttonLight.id -> 0
                binding.radiobuttonDark.id -> 1
                binding.radiobuttonAuto.id -> 2
                else -> 0
            }
            viewModel.setTheme(theme)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
