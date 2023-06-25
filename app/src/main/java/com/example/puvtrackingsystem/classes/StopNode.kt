package com.example.puvtrackingsystem.classes

class StopNode(latitude: Double, longitude: Double) {
    lateinit var coordinates: Coordinates

    init {
        this.coordinates = Coordinates(latitude, longitude)
    }
}