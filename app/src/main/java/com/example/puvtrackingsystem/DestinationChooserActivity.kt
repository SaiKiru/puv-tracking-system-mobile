package com.example.puvtrackingsystem

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
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
    }

    private fun toBoarding() {
        Intent(this, BoardingActivity::class.java).also {
            startActivity(it)
        }
    }
}