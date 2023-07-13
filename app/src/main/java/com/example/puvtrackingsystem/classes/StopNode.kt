package com.example.puvtrackingsystem.classes

class StopNode(
    var name: String,
    latitude: Double,
    longitude: Double
) {
    var coordinates: Coordinates

    init {
        this.coordinates = Coordinates(latitude, longitude)
    }
}