package com.topsble.bleutility

import android.util.Log
import com.jakewharton.rx.ReplayingShare
import com.pixplicity.easyprefs.library.Prefs

import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleDevice
import com.polidea.rxandroidble2.scan.ScanFilter
import com.polidea.rxandroidble2.scan.ScanResult
import com.polidea.rxandroidble2.scan.ScanSettings
import com.topsble.application.SampleApplication
import com.topsble.base.ConnectionInterface
import com.topsble.base.ScanInterface
import com.topsble.scan.ScanActivity
import com.topsble.util.isConnected
import com.topsble.util.toHex
import io.reactivex.Observable

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import java.util.*


class BLEUtilityClass {

    companion object {

        const val EXTRA_MAC_ADDRESS = "extra_mac_address"

        // For scanning

        private var rxBleClient: RxBleClient? = null
        private var scanDisposable: Disposable? = null
        private var callBackScan: ScanInterface? = null;
        private var callBackConnection: ConnectionInterface? = null

        val isScanning: Boolean
            get() = scanDisposable != null

        fun initializeScanBLE(callBack: ScanInterface) {

            this.callBackScan = callBack
            rxBleClient = SampleApplication.rxBleClient
        }




        fun scanBleDevices() {

            scanBleDevicess()
                    .observeOn(AndroidSchedulers.mainThread())
                    .doFinally { dispose() }
                    .subscribe({

                        callBackScan?.onBLEConnected(it)

                    },
                            { onScanFailure(it) })
                    .let { scanDisposable = it }

        }

        private fun scanBleDevicess(): Observable<ScanResult> {

            val scanSettings = ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                    .build()

            val scanFilter = ScanFilter.Builder()
                    .build()

            return rxBleClient!!.scanBleDevices(scanSettings, scanFilter)
        }

        private fun onScanFailure(throwable: Throwable) {
            callBackScan?.showErrorMessage(throwable)
        }

        fun disposeScan() {
            if (scanDisposable != null) {
                if (isScanning) {
                    /*
                     * Stop scanning in onPause callback.
                     */
                    disposeAll()
                }
            }
        }

        private fun dispose() {
            scanDisposable = null
            callBackScan?.disposeBLE()
        }

        fun disposeAll() {
            scanDisposable!!.dispose()
        }

        // For Connection

        private lateinit var bleDevice: RxBleDevice

        private var connectionDisposable: Disposable? = null

        private var stateDisposable: Disposable? = null

        fun initializeConnection(callBack: ConnectionInterface) {

            this.callBackConnection = callBack;

            bleDevice = SampleApplication.rxBleClient.getBleDevice(Prefs.getString("macAddress", ""))

            bleDevice.observeConnectionStateChanges()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { onConnectionStateChange(it) }
                    .let { stateDisposable = it }

            connectionObservable = prepareConnectionObservable()
        }

        fun onConnectionStateChange(newState: RxBleConnection.RxBleConnectionState) {

            if (bleDevice.isConnected) {
                callBackConnection?.connectBLE()

            } else {
                callBackConnection?.disConnectBLE()
            }

        }

        fun onConnectToggleClick() {
            if (bleDevice.isConnected) {
                triggerDisconnect()
            } else {
                bleDevice.establishConnection(false)
                        .observeOn(AndroidSchedulers.mainThread())
                        .doFinally { disposeConnection() }
                        .subscribe({ onConnectionReceived() }, { onConnectionFailure(it) })
                        .let { connectionDisposable = it }
            }
        }

        private fun onConnectionFailure(throwable: Throwable) {
            callBackConnection?.showErrorMessage(throwable.localizedMessage)

        }

        private fun onConnectionReceived() {
            callBackConnection?.showErrorMessage("Connection received")
        }


        fun triggerDisconnect() = connectionDisposable?.dispose()

        fun disposeConnection() {
            connectionDisposable = null
        }

        fun stateDispose() {
            stateDisposable?.dispose()
        }

        // read operation :

        private lateinit var connectionObservable: Observable<RxBleConnection>
        private val disconnectTriggerSubject = PublishSubject.create<Unit>()
        private val connectionDisposableRead = CompositeDisposable()

        private fun prepareConnectionObservable(): Observable<RxBleConnection> =
                bleDevice
                        .establishConnection(false)
                        .takeUntil(disconnectTriggerSubject)
                        .compose(ReplayingShare.instance())

        fun onReadClick(characteristicUuid: UUID) {

            if (bleDevice.isConnected) {
                connectionObservable
                        .firstOrError()
                        .flatMap { it.readCharacteristic(characteristicUuid) }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ bytes ->
                            Log.e("ble_read_data_", String(bytes) + "\n" +
                                    bytes?.toHex())
                        }, { onReadFailure(it) })
                        .let { connectionDisposableRead.add(it) }
            }
        }

        private fun onReadFailure(throwable: Throwable) {
            callBackConnection?.showErrorMessage(throwable.localizedMessage)
        }

        // write operation

        fun onWriteClick(characteristicUuid: UUID, inputBytes: ByteArray) {
            if (bleDevice.isConnected) {
                connectionObservable
                        .firstOrError()
                        .flatMap { it.writeCharacteristic(characteristicUuid, inputBytes) }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ onWriteSuccess() }, { onWriteFailure(it) })
                        .let { connectionDisposableRead.add(it) }
            }
        }

        private fun onWriteSuccess() {
            callBackConnection?.writeSuccess()
        }


        private fun onWriteFailure(throwable: Throwable) {
            callBackConnection?.showErrorMessage(throwable.localizedMessage)
        }


        // discover

        private val discoveryDisposable = CompositeDisposable()

        fun onDiscoverClick() {

            bleDevice.establishConnection(false)
                    .flatMapSingle { it.discoverServices() }
                    .take(1) // Disconnect automatically after discovery
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe { callBackConnection?.discoverUpdateUI(!bleDevice.isConnected) }
                    .doFinally { callBackConnection?.discoverUpdateUI(!bleDevice.isConnected) }
                    .subscribe({
                        Log.e("discover_device__", it.toString())
                        callBackConnection?.discoverDeviceList(it)
                    }, {
                        callBackConnection?.showErrorMessage("Connection error: $it")
                    })
                    .let { discoveryDisposable.add(it) }
        }


        fun clearDiscoverDisposal() {

            discoveryDisposable.clear()
        }
    }

     fun newTest(method_as_param: (str:String) -> Unit) {
         // Here we get method as parameter and require 2 params and add value
         // to this two parameter which calculate and return expected value


         // print and see the result.
         println(method_as_param)


     }

}

