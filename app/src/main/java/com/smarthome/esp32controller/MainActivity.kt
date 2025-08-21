package com.smarthome.esp32controller

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.IOException
import java.io.OutputStream
import java.util.*

class MainActivity : AppCompatActivity() {
    
    private lateinit var tvConnectionStatus: TextView
    private lateinit var tvMessage: TextView
    private lateinit var tvDevicesLabel: TextView
    private lateinit var btnScan: Button
    private lateinit var btnConnect: Button
    private lateinit var recyclerViewDevices: RecyclerView
    private lateinit var layoutDeviceSection: android.view.ViewGroup
    private lateinit var layoutScanningProgress: android.view.ViewGroup
    private lateinit var layoutEmptyState: android.view.ViewGroup
    private lateinit var statusIndicator: android.view.View
    
    // Room control buttons
    private lateinit var btnBedroomOn: Button
    private lateinit var btnBedroomOff: Button
    private lateinit var btnLivingRoomOn: Button
    private lateinit var btnLivingRoomOff: Button
    private lateinit var btnBathroomOn: Button
    private lateinit var btnBathroomOff: Button
    private lateinit var btnKitchenOn: Button
    private lateinit var btnKitchenOff: Button

    // Room status indicators
    private lateinit var tvBedroomStatus: TextView
    private lateinit var tvLivingRoomStatus: TextView
    private lateinit var tvBathroomStatus: TextView
    private lateinit var tvKitchenStatus: TextView
    
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var selectedDevice: BluetoothDevice? = null
    
    private lateinit var deviceAdapter: ModernBluetoothDeviceAdapter
    
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
    
    // Standard SerialPortService ID
    private val serialPortUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    
    companion object {
        private const val REQUEST_PERMISSIONS = 2
        private const val TAG = "BluetoothApp"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        initializeViews()
        setupClickListeners()
        initializeBluetooth()
        setupDeviceList()
    }
    
    private fun initializeViews() {
        tvConnectionStatus = findViewById(R.id.tvConnectionStatus)
        tvMessage = findViewById(R.id.tvMessage)
        tvDevicesLabel = findViewById(R.id.tvDevicesLabel)
        btnScan = findViewById(R.id.btnScan)
        btnConnect = findViewById(R.id.btnConnect)
        recyclerViewDevices = findViewById(R.id.recyclerViewDevices)
        layoutDeviceSection = findViewById(R.id.layoutDeviceSection)
        layoutScanningProgress = findViewById(R.id.layoutScanningProgress)
        layoutEmptyState = findViewById(R.id.layoutEmptyState)
        statusIndicator = findViewById(R.id.statusIndicator)
        
        // Initialize room controls
        btnBedroomOn = findViewById(R.id.btnBedroomOn)
        btnBedroomOff = findViewById(R.id.btnBedroomOff)
        btnLivingRoomOn = findViewById(R.id.btnLivingRoomOn)
        btnLivingRoomOff = findViewById(R.id.btnLivingRoomOff)
        btnBathroomOn = findViewById(R.id.btnBathroomOn)
        btnBathroomOff = findViewById(R.id.btnBathroomOff)
        btnKitchenOn = findViewById(R.id.btnKitchenOn)
        btnKitchenOff = findViewById(R.id.btnKitchenOff)

        // Initialize status indicators
        tvBedroomStatus = findViewById(R.id.tvBedroomStatus)
        tvLivingRoomStatus = findViewById(R.id.tvLivingRoomStatus)
        tvBathroomStatus = findViewById(R.id.tvBathroomStatus)
        tvKitchenStatus = findViewById(R.id.tvKitchenStatus)
    }
    
