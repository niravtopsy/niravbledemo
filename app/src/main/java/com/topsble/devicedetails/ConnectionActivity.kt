package com.topsble.devicedetails

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pixplicity.easyprefs.library.Prefs
import com.polidea.rxandroidble2.RxBleDeviceServices
import com.polidea.rxandroidble2.samplekotlin.example3_discovery.DiscoveryResultsAdapter
import com.topsble.R
import com.topsble.base.ConnectionInterface
import com.topsble.bleutility.BLEUtilityClass
import com.topsble.bleutility.BLEUtilityClass.Companion.EXTRA_MAC_ADDRESS
import com.topsble.util.showSnackbarShort
import kotlinx.android.synthetic.main.activity_devicedetails.*
import java.util.*


class ConnectionActivity : AppCompatActivity(), ConnectionInterface {

    private lateinit var characteristicUuid: UUID
    private val resultsAdapter = DiscoveryResultsAdapter {
        onAdapterItemClick(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_devicedetails)

        val macAddress = intent.getStringExtra(EXTRA_MAC_ADDRESS)
        title = getString(R.string.mac_address, macAddress)
        Prefs.putString("macAddress", macAddress)
        BLEUtilityClass.initializeConnection(this)
        btnConnect.setOnClickListener { BLEUtilityClass.onConnectToggleClick() }
        btnRead.setOnClickListener {
            characteristicUuid = UUID.fromString("00002a00-0000-1000-8000-00805f9b34fb")
            BLEUtilityClass.onReadClick(characteristicUuid)
        }

        btnWrite.setOnClickListener {

            var inputBytes: ByteArray = "123456789".toByteArray()

            characteristicUuid = UUID.fromString("00001531-0000-3512-2118-0009af100700")
            BLEUtilityClass.onWriteClick(characteristicUuid, inputBytes)
        }

        btnDiscover.setOnClickListener {
            BLEUtilityClass.onDiscoverClick()
        }

        with(rcvDiscoverList) {
            setHasFixedSize(true)
            itemAnimator = null
            adapter = resultsAdapter
        }

    }

    override fun onPause() {
        super.onPause()
        BLEUtilityClass.triggerDisconnect()
    }

    override fun onDestroy() {
        super.onDestroy()
        BLEUtilityClass.stateDispose()
    }

    override fun connectBLE() {
        btnConnect.setText(R.string.disconnect)
        BLEUtilityClass.clearDiscoverDisposal()
    }

    override fun disConnectBLE() {
        btnConnect.setText(R.string.connect)
    }

    override fun showErrorMessage(error: String) {
        showSnackbarShort("Connection error: $error")
    }

    companion object {
        fun newInstance(context: Context, macAddress: String): Intent =
                Intent(context, ConnectionActivity::class.java).apply { putExtra(EXTRA_MAC_ADDRESS, macAddress) }
    }

    override fun discoverUpdateUI(isConnected: Boolean) {
        btnDiscover.isEnabled = isConnected
    }

    override fun discoverDeviceList(rxBleDiscoverDevice: RxBleDeviceServices) {
        resultsAdapter.swapScanResult(rxBleDiscoverDevice)
    }

    private fun onAdapterItemClick(item: DiscoveryResultsAdapter.AdapterItem) {
        when (item.type) {
            DiscoveryResultsAdapter.AdapterItem.CHARACTERISTIC -> {
                // action here
            }
            else -> showSnackbarShort(R.string.not_clickable)
        }
    }

    override fun writeSuccess() {
        showSnackbarShort("Write Success")
    }
}
