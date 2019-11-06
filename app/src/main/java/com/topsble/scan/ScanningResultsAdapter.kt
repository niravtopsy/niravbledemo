package com.topsble.scan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.polidea.rxandroidble2.scan.ScanResult
import com.topsble.R
import java.util.*

internal class ScanningResultsAdapter(
    private val onClickListener: (ScanResult) -> Unit
) : RecyclerView.Adapter<ScanningResultsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var tvDeviceName: TextView? = itemView.findViewById(R.id.tvDeviceName)
        var tvDeviceMac: TextView? = itemView.findViewById(R.id.tvDeviceMac)
        var tvDeviceRSSI: TextView? = itemView.findViewById(R.id.tvDeviceRSSI)

    }

    private val data = mutableListOf<ScanResult>()

    fun addScanResult(bleScanResult: ScanResult) {
        // Not the best way to ensure distinct devices, just for the sake of the demo.
        data.withIndex()
            .firstOrNull { it.value.bleDevice == bleScanResult.bleDevice }
            ?.let {
                // device already in data list => update
                data[it.index] = bleScanResult
                notifyItemChanged(it.index)
            }
            ?: run {
                // new device => add to data list
                with(data) {
                    add(bleScanResult)
                    sortBy { it.bleDevice.macAddress }
                }
                notifyDataSetChanged()
            }
    }

    fun clearScanResults() {
        data.clear()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(data[position]) {

            if (bleDevice.name == null) {
                holder.tvDeviceName!!.text = "Device Name : Unknown"
            } else {
                holder.tvDeviceName!!.text = "Device Name : " + String.format(Locale.getDefault(), "%s", bleDevice.name!!)
            }

            holder.tvDeviceMac!!.text = "Device Mac : " + String.format(Locale.getDefault(), "%s", bleDevice.macAddress)
            holder.tvDeviceRSSI!!.text = "Device RSSI : " + String.format(Locale.getDefault(), "%d", rssi)

            holder.itemView.setOnClickListener { onClickListener(this) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        LayoutInflater.from(parent.context)
            .inflate(R.layout.inflator_scan_result, parent, false)
            .let { ViewHolder(it) }
}
