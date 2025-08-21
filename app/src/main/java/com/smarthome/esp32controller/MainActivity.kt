package com.smarthome.esp32controller

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException
import java.io.OutputStream
import java.util.*

class MainActivity : AppCompatActivity() {
    
    private lateinit var tvConnectionStatus: TextView
    private lateinit var btnScan: Button
    private lateinit var btnConnect: Button
    private lateinit var statusIndicator: android.view.View
    
    // Modern toggle switches for room control
    private lateinit var switchBedroom: Switch
    private lateinit var switchLivingRoom: Switch
    private lateinit var switchBathroom: Switch
    private lateinit var switchKitchen: Switch

    // Room status indicators
    private lateinit var tvBedroomStatus: TextView
    private lateinit var tvLivingRoomStatus: TextView
    private lateinit var tvBathroomStatus: TextView
    private lateinit var tvKitchenStatus: TextView
    
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var selectedDevice: BluetoothDevice? = null
    
    // Activity result launcher for enabling Bluetooth
    private val enableBluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            showMessage(getString(R.string.bluetooth_enabled))
        } else {
            showMessage(getString(R.string.bluetooth_required))
        }
    }

    // Activity result launcher for device scan activity
    private val deviceScanLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == DeviceScanActivity.RESULT_DEVICE_CONNECTED) {
            result.data?.let { data ->
                val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    data.getParcelableExtra(DeviceScanActivity.EXTRA_SELECTED_DEVICE, BluetoothDevice::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    data.getParcelableExtra(DeviceScanActivity.EXTRA_SELECTED_DEVICE)
                }
                
                device?.let { bluetoothDevice ->
                    selectedDevice = bluetoothDevice
                    connectToDevice(bluetoothDevice)
                }
            }
        }
    }
    
    // Standard SerialPortService ID
    private val serialPortUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    
    companion object {
        private const val TAG = "BluetoothApp"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        initializeViews()
        setupClickListeners()
        initializeBluetooth()
    }
    
    private fun initializeViews() {
        tvConnectionStatus = findViewById(R.id.tvConnectionStatus)
        btnScan = findViewById(R.id.btnScan)
        btnConnect = findViewById(R.id.btnConnect)
        statusIndicator = findViewById(R.id.statusIndicator)
        
        // Initialize modern toggle switches
        switchBedroom = findViewById(R.id.switchBedroom)
        switchLivingRoom = findViewById(R.id.switchLivingRoom)
        switchBathroom = findViewById(R.id.switchBathroom)
        switchKitchen = findViewById(R.id.switchKitchen)

        // Initialize status indicators
        tvBedroomStatus = findViewById(R.id.tvBedroomStatus)
        tvLivingRoomStatus = findViewById(R.id.tvLivingRoomStatus)
        tvBathroomStatus = findViewById(R.id.tvBathroomStatus)
        tvKitchenStatus = findViewById(R.id.tvKitchenStatus)
        
        // Disable room controls initially
        disableRoomControls()
    }
    
    private fun setupClickListeners() {
        btnScan.setOnClickListener {
            animateButtonPress(btnScan)
            // Launch the device scan activity
            val intent = Intent(this, DeviceScanActivity::class.java)
            deviceScanLauncher.launch(intent)
        }
        
        btnConnect.setOnClickListener {
            animateButtonPress(btnConnect)
            if (bluetoothSocket?.isConnected == true) {
                disconnectFromDevice()
            } else {
                selectedDevice?.let { connectToDevice(it) }
            }
        }

        // Modern toggle switch listeners with smooth animations
        switchBedroom.setOnCheckedChangeListener { _, isChecked ->
            animateSwitch(switchBedroom)
            if (isChecked) {
                sendCommand("BEDROOM_ON")
            } else {
                sendCommand("BEDROOM_OFF")
            }
            updateRoomStatus("bedroom", isChecked)
        }

        switchLivingRoom.setOnCheckedChangeListener { _, isChecked ->
            animateSwitch(switchLivingRoom)
            if (isChecked) {
                sendCommand("LIVINGROOM_ON")
            } else {
                sendCommand("LIVINGROOM_OFF")
            }
            updateRoomStatus("livingroom", isChecked)
        }

        switchBathroom.setOnCheckedChangeListener { _, isChecked ->
            animateSwitch(switchBathroom)
            if (isChecked) {
                sendCommand("BATHROOM_ON")
            } else {
                sendCommand("BATHROOM_OFF")
            }
            updateRoomStatus("bathroom", isChecked)
        }

        switchKitchen.setOnCheckedChangeListener { _, isChecked ->
            animateSwitch(switchKitchen)
            if (isChecked) {
                sendCommand("KITCHEN_ON")
            } else {
                sendCommand("KITCHEN_OFF")
            }
            updateRoomStatus("kitchen", isChecked)
        }
    }
    
    private fun initializeBluetooth() {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        
        if (bluetoothAdapter == null) {
            showMessage(getString(R.string.bluetooth_not_supported))
            return
        }
        
        if (!bluetoothAdapter!!.isEnabled) {
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBluetoothLauncher.launch(enableBluetoothIntent)
        } else {
            showMessage(getString(R.string.bluetooth_enabled))
        }
    }
    
    private fun connectToDevice(device: BluetoothDevice) {
        try {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) 
                        Manifest.permission.BLUETOOTH_CONNECT 
                    else 
                        Manifest.permission.BLUETOOTH
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                showMessage(getString(R.string.bluetooth_connect_permission_required))
                return
            }
            
            // Stop discovery before connecting
            bluetoothAdapter?.cancelDiscovery()
            
            bluetoothSocket = device.createRfcommSocketToServiceRecord(serialPortUUID)
            
            showMessage(getString(R.string.connecting))
            updateConnectionStatus(getString(R.string.status_connecting), false)
            
            Thread {
                try {
                    bluetoothSocket!!.connect()
                    outputStream = bluetoothSocket!!.outputStream
                    
                    runOnUiThread {
                        updateConnectionStatus(getString(R.string.status_connected), true)
                        showMessage("Ready") // Clear message area with simple ready status
                        updateCurrentDeviceDisplay(device)
                        btnConnect.text = getString(R.string.disconnect)
                        btnConnect.isEnabled = true
                        enableRoomControls()
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "Connection failed", e)
                    runOnUiThread {
                        updateConnectionStatus(getString(R.string.status_connection_failed), false)
                        showMessage(getString(R.string.failed_to_connect, e.message ?: "Unknown error"))
                    }
                }
            }.start()
            
        } catch (e: IOException) {
            Log.e(TAG, "Error creating socket", e)
            showMessage(getString(R.string.failed_to_connect, e.message ?: "Unknown error"))
        }
    }
    
    private fun disconnectFromDevice() {
        try {
            bluetoothSocket?.close()
            outputStream?.close()
            updateConnectionStatus(getString(R.string.status_disconnected), false)
            updateCurrentDeviceDisplay(null)
            showMessage(getString(R.string.disconnected_from_device))
            btnConnect.text = getString(R.string.connect)
            btnConnect.isEnabled = selectedDevice != null
            disableRoomControls()
        } catch (e: IOException) {
            Log.e(TAG, "Error disconnecting", e)
        }
    }

    private fun updateCurrentDeviceDisplay(device: BluetoothDevice?) {
        val tvCurrentDevice = findViewById<TextView>(R.id.tvCurrentDevice)
        if (device != null) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) 
                        Manifest.permission.BLUETOOTH_CONNECT 
                    else 
                        Manifest.permission.BLUETOOTH
                ) == PackageManager.PERMISSION_GRANTED) {
                val deviceName = device.name ?: "Unknown Device"
                val deviceAddress = device.address
                tvCurrentDevice.text = "$deviceName\n$deviceAddress"
            } else {
                tvCurrentDevice.text = "Connected Device"
            }
        } else {
            tvCurrentDevice.text = "No device connected"
        }
    }
    
    private fun sendCommand(command: String) {
        if (bluetoothSocket?.isConnected != true) {
            showMessage(getString(R.string.not_connected_to_device))
            return
        }
        
        try {
            // Ultra-fast single character commands instead of strings
            val fastCommand = when(command) {
                "BEDROOM_ON" -> "1"
                "BEDROOM_OFF" -> "2"
                "LIVINGROOM_ON" -> "3"
                "LIVINGROOM_OFF" -> "4"
                "BATHROOM_ON" -> "5"
                "BATHROOM_OFF" -> "6"
                "KITCHEN_ON" -> "7"
                "KITCHEN_OFF" -> "8"
                else -> command
            }
            
            outputStream?.write(fastCommand.toByteArray())
            outputStream?.flush() // Force immediate send
            
            Log.d(TAG, "Fast command sent: $fastCommand")
        } catch (e: IOException) {
            Log.e(TAG, "Error sending command", e)
            showMessage(getString(R.string.failed_to_send_command))
        }
    }
    
    private fun updateConnectionStatus(status: String, connected: Boolean) {
        tvConnectionStatus.text = status
        
        val (backgroundColor, indicatorDrawable) = if (connected) {
            Pair(ContextCompat.getColor(this, R.color.success_green),
                 ContextCompat.getDrawable(this, R.drawable.status_indicator_connected))
        } else {
            Pair(ContextCompat.getColor(this, R.color.status_disconnected),
                 ContextCompat.getDrawable(this, R.drawable.status_indicator_disconnected))
        }
        
        tvConnectionStatus.setTextColor(backgroundColor)
        statusIndicator.background = indicatorDrawable
    }
    
    private fun enableRoomControls() {
        val switches = arrayOf(
            switchBedroom, switchLivingRoom,
            switchBathroom, switchKitchen
        )
        
        switches.forEach { switch ->
            switch.isEnabled = true
        }
    }
    
    private fun disableRoomControls() {
        val switches = arrayOf(
            switchBedroom, switchLivingRoom,
            switchBathroom, switchKitchen
        )
        
        switches.forEach { switch ->
            switch.isEnabled = false
        }
    }
    
    private fun updateRoomStatus(room: String, isOn: Boolean) {
        val status = if (isOn) "ON" else "OFF"
        val textColor = if (isOn) ContextCompat.getColor(this, android.R.color.white)
                       else ContextCompat.getColor(this, android.R.color.white)
        val backgroundDrawable = if (isOn) R.drawable.status_badge_on else R.drawable.status_badge_off
        
        when (room) {
            "bedroom" -> {
                tvBedroomStatus.text = status
                tvBedroomStatus.setTextColor(textColor)
                tvBedroomStatus.setBackgroundResource(backgroundDrawable)
            }
            "livingroom" -> {
                tvLivingRoomStatus.text = status
                tvLivingRoomStatus.setTextColor(textColor)
                tvLivingRoomStatus.setBackgroundResource(backgroundDrawable)
            }
            "bathroom" -> {
                tvBathroomStatus.text = status
                tvBathroomStatus.setTextColor(textColor)
                tvBathroomStatus.setBackgroundResource(backgroundDrawable)
            }
            "kitchen" -> {
                tvKitchenStatus.text = status
                tvKitchenStatus.setTextColor(textColor)
                tvKitchenStatus.setBackgroundResource(backgroundDrawable)
            }
        }
    }
    
    private fun showMessage(message: String) {
        // Since we removed tvMessage, just log the message
        Log.d(TAG, message)
        // Optionally show a Toast for important messages
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }
    
    private fun animateSwitch(switch: Switch) {
        // Beautiful scale animation for switches
        switch.animate()
            .scaleX(1.1f)
            .scaleY(1.1f)
            .setDuration(100)
            .withEndAction {
                switch.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(100)
                    .start()
            }
            .start()
    }
    
    private fun animateButtonPress(button: Button) {
        val scaleDown = android.view.animation.ScaleAnimation(
            1.0f, 0.95f, 1.0f, 0.95f,
            android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f,
            android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 50  // Faster animation for more responsive feel
            repeatCount = 1
            repeatMode = android.view.animation.Animation.REVERSE
        }
        button.startAnimation(scaleDown)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        try {
            bluetoothSocket?.close()
            outputStream?.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error closing connections", e)
        }
    }
}