    private fun setupClickListeners() {
        btnScan.setOnClickListener {
            animateButtonPress(btnScan)
            if (bluetoothAdapter?.isDiscovering == true) {
                bluetoothAdapter?.cancelDiscovery()
                btnScan.text = getString(R.string.scan_for_devices)
                layoutScanningProgress.visibility = android.view.View.GONE
                
                // Show empty state if no devices found
                if (deviceAdapter.getDeviceCount() == 0) {
                    layoutEmptyState.visibility = android.view.View.VISIBLE
                }
                
                showMessage(getString(R.string.scan_completed))
            } else {
                startDeviceScan()
            }
        }
        
        btnConnect.setOnClickListener {
            animateButtonPress(btnConnect)
            if (bluetoothSocket?.isConnected == true) {
                disconnectFromDevice()
            } else {
                selectedDevice?.let { connectToDevice(it) }
            }
        }
        
        // Room control listeners
        btnBedroomOn.setOnClickListener { 
            animateButtonPress(btnBedroomOn)
            sendCommand("BEDROOM_ON")
            updateRoomStatus("bedroom", true)
        }
        btnBedroomOff.setOnClickListener { 
            animateButtonPress(btnBedroomOff)
            sendCommand("BEDROOM_OFF")
            updateRoomStatus("bedroom", false)
        }

        btnLivingRoomOn.setOnClickListener { 
            animateButtonPress(btnLivingRoomOn)
            sendCommand("LIVINGROOM_ON")
            updateRoomStatus("livingroom", true)
        }
        btnLivingRoomOff.setOnClickListener { 
            animateButtonPress(btnLivingRoomOff)
            sendCommand("LIVINGROOM_OFF")
            updateRoomStatus("livingroom", false)
        }

        btnBathroomOn.setOnClickListener { 
            animateButtonPress(btnBathroomOn)
            sendCommand("BATHROOM_ON")
            updateRoomStatus("bathroom", true)
        }
        btnBathroomOff.setOnClickListener { 
            animateButtonPress(btnBathroomOff)
            sendCommand("BATHROOM_OFF")
            updateRoomStatus("bathroom", false)
        }

        btnKitchenOn.setOnClickListener { 
            animateButtonPress(btnKitchenOn)
            sendCommand("KITCHEN_ON")
            updateRoomStatus("kitchen", true)
        }
        btnKitchenOff.setOnClickListener { 
            animateButtonPress(btnKitchenOff)
            sendCommand("KITCHEN_OFF")
            updateRoomStatus("kitchen", false)
        }
    }
    
    private fun setupDeviceList() {
        deviceAdapter = ModernBluetoothDeviceAdapter { device ->
            selectedDevice = device
            btnConnect.isEnabled = true
            btnConnect.text = getString(R.string.connect_to_device, device.name ?: getString(R.string.unknown_device))
            showMessage(getString(R.string.selected_device, device.name ?: getString(R.string.unknown_device), device.address))
        }
        
        recyclerViewDevices.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = deviceAdapter
            setHasFixedSize(true)
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
        }
        
        checkPermissions()
        
