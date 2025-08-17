package com.example.testing

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class BluetoothDeviceAdapter(private val context: Context) : BaseAdapter() {
    
    private val devices = mutableListOf<BluetoothDevice>()
    
    fun addDevice(device: BluetoothDevice) {
        if (!devices.contains(device)) {
            devices.add(device)
            notifyDataSetChanged()
        }
    }
    
    fun clearDevices() {
        devices.clear()
        notifyDataSetChanged()
    }
    
    fun getDevice(position: Int): BluetoothDevice {
        return devices[position]
    }
    
    override fun getCount(): Int = devices.size
    
    override fun getItem(position: Int): Any = devices[position]
    
    override fun getItemId(position: Int): Long = position.toLong()
    
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(
            android.R.layout.simple_list_item_2, parent, false
        )
        
        val device = devices[position]
        val nameTextView = view.findViewById<TextView>(android.R.id.text1)
        val addressTextView = view.findViewById<TextView>(android.R.id.text2)
        
        nameTextView.text = device.name ?: context.getString(R.string.unknown_device)
        addressTextView.text = device.address
        
        return view
    }
}
