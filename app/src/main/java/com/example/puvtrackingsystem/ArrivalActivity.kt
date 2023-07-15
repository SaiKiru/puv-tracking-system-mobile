package com.example.puvtrackingsystem

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import com.example.puvtrackingsystem.classes.BufferTime
import com.example.puvtrackingsystem.classes.DataManager
import com.example.puvtrackingsystem.classes.Map
import com.example.puvtrackingsystem.classes.PUV
import com.example.puvtrackingsystem.utils.calculateTravelTime
import java.util.Calendar

class ArrivalActivity : AppCompatActivity() {
    private lateinit var puvSpinner: Spinner
    private lateinit var destinationSpinner: Spinner
    private lateinit var puvContainer: FrameLayout
    private lateinit var etaTextTV: TextView
    private lateinit var etaGroup: LinearLayout

    // Data Listeners
    private lateinit var puvDataListener: DataManager.PUVDataListener
    private lateinit var bufferTimeListener: DataManager.BufferTimeListener

    // Data state
    private var puvData: Array<PUV>? = null
    private var bufferTimes: Array<BufferTime>? = null
    private var currentPuv: PUV? = null
    private var destinationNode: Int? = null

    // Util variables
    private lateinit var puvDataKeys: IntArray


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_arrival)

        puvSpinner = findViewById(R.id.puv_spinner)
        destinationSpinner = findViewById(R.id.destination_spinner)
        puvContainer = findViewById(R.id.puv_container)
        etaTextTV = findViewById(R.id.eta_text_tv)
        etaGroup = findViewById(R.id.eta_group)

        puvDataKeys = intent.getIntArrayExtra("puvDataKeys")!!

        puvDataListener = object: DataManager.PUVDataListener {
            override fun run(data: Array<PUV>) {
                puvData = data
                populatePuvSpinner(puvDataKeys)
            }
        }

        bufferTimeListener = object : DataManager.BufferTimeListener {
            override fun run(data: Array<BufferTime>) {
                updateBufferTimes(data)
                updateEtaData(currentPuv, destinationNode)
            }
        }

        DataManager.apply {
            addListener(puvDataListener)
            addListener(bufferTimeListener)
        }

        puvSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val key = puvDataKeys[position]

                currentPuv = puvData!![key]
                updatePuvData(currentPuv!!)
                updateEtaData(currentPuv, destinationNode)
                populateDestinationSpinner(currentPuv!!.nextStop)
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
                // TODO: offset
                destinationNode = position
                updateEtaData(currentPuv, destinationNode)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        DataManager.apply {
            removeListener(puvDataListener)
            removeListener(bufferTimeListener)
        }
    }

    private fun updatePuvData(puv: PUV) {

    }

    private fun updateEtaData(puv: PUV?, stopNode: Int? = null) {
        if (puv == null || stopNode == null) return

        val bufferTime = getCurrentBufferTime()
        val distance = Map.measurePUVDistance(puv, stopNode)
        val eta = calculateTravelTime(distance, puv.speed) + bufferTime.value * 0.1

        etaTextTV.text = "${(eta * 60).toInt()} minutes"
    }

    private fun updateBufferTimes(data: Array<BufferTime>) {
        bufferTimes = data
    }

    private fun getCurrentBufferTime(): BufferTime {
        val bufferTime: BufferTime =
            if (bufferTimes == null) {
                BufferTime(0.0, 1)
            } else {
                val cal = Calendar.getInstance()
                val currentHour = cal.get(Calendar.HOUR_OF_DAY)
                bufferTimes!![(currentHour + 23) % 24]
            }

        return bufferTime
    }

    private fun populatePuvSpinner(puvKeys: IntArray) {
        var dataSet: Array<String> = arrayOf()

        puvKeys.forEach { key -> dataSet = dataSet.plus("PUV ${key + 1}") }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, dataSet)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        puvSpinner.adapter = adapter
    }

    private fun populateDestinationSpinner(initial: Int) {
        val destinationList: List<String> =
            if (DataManager.destination == DataManager.Destination.HOME) {
                Map.getHomeStops(initial)
            } else {
                Map.getTownStops(initial)
            }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, destinationList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        destinationSpinner.adapter = adapter
    }
}