package com.example.puvtrackingsystem

import HttpGetRequestAsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import com.example.puvtrackingsystem.classes.BufferTime
import com.example.puvtrackingsystem.classes.PUV
import com.example.puvtrackingsystem.constants.getStopNodes
import com.example.puvtrackingsystem.utils.calculateTravelTime
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
    private var currentStopNode: String = "Ambiong"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_puvdetails)

        fromSpinner = findViewById(R.id.from_spinnner)
        destinationSpinner = findViewById(R.id.destination_spinner)
        passengersTV = findViewById(R.id.passengers_tv)
        etaTV = findViewById(R.id.eta_tv)

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

        getBufferTimes()
        schedulePUVDataUpdates()
    }

    private fun schedulePUVDataUpdates() {
        val handler  = Handler(Looper.getMainLooper())
        val runnable = object: Runnable {
            override fun run() {
                getPUVData()
                handler.postDelayed(this, 20_000)
            }
        }

        handler.postDelayed(runnable, 10)
    }

    private fun updatePUVDetails() {
        if (bufferTimes == null || currentPuv == null) return

        val cal = Calendar.getInstance()
        val currentHour = cal.get(Calendar.HOUR_OF_DAY)
        val bufferTime = bufferTimes!![(currentHour + 23) % 24]
        val distance = currentPuv!!.coordinates.distanceTo(getStopNodes()[currentStopNode]!!.coordinates)
        val expectedTime = calculateTravelTime(distance, currentPuv!!.speed) + bufferTime.value * 0.1

        passengersTV.text = currentPuv!!.passengersOnboard.toString()
        etaTV.text = "${(expectedTime * 60).toInt()} minutes"
    }

    private fun getBufferTimes() {
        HttpGetRequestAsyncTask { response ->
            val type = Array<BufferTime>::class.java
            val data = Gson().fromJson(response, type)
            bufferTimes = data

            updatePUVDetails()
        }.execute("https://script.google.com/macros/s/AKfycbwxvuUBkTmxkQcjLxvFmdfcfpwBNnCtVpAcNrQLwhOmarXORfxM05HpAExU_x9cVeQQ/exec?action=getBufferTimes")
    }

    private fun getPUVData() {
        HttpGetRequestAsyncTask { response ->
            val type = Array<PUV>::class.java
            val data = Gson().fromJson(response, type)

            puvs = data
            when (puv) {
                "PUV1" -> currentPuv = puvs!![0]
                "PUV2" -> currentPuv = puvs!![1]
                "PUV3" -> currentPuv = puvs!![2]
            }

            updatePUVDetails()
        }.execute("https://script.google.com/macros/s/AKfycbwxvuUBkTmxkQcjLxvFmdfcfpwBNnCtVpAcNrQLwhOmarXORfxM05HpAExU_x9cVeQQ/exec?action=getPUVSummary")
    }
}