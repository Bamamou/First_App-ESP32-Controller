package com.smarthome.esp32controller

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ModernBluetoothDeviceAdapter(
    private val onDeviceClick: (BluetoothDevice) -> Unit
) : RecyclerView.Adapter<ModernBluetoothDeviceAdapter.DeviceViewHolder>() {

    private val devices = mutableListOf<BluetoothDevice>()
    private var selectedPosition = -1

    fun addDevice(device: BluetoothDevice) {
        if (!devices.any { it.address == device.address }) {
            devices.add(device)
            notifyItemInserted(devices.size - 1)
        }
    }

    fun clearDevices() {
        devices.clear()
        selectedPosition = -1
        notifyDataSetChanged()
    }

    fun getDevice(position: Int): BluetoothDevice? {
        return if (position in 0 until devices.size) devices[position] else null
    }

    fun setSelectedPosition(position: Int) {
        val oldPosition = selectedPosition
        selectedPosition = position
        if (oldPosition != -1) notifyItemChanged(oldPosition)
        if (selectedPosition != -1) notifyItemChanged(selectedPosition)
    }

    fun getDeviceCount(): Int = devices.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bluetooth_device, parent, false)
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(devices[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = devices.size

    inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDeviceName: TextView = itemView.findViewById(R.id.tvDeviceName)
        private val tvDeviceAddress: TextView = itemView.findViewById(R.id.tvDeviceAddress)
        private val tvDeviceStatus: TextView = itemView.findViewById(R.id.tvDeviceStatus)
        private val tvDeviceType: TextView = itemView.findViewById(R.id.tvDeviceType)
        private val selectionIndicator: View = itemView.findViewById(R.id.selectionIndicator)

        fun bind(device: BluetoothDevice, isSelected: Boolean) {
            // Device name with fallback
            val deviceName = try {
                device.name ?: "Unknown Device"
            } catch (e: SecurityException) {
                "Unknown Device"
            }
            
            tvDeviceName.text = deviceName
            tvDeviceAddress.text = device.address
            
            // Determine device type and status
            when {
                deviceName.contains("ESP32", ignoreCase = true) -> {
                    tvDeviceType.text = "ESP32 Controller"
                    tvDeviceStatus.text = "Compatible"
                    tvDeviceStatus.setTextColor(itemView.context.getColor(R.color.success_green))
                }
                deviceName.contains("HC-", ignoreCase = true) -> {
                    tvDeviceType.text = "Bluetooth Module"
                    tvDeviceStatus.text = "Compatible"
                    tvDeviceStatus.setTextColor(itemView.context.getColor(R.color.success_green))
                }
                else -> {
                    tvDeviceType.text = "Unknown Device"
                    tvDeviceStatus.text = "Unknown"
                    tvDeviceStatus.setTextColor(itemView.context.getColor(R.color.text_hint))
                }
            }

            // Selection state
            selectionIndicator.visibility = if (isSelected) View.VISIBLE else View.INVISIBLE
            selectionIndicator.isSelected = isSelected

            // Click handler
            itemView.setOnClickListener {
                onDeviceClick(device)
                setSelectedPosition(bindingAdapterPosition)
            }
        }
    }
}
