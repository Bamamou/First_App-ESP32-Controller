package com.example.testing

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
import java.io.IOException
import java.io.OutputStream
import java.util.*

class MainActivity : AppCompatActivity() {
    
    private lateinit var tvConnectionStatus: TextView
    private lateinit var tvMessage: TextView
    private lateinit var tvDevicesLabel: TextView
    private lateinit var btnScan: Button
    private lateinit var btnConnect: Button
    private lateinit var btnOn: Button
    private lateinit var btnOff: Button
    private lateinit var listViewDevices: ListView
    private lateinit var statusIndicator: android.view.View
    
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var selectedDevice: BluetoothDevice? = null
    
    private lateinit var deviceAdapter: BluetoothDeviceAdapter
    
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
        btnOn = findViewById(R.id.btnOn)
        btnOff = findViewById(R.id.btnOff)
        listViewDevices = findViewById(R.id.listViewDevices)
        statusIndicator = findViewById(R.id.statusIndicator)
    }
    
    private fun setupClickListeners() {
        btnScan.setOnClickListener {
            animateButtonPress(btnScan)
            if (bluetoothAdapter?.isDiscovering == true) {
                bluetoothAdapter?.cancelDiscovery()
                btnScan.text = getString(R.string.scan_for_devices)
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
        
        btnOn.setOnClickListener {
            animateButtonPress(btnOn)
            sendCommand("ON")
        }
        
        btnOff.setOnClickListener {
            animateButtonPress(btnOff)
            sendCommand("OFF")
        }
    }
    
    private fun setupDeviceList() {
        deviceAdapter = BluetoothDeviceAdapter(this)
        listViewDevices.adapter = deviceAdapter
        
        listViewDevices.setOnItemClickListener { _, _, position, _ ->
            selectedDevice = deviceAdapter.getDevice(position)
            btnConnect.isEnabled = true
            btnConnect.text = getString(R.string.connect_to_device, selectedDevice?.name ?: getString(R.string.unknown_device))
            showMessage(getString(R.string.selected_device, selectedDevice?.name ?: getString(R.string.unknown_device), selectedDevice?.address))
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
                listViewDevices.visibility = android.view.View.VISIBLE
                
                val fadeInAnimation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.fade_in_up)
                tvDevicesLabel.startAnimation(fadeInAnimation)
                listViewDevices.startAnimation(fadeInAnimation)
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
                listViewDevices.visibility = android.view.View.VISIBLE
                
                val fadeInAnimation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.fade_in_up)
                tvDevicesLabel.startAnimation(fadeInAnimation)
                listViewDevices.startAnimation(fadeInAnimation)
            }
        }
        
        // Start discovery for new devices
        bluetoothAdapter?.startDiscovery()
        btnScan.text = getString(R.string.stop_scan)
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
        
        btnOn.isEnabled = connected
        btnOff.isEnabled = connected
        btnScan.isEnabled = !connected
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