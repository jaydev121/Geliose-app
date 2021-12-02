package com.example.geliose

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.ParcelUuid
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_menu.*

private const val ENABLE_BLUETOOTH_REQUEST_CODE = 1
private const val LOCATION_PERMISSION_REQUEST_CODE = 2


class MenuActivity : AppCompatActivity() {

    private lateinit var mAdapter: ScanResultAdapter

    val isLocationPermissionGranted
        get() = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)

    fun Context.hasPermission(permissionType: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permissionType) ==
                PackageManager.PERMISSION_GRANTED
    }

    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    override fun onResume() {
        super.onResume()
        if (!bluetoothAdapter.isEnabled) {
            promptEnableBluetooth()
        }
    }

    private fun setupRecyclerView() {
        scan_results_recycler_view.apply {
          //  mAdapter = ScanResultAdapter(scanResults,onClickListener = ScanButton )
            adapter = scanResultAdapter // mAdapter
            layoutManager = LinearLayoutManager(
                    this@MenuActivity,
                    RecyclerView.VERTICAL,
                    false
            )
            isNestedScrollingEnabled = false
        }

        val animator = scan_results_recycler_view.itemAnimator
        if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }
    }

    private fun promptEnableBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ENABLE_BLUETOOTH_REQUEST_CODE -> {
                if (resultCode != Activity.RESULT_OK) {
                    promptEnableBluetooth()
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

       ScanButton.setOnClickListener(){
           if (isScanning) {
               stopBleScan()
           } else {
               startBleScan()
           }

       setupRecyclerView()

       }

        button.setOnClickListener(){
            val intent = Intent(this,SocActivity::class.java)
            startActivity(intent)
        }

        button2.setOnClickListener(){
            val intent = Intent(this,ValuesActivity::class.java)
            startActivity(intent)
        }

        button3.setOnClickListener(){
            val intent = Intent(this,CellStatusActivity::class.java)
            startActivity(intent)
        }

        button4.setOnClickListener(){
            val intent = Intent(this,InfoActivity::class.java)
            startActivity(intent)
        }


        }

    private var isScanning = false
        set(value) {
            field = value
            runOnUiThread { //ScanButton = if (value) "Stop Scan" else "Start Scan"
             }
        }

    private fun startBleScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isLocationPermissionGranted) {
            requestLocationPermission()
        } else {
            scanResults.clear()
            scanResultAdapter.notifyDataSetChanged()
            bleScanner.startScan(null, scanSettings, scanCallback)
            isScanning = true
        }
    }

    private fun stopBleScan() {
        bleScanner.stopScan(scanCallback)
        isScanning = false
    }



    private fun Activity.requestPermission(permission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
    }
    ////////////////////////////////////////////////////////////////////////////////////////////
    private fun requestLocationPermission() {
        if (isLocationPermissionGranted) {
            return
        }
        runOnUiThread {
            val alert =   AlertDialog.Builder(this)
            alert.setTitle("Location permission required")
            alert.setMessage("Starting from Android M (6.0), the system requires apps to be granted " +
                    "location access in order to scan for BLE devices.")
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok){ dialogInterface: DialogInterface, i: Int ->
                        requestPermission(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                LOCATION_PERMISSION_REQUEST_CODE
                        )

                    }


            val showalert = alert.create()
            showalert.setTitle("AlertDialogExample")
            showalert.show()
        }
////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.firstOrNull() == PackageManager.PERMISSION_DENIED) {
                    requestLocationPermission()
                } else {
                    startBleScan()
                }
            }
        }
    }


    val filter = ScanFilter.Builder().setServiceUuid(
            ParcelUuid.fromString("0000FFE0-0000-1000-8000-00805F9B34FB")
    ).build()


    private val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val indexQuery = scanResults.indexOfFirst { it.device.address == result.device.address }
            if (indexQuery != -1) { // A scan result already exists with the same address
                scanResults[indexQuery] = result
                scanResultAdapter.notifyItemChanged(indexQuery)
            } else {
                with(result.device) {
                    Log.i("ScanCallback","Found BLE device! Name: ${name ?: "unnamed"},address:$address")
                }
                scanResults.add(result)
                scanResultAdapter.notifyItemInserted(scanResults.size - 1)
            }
        }

        override fun onScanFailed(errorCode: Int) {

            Log.e("ScanCallback","onScanFailed : code $errorCode")
        }
    }

    private val scanResults = mutableListOf<ScanResult>()

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val deviceAddress = gatt.device.address

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.w("BluetoothGattCallback", "Successfully connected to $deviceAddress")
                    // TODO: Store a reference to BluetoothGatt
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.w("BluetoothGattCallback", "Successfully disconnected from $deviceAddress")
                    gatt.close()
                }
            } else {
                Log.w("BluetoothGattCallback", "Error $status encountered for $deviceAddress! Disconnecting...")
                gatt.close()
            }
        }
    }


    private val scanResultAdapter: ScanResultAdapter by lazy {
        ScanResultAdapter(scanResults) { result ->
            // User tapped on a scan result
            if (isScanning) {
                stopBleScan()
            }
            with(result.device) {
                Log.w("ScanResultAdapter", "Connecting to $address")
                connectGatt(this@MenuActivity, false, gattCallback)
            }
        }
    }












}