        // Register receiver for device discovery
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        registerReceiver(bluetoothReceiver, filter)
    }
    
    
    // BroadcastReceiver for device discovery
    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                        device?.let { handleFoundDevice(it) }
                    } else {
                        @Suppress("DEPRECATION")
                        val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        device?.let { handleFoundDevice(it) }
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    btnScan.text = getString(R.string.scan_for_devices)
                    showMessage(getString(R.string.scan_completed))
                }
            }
        }
    }
    
    private fun handleFoundDevice(device: BluetoothDevice) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            deviceAdapter.addDevice(device)
            
            // Show device list with animation if it's the first device
            if (tvDevicesLabel.visibility == android.view.View.GONE) {
                tvDevicesLabel.visibility = android.view.View.VISIBLE
                layoutDeviceSection.visibility = android.view.View.VISIBLE
                layoutEmptyState.visibility = android.view.View.GONE
                
                val fadeInAnimation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.fade_in_up)
                tvDevicesLabel.startAnimation(fadeInAnimation)
                layoutDeviceSection.startAnimation(fadeInAnimation)
            }
            
            showMessage(getString(R.string.found_device, device.name ?: getString(R.string.unknown_device)))
        }
    }
    
    private fun startDeviceScan() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            showMessage(getString(R.string.bluetooth_scan_permission_required))
            return
        }
        
        deviceAdapter.clearDevices()
        selectedDevice = null
        btnConnect.isEnabled = false
        btnConnect.text = getString(R.string.connect)
        
        // Add paired devices first
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val pairedDevices = bluetoothAdapter?.bondedDevices
            pairedDevices?.forEach { device ->
                deviceAdapter.addDevice(device)
            }
            
            if (pairedDevices?.isNotEmpty() == true) {
                tvDevicesLabel.visibility = android.view.View.VISIBLE
                layoutDeviceSection.visibility = android.view.View.VISIBLE
                layoutEmptyState.visibility = android.view.View.GONE
                
                val fadeInAnimation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.fade_in_up)
                tvDevicesLabel.startAnimation(fadeInAnimation)
                layoutDeviceSection.startAnimation(fadeInAnimation)
            }
        }
        
        // Start discovery for new devices
        bluetoothAdapter?.startDiscovery()
        btnScan.text = getString(R.string.stop_scan)
        
        // Show scanning progress
        layoutScanningProgress.visibility = android.view.View.VISIBLE
        layoutDeviceSection.visibility = android.view.View.VISIBLE
        
        showMessage(getString(R.string.scanning_for_devices))
    }
    
    private fun checkPermissions() {
        val permissions = mutableListOf<String>()
        
        // Add permissions based on Android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.addAll(arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            ))
        } else {
            permissions.addAll(arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            ))
        }
        
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), REQUEST_PERMISSIONS)
        }
    }
    
    private fun connectToDevice(device: BluetoothDevice) {
        try {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                showMessage(getString(R.string.bluetooth_connect_permission_required))
                return
            }
            
            // Stop discovery before connecting
            bluetoothAdapter?.cancelDiscovery()
            
            bluetoothSocket = device.createRfcommSocketToServiceRecord(serialPortUUID)
            
            showMessage(getString(R.string.connecting))
            updateConnectionStatus(getString(R.string.connecting), false)
            
            bluetoothSocket!!.connect()
            outputStream = bluetoothSocket!!.outputStream
            
            updateConnectionStatus(getString(R.string.status_connected), true)
            showMessage(getString(R.string.connected_to_device, device.name ?: getString(R.string.unknown_device)))
            btnConnect.text = getString(R.string.disconnect)
            
        } catch (e: IOException) {
            Log.e(TAG, "Error connecting to device", e)
            updateConnectionStatus(getString(R.string.status_connection_failed), false)
            showMessage(getString(R.string.failed_to_connect, e.message ?: "Unknown error"))
        }
    }
    
    private fun disconnectFromDevice() {
        try {
            bluetoothSocket?.close()
            outputStream?.close()
            updateConnectionStatus(getString(R.string.status_disconnected), false)
            showMessage(getString(R.string.disconnected_from_device))
            btnConnect.text = getString(R.string.connect)
            btnConnect.isEnabled = selectedDevice != null
        } catch (e: IOException) {
            Log.e(TAG, "Error disconnecting", e)
        }
    }
    
    private fun sendCommand(command: String) {
        if (bluetoothSocket?.isConnected != true) {
            showMessage(getString(R.string.not_connected_to_device))
            return
        }
        
        try {
            outputStream?.write(command.toByteArray())
            showMessage(getString(R.string.sent_command, command))
            Log.d(TAG, "Command sent: $command")
        } catch (e: IOException) {
            Log.e(TAG, "Error sending command", e)
            showMessage(getString(R.string.failed_to_send_command))
        }
    }
    
    private fun updateConnectionStatus(status: String, connected: Boolean) {
        tvConnectionStatus.text = status
        
        // Update status indicator and colors
        val (textColor, indicatorDrawable) = when {
            status.contains("Connected") -> {
                Pair(ContextCompat.getColor(this, R.color.status_connected),
                     ContextCompat.getDrawable(this, R.drawable.status_indicator_connected))
            }
            status.contains("Connecting") -> {
                Pair(ContextCompat.getColor(this, R.color.status_connecting),
                     ContextCompat.getDrawable(this, R.drawable.status_indicator_connected))
            }
            else -> {
                Pair(ContextCompat.getColor(this, R.color.status_disconnected),
                     ContextCompat.getDrawable(this, R.drawable.status_indicator_disconnected))
            }
        }
        
        tvConnectionStatus.setTextColor(textColor)
        statusIndicator.background = indicatorDrawable
        
        btnScan.isEnabled = !connected
        
        // Enable/disable room controls
        val roomButtons = listOf(
            btnBedroomOn, btnBedroomOff,
            btnLivingRoomOn, btnLivingRoomOff,
            btnBathroomOn, btnBathroomOff,
            btnKitchenOn, btnKitchenOff
        )

        roomButtons.forEach { button ->
            button.isEnabled = connected
        }
    }
    
    private fun updateRoomStatus(room: String, isOn: Boolean) {
        val status = if (isOn) "ON" else "OFF"
        val color = if (isOn) 
            ContextCompat.getColor(this, R.color.success_green) 
        else 
            ContextCompat.getColor(this, R.color.text_secondary)

        when (room.lowercase()) {
            "bedroom" -> {
                tvBedroomStatus.text = status
                tvBedroomStatus.setTextColor(color)
            }
            "livingroom" -> {
                tvLivingRoomStatus.text = status
                tvLivingRoomStatus.setTextColor(color)
            }
            "bathroom" -> {
                tvBathroomStatus.text = status
                tvBathroomStatus.setTextColor(color)
            }
            "kitchen" -> {
                tvKitchenStatus.text = status
                tvKitchenStatus.setTextColor(color)
            }
        }
    }
    
    private fun showMessage(message: String) {
        tvMessage.text = message
        
        // Create custom toast with modern approach
        val inflater = layoutInflater
        val layout = inflater.inflate(R.layout.custom_toast, null)
        val toastText = layout.findViewById<TextView>(R.id.toast_text)
        toastText.text = message
        
        @Suppress("DEPRECATION")
        val toast = Toast(applicationContext).apply {
            duration = Toast.LENGTH_SHORT
            view = layout
        }
        toast.show()
    }
    
    private fun animateButtonPress(button: android.view.View) {
        val animation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.button_press)
        button.startAnimation(animation)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        disconnectFromDevice()
        try {
            unregisterReceiver(bluetoothReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver not registered
        }
        bluetoothAdapter?.cancelDiscovery()
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS) {
            val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            if (allGranted) {
                showMessage(getString(R.string.permissions_granted))
            } else {
                showMessage(getString(R.string.permissions_required))
            }
        }
    }
}