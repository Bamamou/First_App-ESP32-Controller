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

    fun updateDevices(newDevices: List<BluetoothDevice>) {
        devices.clear()
        devices.addAll(newDevices)
        selectedPosition = -1
        notifyDataSetChanged()
    }

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
