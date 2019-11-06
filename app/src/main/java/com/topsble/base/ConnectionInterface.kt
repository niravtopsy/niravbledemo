package com.topsble.base

import com.polidea.rxandroidble2.RxBleDeviceServices


/**
 * MVVM Demo by Nirav Mehta 13-Sep-2019
 */

interface ConnectionInterface {

    fun connectBLE()

    fun disConnectBLE()

    fun showErrorMessage(error: String)

    fun discoverUpdateUI(isConnected: Boolean)

    fun discoverDeviceList(rxBleDiscoverDevice: RxBleDeviceServices)

    fun writeSuccess()
}
