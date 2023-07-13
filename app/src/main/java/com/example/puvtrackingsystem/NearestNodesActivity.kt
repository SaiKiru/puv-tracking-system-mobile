package com.example.puvtrackingsystem

import HttpGetRequestAsyncTask
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.puvtrackingsystem.classes.BufferTime
import com.example.puvtrackingsystem.classes.Coordinates
import com.example.puvtrackingsystem.classes.PUV
import com.example.puvtrackingsystem.classes.StopNode
import com.example.puvtrackingsystem.utils.API
import com.example.puvtrackingsystem.utils.calculateTravelTime
import com.example.puvtrackingsystem.utils.isLocationEnabled
import com.example.puvtrackingsystem.utils.requestEnableLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.gson.Gson
import java.util.Calendar

class NearestNodesActivity : AppCompatActivity() {
    private lateinit var nearestStopTV: TextView
    private lateinit var nearestEtaTV: TextView
    private var bufferTimes: Array<BufferTime>? = null
    private var puvs: Array<PUV>? = null
    private var nearestStop: StopNode? = null
    private var nearestStopName: String? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationScheduler: Handler
    private lateinit var puvDataScheduler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nearest_nodes)

        nearestStopTV = findViewById(R.id.nearest_stop_tv)
        nearestEtaTV = findViewById(R.id.nearest_eta_tv)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        getBufferTimes()
        scheduleLocationUpdates()
        schedulePUVDataUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        locationScheduler.removeCallbacksAndMessages(null)
        puvDataScheduler.removeCallbacksAndMessages(null)
    }

    private fun scheduleLocationUpdates() {
        locationScheduler  = Handler(Looper.getMainLooper())
        val runnable = object: Runnable {
            override fun run() {
                requestLocation()
                locationScheduler.postDelayed(this, 5_000)
            }
        }

        locationScheduler.postDelayed(runnable, 10)
    }

    private fun schedulePUVDataUpdates() {
        puvDataScheduler  = Handler(Looper.getMainLooper())
        val runnable = object: Runnable {
            override fun run() {
                getPUVData()
                puvDataScheduler.postDelayed(this, 20_000)
            }
        }

        puvDataScheduler.postDelayed(runnable, 10)
    }

    private fun requestLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) return

        if (!isLocationEnabled(this)) {
            requestEnableLocation(this)
            finish()
            return
        }

        fusedLocationClient
            .getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, object : CancellationToken() {
                override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token

                override fun isCancellationRequested() = false
            })
            .addOnSuccessListener { location: Location? ->
                if (location == null) {
                    Toast
                        .makeText(this, "Could not get location. Turn on location services and internet :)", Toast.LENGTH_LONG)
                        .show()
                    return@addOnSuccessListener
                };

                val currentLocation = Coordinates(location.latitude, location.longitude)
                var nearestStop = getStopNodes().values.first()
                nearestStopName = getStopNodes().keys.first()
                var nearestDistance = currentLocation.distanceTo(nearestStop.coordinates)

                for (i in 1 until getStopNodes().values.size) {
                    val stopNode = getStopNodes().values.toList()[i]

                    val currentEvaluated = currentLocation.distanceTo(stopNode.coordinates)
                    if (currentEvaluated < nearestDistance) {
                        nearestStop = stopNode
                        nearestDistance = currentEvaluated
                        nearestStopName = getStopNodes().keys.toList()[i]
                    }
                }

                this@NearestNodesActivity.nearestStop = nearestStop
            }
    }

    private fun updateDetails() {
        if (bufferTimes == null || puvs == null || nearestStop == null) return

        val cal = Calendar.getInstance()
        val currentHour = cal.get(Calendar.HOUR_OF_DAY)
        val bufferTime = bufferTimes!![(currentHour + 23) % 24]
        var nearestPUV: PUV = puvs!![0];
        var expectedTime = calculateTravelTime(nearestPUV.coordinates.distanceTo(nearestStop!!.coordinates), nearestPUV.speed) + bufferTime.value * 0.1

        for (i in 1 until puvs!!.size) {
            val puv = puvs!![i]

            val currentEvaluated = calculateTravelTime(puv.coordinates.distanceTo(nearestStop!!.coordinates), puv.speed) + bufferTime.value * 0.1

            if (currentEvaluated < expectedTime) {
                nearestPUV = puv
                expectedTime = currentEvaluated
            }
        }

        nearestStopTV.text = nearestStopName
        nearestEtaTV.text = "${(expectedTime * 60).toInt()} minutes"
    }

    private fun getBufferTimes() {
        API.getBufferTimes(
            callback = { response ->
                val type = Array<BufferTime>::class.java
                val data = Gson().fromJson(response, type)
                bufferTimes = data

                updateDetails()
            },
            errorHandler = {
                showConnectionErrorToast()
            }
        )
    }

    private fun getPUVData() {
        API.getPUVSummary(
            callback = { response ->
                val type = Array<PUV>::class.java
                val data = Gson().fromJson(response, type)

                puvs = data

                updateDetails()
            },
            errorHandler = {
                showConnectionErrorToast()
            }
        )
    }

    private fun showConnectionErrorToast() {
        Toast
            .makeText(this, "Could not connect to the internet.", Toast.LENGTH_LONG)
            .show()
    }
}