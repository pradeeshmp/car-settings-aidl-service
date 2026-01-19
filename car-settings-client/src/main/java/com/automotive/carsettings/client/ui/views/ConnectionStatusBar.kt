package com.automotive.carsettings.client.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.automotive.carsettings.client.databinding.ViewConnectionStatusBarBinding
import com.automotive.carsettings.client.manager.CarSettingsManager

/**
 * Custom view for displaying connection status with reconnect button.
 */
class ConnectionStatusBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: ViewConnectionStatusBarBinding

    init {
        binding = ViewConnectionStatusBarBinding.inflate(LayoutInflater.from(context), this, true)
    }

    fun setState(state: CarSettingsManager.ConnectionState) {
        binding.apply {
            when (state) {
                is CarSettingsManager.ConnectionState.Connected -> {
                    textStatus.text = "Connected"
                    textStatus.setTextColor(context.getColor(android.R.color.holo_green_dark))
                    buttonReconnect.visibility = GONE
                }
                is CarSettingsManager.ConnectionState.Connecting -> {
                    textStatus.text = "Connecting..."
                    textStatus.setTextColor(context.getColor(android.R.color.holo_orange_dark))
                    buttonReconnect.visibility = GONE
                }
                is CarSettingsManager.ConnectionState.Disconnected -> {
                    textStatus.text = "Disconnected"
                    textStatus.setTextColor(context.getColor(android.R.color.holo_red_dark))
                    buttonReconnect.visibility = VISIBLE
                }
                is CarSettingsManager.ConnectionState.Error -> {
                    textStatus.text = "Error: ${state.message}"
                    textStatus.setTextColor(context.getColor(android.R.color.holo_red_dark))
                    buttonReconnect.visibility = VISIBLE
                }
            }
        }
    }

    fun setOnReconnectClickListener(listener: OnClickListener) {
        binding.buttonReconnect.setOnClickListener(listener)
    }
}
