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
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.IOException
import java.util.*

class DeviceScanActivity : AppCompatActivity() {

    private lateinit var recyclerViewDevices: RecyclerView
    private lateinit var layoutScanningProgress: LinearLayout
    private lateinit var layoutEmptyState: LinearLayout
    private lateinit var btnScan: Button
    private lateinit var btnBack: Button
    private lateinit var tvMessage: TextView
    private lateinit var progressScanning: ProgressBar

    private var bluetoothAdapter: BluetoothAdapter? = null
    private lateinit var deviceAdapter: ModernBluetoothDeviceAdapter
    private val discoveredDevices = mutableSetOf<BluetoothDevice>()
    private var isScanning = false

    companion object {
        private const val TAG = "DeviceScanActivity"
        const val EXTRA_SELECTED_DEVICE = "selected_device"
        const val RESULT_DEVICE_CONNECTED = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_scan)

        initializeViews()
        setupBluetoothAdapter()
        setupRecyclerView()
        setupClickListeners()
        checkBluetoothAndPermissions()
    }

    private fun initializeViews() {
        recyclerViewDevices = findViewById(R.id.recyclerViewDevices)
        layoutScanningProgress = findViewById(R.id.layoutScanningProgress)
        layoutEmptyState = findViewById(R.id.layoutEmptyState)
        btnScan = findViewById(R.id.btnScan)
        btnBack = findViewById(R.id.btnBack)
        tvMessage = findViewById(R.id.tvMessage)
        progressScanning = findViewById(R.id.progressScanning)
    }

    private fun setupBluetoothAdapter() {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
    }

    private fun setupRecyclerView() {
        deviceAdapter = ModernBluetoothDeviceAdapter { device ->
            onDeviceSelected(device)
        }
        
        recyclerViewDevices.apply {
            layoutManager = LinearLayoutManager(this@DeviceScanActivity)
            adapter = deviceAdapter
        }
    }

    private fun setupClickListeners() {
        btnScan.setOnClickListener {
            if (isScanning) {
                stopScanning()
            } else {
                startScanning()
            }
        }

        btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun checkBluetoothAndPermissions() {
        if (bluetoothAdapter == null) {
            showMessage("Bluetooth not supported on this device")
            finish()
            return
        }

        if (!bluetoothAdapter!!.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            bluetoothEnableLauncher.launch(enableBtIntent)
        } else {
            checkPermissions()
        }
    }

    private val bluetoothEnableLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            checkPermissions()
        } else {
            showMessage("Bluetooth is required for device scanning")
            finish()
        }
    }

    private fun checkPermissions() {
        val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            )
        }

        val missingPermissions = requiredPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            permissionLauncher.launch(missingPermissions.toTypedArray())
        } else {
            onPermissionsGranted()
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            onPermissionsGranted()
        } else {
            showMessage("Permissions are required for device scanning")
            finish()
        }
    }

    private fun onPermissionsGranted() {
        showMessage("Ready to scan for devices")
        registerBluetoothReceiver()
        startScanning()
    }

    private fun registerBluetoothReceiver() {
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        registerReceiver(bluetoothReceiver, filter)
    }

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }

                    device?.let { bluetoothDevice ->
                        if (ActivityCompat.checkSelfPermission(
                                this@DeviceScanActivity,
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) 
                                    Manifest.permission.BLUETOOTH_CONNECT 
                                else 
                                    Manifest.permission.BLUETOOTH
                            ) == PackageManager.PERMISSION_GRANTED) {
                            
                            Log.d(TAG, "Found device: ${bluetoothDevice.name} - ${bluetoothDevice.address}")
                            
                            if (discoveredDevices.add(bluetoothDevice)) {
                                runOnUiThread {
                                    deviceAdapter.updateDevices(discoveredDevices.toList())
                                    updateEmptyState()
                                    showMessage("Found: ${bluetoothDevice.name ?: "Unknown Device"}")
                                }
                            }
                        }
                    }
                }
                
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.d(TAG, "Device discovery finished")
                    runOnUiThread {
                        stopScanning()
                        showMessage("Scan completed - Found ${discoveredDevices.size} devices")
                    }
                }
            }
        }
    }

    private fun startScanning() {
        if (ActivityCompat.checkSelfPermission(
                this,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) 
                    Manifest.permission.BLUETOOTH_SCAN 
                else 
                    Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        discoveredDevices.clear()
        deviceAdapter.updateDevices(emptyList())
        
        isScanning = true
        updateScanButton()
        showScanningProgress(true)
        updateEmptyState()
        
        bluetoothAdapter?.startDiscovery()
        showMessage("Scanning for devices...")
        
        // Auto-stop scanning after 12 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            if (isScanning) {
                stopScanning()
            }
        }, 12000)
    }

    private fun stopScanning() {
        if (ActivityCompat.checkSelfPermission(
                this,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) 
                    Manifest.permission.BLUETOOTH_SCAN 
                else 
                    Manifest.permission.BLUETOOTH_ADMIN
            ) == PackageManager.PERMISSION_GRANTED) {
            bluetoothAdapter?.cancelDiscovery()
        }
        
        isScanning = false
        updateScanButton()
        showScanningProgress(false)
        updateEmptyState()
    }

    private fun updateScanButton() {
        btnScan.text = if (isScanning) "Stop Scan" else "Start Scan"
    }

    private fun showScanningProgress(show: Boolean) {
        layoutScanningProgress.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun updateEmptyState() {
        val shouldShowEmpty = discoveredDevices.isEmpty() && !isScanning
        layoutEmptyState.visibility = if (shouldShowEmpty) View.VISIBLE else View.GONE
        recyclerViewDevices.visibility = if (discoveredDevices.isNotEmpty()) View.VISIBLE else View.GONE
    }

    private fun onDeviceSelected(device: BluetoothDevice) {
        showMessage("Connecting to ${device.name ?: "Unknown Device"}...")
        
        // Stop scanning when device is selected
        stopScanning()
        
        // Attempt to connect to the selected device
        connectToDevice(device)
    }

    private fun connectToDevice(device: BluetoothDevice) {
        Thread {
            try {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) 
                            Manifest.permission.BLUETOOTH_CONNECT 
                        else 
                            Manifest.permission.BLUETOOTH
                    ) != PackageManager.PERMISSION_GRANTED) {
                    runOnUiThread {
                        showMessage("Bluetooth permission required")
                    }
                    return@Thread
                }

                val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // Standard SerialPortService ID
                val socket = device.createRfcommSocketToServiceRecord(uuid)
                
                runOnUiThread {
                    showMessage("Connecting...")
                }
                
                socket.connect()
                
                runOnUiThread {
                    showMessage("Connected successfully!")
                    
                    // Return to MainActivity with the connected device
                    val resultIntent = Intent().apply {
                        putExtra(EXTRA_SELECTED_DEVICE, device)
                    }
                    setResult(RESULT_DEVICE_CONNECTED, resultIntent)
                    
                    // Close the socket since MainActivity will create its own
                    try {
                        socket.close()
                    } catch (e: IOException) {
                        Log.w(TAG, "Error closing test socket", e)
                    }
                    
                    finish()
                }
                
            } catch (e: IOException) {
                Log.e(TAG, "Connection failed", e)
                runOnUiThread {
                    showMessage("Connection failed: ${e.message}")
                    Toast.makeText(this, "Failed to connect to ${device.name ?: "device"}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun showMessage(message: String) {
        tvMessage.text = message
        Log.d(TAG, message)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(bluetoothReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver was not registered
        }
        
        if (isScanning) {
            stopScanning()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setResult(RESULT_CANCELED)
        finish()
    }
}
