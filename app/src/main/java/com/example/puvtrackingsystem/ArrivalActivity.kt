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
import com.example.puvtrackingsystem.classes.TimeFormatter
import com.example.puvtrackingsystem.utils.calculateTravelTime
import java.util.Calendar
import kotlin.math.max

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
    private var nearestNodeIdx: Int? = null
    private var currentPuvSelection: Int? = null
    private var currentDestinationSelection: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_arrival)

        puvSpinner = findViewById(R.id.puv_spinner)
        destinationSpinner = findViewById(R.id.destination_spinner)
        puvContainer = findViewById(R.id.puv_container)
        etaTextTV = findViewById(R.id.eta_text_tv)
        etaGroup = findViewById(R.id.eta_group)

        puvDataKeys = intent.getIntArrayExtra("puvDataKeys")!!
        nearestNodeIdx = intent.getIntExtra("nearestNodeIdx", 0)
        currentPuvSelection = intent.getIntExtra("initialKey", -1)

        puvDataListener = object: DataManager.PUVDataListener {
            override fun run(data: Array<PUV>) {
                puvData = data

                if (currentPuv == null) {
                    currentPuv = DataManager.puvData?.get(currentPuvSelection!!)
                } else if (currentPuvSelection != null) {
                    currentPuv = data[currentPuvSelection!!]
                }

                puvDataKeys = DataManager.getPuvFiltered(max(currentPuv!!.nextStop - 1, nearestNodeIdx!!))

                populatePuvSpinner(puvDataKeys)
            }
        }

        bufferTimeListener = object : DataManager.BufferTimeListener {
            override fun run(data: Array<BufferTime>) {
                updateBufferTimes(data)
                updateEtaData(currentPuv, destinationNode)
            }
        }

        puvSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val key = puvDataKeys[position]

                currentPuvSelection = key
                currentPuv = puvData!![key]
                updatePuvData(currentPuv!!, key + 1)
                updateEtaData(currentPuv, destinationNode)
                populateDestinationSpinner(max(currentPuv!!.nextStop - 1, nearestNodeIdx!!))
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
                destinationNode = position + max(currentPuv!!.nextStop - 1, nearestNodeIdx!!)
                currentDestinationSelection = destinationNode
                updateEtaData(currentPuv, destinationNode)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }
    }

    override fun onResume() {
        super.onResume()

        DataManager.apply {
            addListener(puvDataListener)
            addListener(bufferTimeListener)
        }
    }

    override fun onPause() {
        super.onPause()

        DataManager.apply {
            removeListener(puvDataListener)
            removeListener(bufferTimeListener)
        }
    }

    private fun updatePuvData(puv: PUV, puvId: Int) {
        val bufferTime = getCurrentBufferTime()
        val puvFragment = PuvCardFragment.newInstance(puv, bufferTime, puvId)

        supportFragmentManager.beginTransaction().apply {
            replace(puvContainer.id, puvFragment)
            commit()
        }
    }

    private fun updateEtaData(puv: PUV?, stopNode: Int? = null) {
        if (puv == null || stopNode == null) return

        val bufferTime = getCurrentBufferTime()
        val distance = Map.measurePUVDistance(puv, stopNode)
        val eta = calculateTravelTime(distance, puv.speed) + bufferTime.value * 0.4 + (20 / 3600.0)

        if (eta == Double.POSITIVE_INFINITY) {
            etaTextTV.text = "---"
        } else {
            val time = TimeFormatter(eta)

            etaTextTV.text = time.getFormattedTime()
        }
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

        if (currentPuvSelection != null && dataSet.isNotEmpty()) {
            val idx = puvKeys.indexOf(currentPuvSelection!!)

            if (idx == -1) {
                puvSpinner.setSelection(0)
            } else {
                puvSpinner.setSelection(idx)
            }
        }
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

        if (currentDestinationSelection != null) {
            val idx = currentDestinationSelection!! - max(currentPuv!!.nextStop - 1, nearestNodeIdx!!)

            destinationSpinner.setSelection(idx)
        }
    }
}