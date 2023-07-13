package com.example.puvtrackingsystem

import HttpGetRequestAsyncTask
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.puvtrackingsystem.classes.BufferTime
import com.example.puvtrackingsystem.classes.Coordinates
import com.example.puvtrackingsystem.classes.PUV
import com.example.puvtrackingsystem.utils.API
import com.example.puvtrackingsystem.utils.calculateDistance
import com.example.puvtrackingsystem.utils.calculateTravelTime
import com.example.puvtrackingsystem.utils.isLocationEnabled
import com.example.puvtrackingsystem.utils.requestEnableLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.gson.Gson
import java.util.Calendar

class PUVDetailsActivity : AppCompatActivity() {
    private lateinit var fromSpinner: Spinner
    private lateinit var destinationSpinner: Spinner
    private lateinit var passengersTV: TextView
    private lateinit var etaTV: TextView
    private var puv: String = "PUV1"
    private var stopNodes: String = "Ambiong"
    private var currentPuv: PUV? = null
    private var bufferTimes: Array<BufferTime>? = null
    private var puvs: Array<PUV>? = null
    private var currentLocation: Coordinates? = null
    private var currentStopNode: String = "Ambiong"
    private lateinit var puvDataScheduler: Handler
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var puvHasInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_puvdetails)

        fromSpinner = findViewById(R.id.from_spinnner)
        destinationSpinner = findViewById(R.id.destination_spinner)
        passengersTV = findViewById(R.id.passengers_tv)
        etaTV = findViewById(R.id.eta_tv)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        ArrayAdapter.createFromResource(
            this,
            R.array.puvs,
            android.R.layout.simple_spinner_item
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            fromSpinner.adapter = it
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.stop_nodes,
            android.R.layout.simple_spinner_item
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            destinationSpinner.adapter = it
        }

        fromSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedValue = parent?.getItemAtPosition(position).toString()
                when (selectedValue) {
                    "PUV 1" -> puv = "PUV1"
                    "PUV 2" -> puv = "PUV2"
                    "PUV 3" -> puv = "PUV3"
                }

                if (puvs == null) return

                when (selectedValue) {
                    "PUV 1" -> currentPuv = puvs!![0]
                    "PUV 2" -> currentPuv = puvs!![1]
                    "PUV 3" -> currentPuv = puvs!![2]
                }

                updatePUVDetails()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }

        fromSpinner.isEnabled = false

        destinationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedValue = parent?.getItemAtPosition(position).toString()
                currentStopNode = selectedValue
                updatePUVDetails()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }

        requestLocation()
        getBufferTimes()
        schedulePUVDataUpdates()
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

                currentLocation = Coordinates(location.latitude, location.longitude)
                updatePUVDetails()
            }
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

    private fun updatePUVDetails() {
        if (bufferTimes == null || currentPuv == null || currentLocation == null) return

        if (!puvHasInitialized) {
            var nearest = puvs!![0]
            var nearestDistance = currentLocation!!.distanceTo(nearest.coordinates)
            var idx = 0

            for (i in 1 until puvs!!.size) {
                val puv = puvs!![i]

                val currentEvaluated = currentLocation!!.distanceTo(puv.coordinates)
                if (currentEvaluated < nearestDistance) {
                    nearest = puv
                    nearestDistance = currentEvaluated
                    idx = i
                }
            }

            currentPuv = nearest
            fromSpinner.setSelection(idx)

            puvHasInitialized = true
            fromSpinner.isEnabled = true
        }

        val cal = Calendar.getInstance()
        val currentHour = cal.get(Calendar.HOUR_OF_DAY)
        val bufferTime = bufferTimes!![(currentHour + 23) % 24]
        val distance = currentPuv!!.coordinates.distanceTo(getStopNodes()[currentStopNode]!!.coordinates)
        val expectedTime = calculateTravelTime(distance, currentPuv!!.speed) + bufferTime.value * 0.1

        passengersTV.text = currentPuv!!.passengersOnboard.toString()
        etaTV.text = "${(expectedTime * 60).toInt()} minutes"
    }

    private fun getBufferTimes() {
        API.getBufferTimes(
            callback = { response ->
                var type = Array<BufferTime>::class.java
                val data = Gson().fromJson(response, type)
                bufferTimes = data

                updatePUVDetails()
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
                when (puv) {
                    "PUV1" -> currentPuv = puvs!![0]
                    "PUV2" -> currentPuv = puvs!![1]
                    "PUV3" -> currentPuv = puvs!![2]
                }

                updatePUVDetails()
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