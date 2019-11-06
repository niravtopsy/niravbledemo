package com.topsble.scan

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.pixplicity.easyprefs.library.Prefs
import com.polidea.rxandroidble2.scan.ScanResult
import com.topsble.R
import com.topsble.base.ScanInterface
import com.topsble.bleutility.BLEUtilityClass

import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.polidea.rxandroidble2.exceptions.BleScanException
import com.topsble.bleutility.BLEUtilityClass.Companion.EXTRA_MAC_ADDRESS
import com.topsble.devicedetails.ConnectionActivity
import com.topsble.util.isLocationPermissionGranted
import com.topsble.util.requestLocationPermission
import com.topsble.util.showError
import kotlinx.android.synthetic.main.activity_scanning.*
import kotlin.math.log


class ScanActivity : AppCompatActivity(), ScanInterface {

    private var hasClickedScan: Boolean = false

    private val resultsAdapter =
            ScanningResultsAdapter { startActivity(ConnectionActivity.newInstance(this, it.bleDevice.macAddress)) }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanning)
        startBluetooth()
        BLEUtilityClass.initializeScanBLE(this)

        configureResultList()
        btnScan.setOnClickListener { onScanToggleClick() }

        BLEUtilityClass().newTest(::nT)
    }

    fun startBluetooth() {

        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        val REQUEST_ENABLE_BT = 1
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
    }

    private fun configureResultList() {
        with(scan_results) {
            setHasFixedSize(true)
            itemAnimator = null
            adapter = resultsAdapter
        }
    }

    fun onScanToggleClick() {
        scanBLEDevice()
        updateButtonUIState()
    }

    private fun scanBLEDevice() {

        if (BLEUtilityClass.isScanning) {
            BLEUtilityClass.disposeAll()
        } else {
            if (isLocationPermissionGranted()) {
                BLEUtilityClass.scanBleDevices()
            } else {
                hasClickedScan = true
                requestLocationPermission()
            }
        }
    }



    private fun updateButtonUIState() {
        btnScan!!.setText(if (BLEUtilityClass.isScanning) R.string.stop_scan else R.string.start_scan)
    }

    override fun disposeBLE() {
        resultsAdapter!!.clearScanResults()
        updateButtonUIState()
    }

    override fun showErrorMessage(throwable: Throwable) {
        if (throwable is BleScanException) showError(throwable)
    }

    override fun onBLEConnected(scanResult: ScanResult) {
        Log.e("testing_scan_de_", scanResult.bleDevice.macAddress)
        resultsAdapter?.addScanResult(scanResult)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (isLocationPermissionGranted(requestCode, grantResults) && hasClickedScan) {
            hasClickedScan = false
            BLEUtilityClass.scanBleDevices()
        }
    }

    public override fun onPause() {
        super.onPause()
        BLEUtilityClass.disposeScan()
    }

    private fun nT(str:String){


    }


}
