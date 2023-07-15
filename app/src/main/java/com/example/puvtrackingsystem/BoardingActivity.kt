package com.example.puvtrackingsystem

import android.content.Intent
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.example.puvtrackingsystem.classes.BufferTime
import com.example.puvtrackingsystem.classes.Coordinates
import com.example.puvtrackingsystem.classes.DataManager
import com.example.puvtrackingsystem.classes.DataManager.LocationDataListener
import com.example.puvtrackingsystem.classes.DataManager.PUVDataListener
import com.example.puvtrackingsystem.classes.DataManager.BufferTimeListener
import com.example.puvtrackingsystem.classes.Map
import com.example.puvtrackingsystem.classes.PUV
import com.example.puvtrackingsystem.classes.StopNode
import com.example.puvtrackingsystem.utils.calculateTravelTime
import java.util.Calendar


class BoardingActivity : AppCompatActivity() {
    // Waiting for PUV View Group
    private lateinit var stopNodeLabelTV: TextView
    private lateinit var stopNodeNameTV: TextView
    private lateinit var etaTextTV: TextView
    private lateinit var nearestPUVContainer: FrameLayout

    // Boarding a PUV View Group
    private lateinit var boardingGroup: ConstraintLayout
    private lateinit var boardConfirmationTextTV: TextView
    private lateinit var confirmationPuvContainer: FrameLayout
    private lateinit var boardingBtn: Button
    private lateinit var ignoreBoardingBtn: Button

    // Data Listeners
    private lateinit var locationDataListener: LocationDataListener
    private lateinit var puvDataListener: PUVDataListener
    private lateinit var bufferTimeListener: BufferTimeListener

