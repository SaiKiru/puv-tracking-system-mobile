package com.example.puvtrackingsystem.classes

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.puvtrackingsystem.utils.isLocationEnabled
import com.example.puvtrackingsystem.utils.requestEnableLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.gson.Gson

object DataManager {
    var destination: Destination? =  null
    var location: Location? = null
    var puvData: Array<PUV>? = null
    var bufferTimes: Array<BufferTime>? = null

    private val locationUpdateListeners: MutableList<LocationDataListener> = arrayListOf()
    private val puvDataUpdateListeners: MutableList<PUVDataListener> = arrayListOf()
    private val bufferTimeUpdateListeners: MutableList<BufferTimeListener> = arrayListOf()

    var toastNetworkErrorShown = false

    fun initialize(context: Context) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        Scheduler.run(context, fusedLocationClient)
    }

    fun addListener(listener: LocationDataListener){
        locationUpdateListeners.add(listener)

        if (location != null) {
            listener.run(location)
        }
    }

    fun addListener(listener: PUVDataListener) {
        puvDataUpdateListeners.add(listener)

        if (puvData != null) {
            listener.run(puvData!!)
        }
    }

    fun addListener(listener: BufferTimeListener) {
        bufferTimeUpdateListeners.add(listener)

        if (bufferTimes != null) {
            listener.run(bufferTimes!!)
        }
    }

    fun removeListener(listener: LocationDataListener) {
        locationUpdateListeners.remove(listener)
    }

    fun removeListener(listener: PUVDataListener) {
        puvDataUpdateListeners.remove(listener)
    }

    fun removeListener(listener: BufferTimeListener) {
        bufferTimeUpdateListeners.remove(listener)
    }

    fun getPuvFiltered(targetNode: Int? = null): IntArray {
        var puvs: IntArray = intArrayOf()
        var target = targetNode

        if (puvData == null) return puvs

        puvs =
            if (destination == Destination.TOWN) {
                if (target == null) target = 16

                filterPuvs(puvData!!, target).toIntArray()
            } else {
                if (target == null) target = 33

                filterPuvs(puvData!!, target).toIntArray()
            }

        return puvs
    }

    private fun filterPuvs(puvs: Array<PUV>, stopNode: Int): Array<Int> {
        var filtered: Array<Int> = arrayOf()

        puvs.forEachIndexed { idx, puv,  ->
            if (stopNode <= 16
                && puv.nextStop - 1 <= stopNode
            ) { // To Town
                filtered = filtered.plus(idx)
            } else if (stopNode <= 33
                && puv.nextStop - 1 <= stopNode
                && puv.nextStop - 1 > 16
            ) { // To Home
                filtered = filtered.plus(idx)
            }
        }

        return filtered
    }

    private fun getLocation(context: Context, locationClient: FusedLocationProviderClient) {
        if (!isLocationEnabled(context)) {
            requestEnableLocation(context)
            return
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        locationClient
            .getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, object : CancellationToken() {
                override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token
                override fun isCancellationRequested() = false
            })
            .addOnSuccessListener { location: Location? ->
                this@DataManager.location = location
                locationUpdateListeners.forEach { listener -> listener.run(location) }
            }
    }

    private fun getPUVData(context: Context, handler: Handler, task: Runnable) {
        API.getPUVSummary(
            callback = { response ->
                val data = Gson().fromJson(response, Array<PUV>::class.java)
                this@DataManager.puvData = data

                puvDataUpdateListeners.forEach { listener -> listener.run(data) }
            },
            errorHandler = {
                if (!toastNetworkErrorShown) {
                    Toast
                        .makeText(context, "Could not get data. Are you connected to the internet?", Toast.LENGTH_LONG)
                        .show()

                    toastNetworkErrorShown = true

                    Handler(Looper.getMainLooper()).postDelayed({
                        toastNetworkErrorShown = false
                    }, 3500)
                }

                handler.removeCallbacks(task)
                handler.post(task)
            }
        )
    }

    private fun getBufferTimes(context: Context, handler: Handler, task: Runnable) {
        API.getBufferTimes(
            callback = { response ->
                val data = Gson().fromJson(response, Array<BufferTime>::class.java)
                this@DataManager.bufferTimes = data

                bufferTimeUpdateListeners.forEach { listener -> listener.run(data) }
            },
            errorHandler = {
                if (!toastNetworkErrorShown) {
                    Toast
                        .makeText(context, "Could not get data. Are you connected to the internet?", Toast.LENGTH_LONG)
                        .show()

                    toastNetworkErrorShown = true

                    Handler(Looper.getMainLooper()).postDelayed({
                        toastNetworkErrorShown = false
                    }, 3500)
                }

                handler.removeCallbacks(task)
                handler.post(task)
            }
        )
    }

    enum class Destination {
        HOME,
        TOWN,
    }

    interface DataListener<T> {
        fun run(data: T)
    }

    interface LocationDataListener: DataListener<Location?> {
        override fun run(data: Location?)
    }

    interface PUVDataListener: DataListener<Array<PUV>> {
        override fun run(data: Array<PUV>)
    }

    interface BufferTimeListener: DataListener<Array<BufferTime>> {
        override fun run(data: Array<BufferTime>)
    }

    private object Scheduler {
        fun run(context: Context, locationClient: FusedLocationProviderClient) {
            scheduleLocationUpdates(3_000, context, locationClient)
            schedulePUVUpdates(1_000 * 20, context)
            scheduleBufferTimeUpdates(1_000 * 60 * 10, context)
        }

        fun scheduleLocationUpdates(time: Long, context: Context, locationClient: FusedLocationProviderClient) {
            val handler = Handler(Looper.getMainLooper())
            val runnable = object: Runnable {
                override fun run() {
                    getLocation(context, locationClient)
                    handler.postDelayed(this, time)
                }
            }

            handler.post(runnable)
        }

        fun schedulePUVUpdates(time: Long, context: Context) {
            val handler = Handler(Looper.getMainLooper())
            val runnable = object: Runnable {
                override fun run() {
                    getPUVData(context, handler, this)
                    handler.postDelayed(this, time)
                }
            }

            handler.post(runnable)
        }

        fun scheduleBufferTimeUpdates(time: Long, context: Context) {
            val handler = Handler(Looper.getMainLooper())
            val runnable = object: Runnable {
                override fun run() {
                    getBufferTimes(context, handler, this)
                    handler.postDelayed(this, time)
                }
            }

            handler.post(runnable)
        }
    }
}