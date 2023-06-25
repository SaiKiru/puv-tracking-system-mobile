package com.example.puvtrackingsystem

import HttpGetRequestAsyncTask
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.puvtrackingsystem.classes.BufferTime
import com.example.puvtrackingsystem.classes.PUV
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {
    private lateinit var puvArrivalBtn: Button
    private lateinit var stopNodesBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestLocationPermissions()

        puvArrivalBtn = findViewById(R.id.puv_arrival_btn)
        puvArrivalBtn.setOnClickListener {
            Intent(this, PUVDetailsActivity::class.java).also {
                startActivity(it)
            }
        }

        stopNodesBtn = findViewById(R.id.stop_nodes_btn)
        stopNodesBtn.setOnClickListener {
            Intent(this, NearestNodesActivity::class.java).also {
                startActivity(it)
            }
        }
    }

    private fun requestLocationPermissions() {
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    // Precise location access granted.
                }
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    // Only approximate location access granted.
                } else -> {
                    // No location access granted.
                }
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            locationPermissionRequest.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION));
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            locationPermissionRequest.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION));
        }
    }
}