    // Data state
    private var location: Location? = null
    private var puvData: Array<PUV>? = null
    private var bufferTimes: Array<BufferTime>? = null
    private var nearestPuv: PUV? = null
    private var nearestNode: StopNode? = null
    private var nearestNodeIdx: Int? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_boarding)

        stopNodeLabelTV = findViewById(R.id.stop_node_label_tv)
        stopNodeNameTV = findViewById(R.id.stop_node_name_tv)
        etaTextTV = findViewById(R.id.eta_text_tv)
        nearestPUVContainer = findViewById(R.id.nearest_puv_container)
        boardingGroup = findViewById(R.id.boarding_group)
        boardConfirmationTextTV = findViewById(R.id.board_confirmation_text_tv)
        confirmationPuvContainer = findViewById(R.id.confirmation_puv_container)
        boardingBtn = findViewById(R.id.boarding_btn)
        ignoreBoardingBtn = findViewById(R.id.ignore_boarding_btn)

        // Create data listeners
        locationDataListener = object : LocationDataListener {
            override fun run(data: Location?) {
                location = data
                if (data != null) {
                    nearestNode = getNearestNode(data)
                    updateNearestNode(nearestNode!!)
                    updateEtaData(puvData, nearestNodeIdx)
                }
            }
        }

        puvDataListener = object : PUVDataListener {
            override fun run(data: Array<PUV>) {
                updateEtaData(data, nearestNodeIdx)
            }
        }

        bufferTimeListener = object : BufferTimeListener {
            override fun run(data: Array<BufferTime>) {
                updateBufferTimes(data)
                updateEtaData(puvData, nearestNodeIdx)
            }
        }

        ignoreBoardingBtn.setOnClickListener {
            boardingGroup.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()

        DataManager.apply {
            addListener(locationDataListener)
            addListener(puvDataListener)
            addListener(bufferTimeListener)
        }
    }

    override fun onPause() {
        super.onPause()

        boardingGroup.visibility = View.GONE

        DataManager.apply {
            removeListener(locationDataListener)
            removeListener(puvDataListener)
            removeListener(bufferTimeListener)
        }
    }

    override fun onBackPressed() {
        if (boardingGroup.isVisible) {
            boardingGroup.visibility = View.GONE
        } else {
            super.onBackPressed()
        }
    }

    private fun updatePuvData(puv: PUV, key: Int) {
        val nearestFragment = PuvCardFragment.newInstance(puv)
        val confirmationFragment = PuvCardFragment.newInstance(puv)

        nearestFragment.setOnClickListener {
            boardingGroup.visibility = View.VISIBLE

            boardingBtn.setOnClickListener {
                Intent(this, ArrivalActivity::class.java).also {
                    it.putExtra("puvDataKeys", DataManager.getPuvFiltered())
                    it.putExtra("initialKey", key)
                    startActivity(it)
                }
            }
        }

        supportFragmentManager.beginTransaction().apply {
            replace(nearestPUVContainer.id, nearestFragment)
            replace(confirmationPuvContainer.id, confirmationFragment)
            commit()
        }
    }

    private fun updateEtaData(puvData: Array<PUV>?, stopNode: Int? = null) {
        if (puvData == null || stopNode == null) return

        val filter = DataManager.getPuvFiltered(stopNode)
        var puvs: Array<PUV> = arrayOf()

        filter.forEach { mask -> puvs = puvs.plus(puvData[mask]) }

        if (puvs.isEmpty()) {
            // TODO Handle no PUVs
            return
        }

        val puv =
            if (puvs.size == 1) {
                updatePuvData(puvs[0], stopNode)
                puvs[0]
            } else {
                getNearestPuv(puvs, stopNode)
            }

        val bufferTime = getCurrentBufferTime()
        val distance = Map.measurePUVDistance(puv, stopNode)
        val eta = calculateTravelTime(distance, puv.speed) + bufferTime.value * 0.1

        etaTextTV.text = "${(eta * 60).toInt()} minutes"
    }

    /**
     * Removes PUVs that are not currently going to user's location
     * @param puvs The PUV list to filter
     * @param stopNode The user's location
     * @return The list of PUVs that can go to user's location
     */
    private fun filterPUVS(puvs: Array<PUV>, stopNode: Int): Array<PUV> {
        val filtered: Array<PUV> = arrayOf()

        puvs.forEach { puv ->
            if (stopNode <= 16 && puv.nextStop <= stopNode) { // To Town
                filtered.plus(puv)
            } else if (stopNode <= 33 && puv.nextStop <= stopNode) { // To Home
                filtered.plus(puv)
            }
        }

        return filtered
    }

    private fun getNearestPuv(puvData: Array<PUV>, stopNode: Int): PUV {
        nearestPuv = puvData[0]
        var idx = 0
        val distance = Map.measurePUVDistance(nearestPuv!!, stopNode)
        var shortestTravelTime = calculateTravelTime(distance, nearestPuv!!.speed)

        for (i in 1 until puvData.size) {
            val puv = puvData[i]
            idx = i
            val distance = Map.measurePUVDistance(puv, stopNode)
            val travelTime = calculateTravelTime(distance, puv.speed)

            if (travelTime < shortestTravelTime) {
                nearestPuv = puv
                shortestTravelTime = travelTime
            }
        }

        updatePuvData(nearestPuv!!, idx)

        return nearestPuv!!
    }

    private fun updateNearestNode(node: StopNode) {
        stopNodeNameTV.text = node.name
    }

    private fun getNearestNode(location: Location): StopNode {
        val start =
            if (DataManager.destination == DataManager.Destination.TOWN) {
                0
            } else {
                17
            }

        val currentLocation = Coordinates(location.latitude, location.longitude)
        nearestNode = Map.routes[start]
        nearestNodeIdx = start
        var nearestDistance = currentLocation.distanceTo(nearestNode!!.coordinates)

        for (i in (start + 1) until Map.routes.size) {
            val node = Map.routes[i]
            val distance = currentLocation.distanceTo(node.coordinates)

            if (distance < nearestDistance) {
                nearestNode = node
                nearestNodeIdx = i
                nearestDistance = distance
            }
        }

        return nearestNode!!
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
}