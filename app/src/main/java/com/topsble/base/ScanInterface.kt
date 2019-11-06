package com.topsble.base


import com.polidea.rxandroidble2.scan.ScanResult

/**
 * MVVM Demo by Nirav Mehta 13-Sep-2019
 */

interface ScanInterface {

    fun disposeBLE()

    fun onBLEConnected(scanResult: ScanResult)

    fun showErrorMessage(throwable: Throwable)
}
