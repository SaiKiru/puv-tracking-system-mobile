package com.example.puvtrackingsystem

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.core.app.ActivityCompat
import com.example.puvtrackingsystem.classes.DataManager
import com.example.puvtrackingsystem.classes.DataManager.Destination

class DestinationChooserActivity : AppCompatActivity() {
    private lateinit var toAuroraButton: Button
    private lateinit var toTownButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_destination_chooser)

        toAuroraButton = findViewById(R.id.to_aurora_btn)
        toTownButton = findViewById(R.id.to_town_btn)

        toAuroraButton.setOnClickListener {
            DataManager.destination = Destination.HOME
            toBoarding()
        }

        toTownButton.setOnClickListener {
            DataManager.destination = Destination.TOWN
            toBoarding()
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ), 10)

            return
        }

        DataManager.initialize(this)
    }

    private fun toBoarding() {
        Intent(this, BoardingActivity::class.java).also {
            startActivity(it)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 10) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                DataManager.initialize(this)
            } else {
                finish()
            }
        }
    }
}