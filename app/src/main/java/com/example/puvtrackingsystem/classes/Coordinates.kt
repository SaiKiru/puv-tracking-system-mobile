package com.example.puvtrackingsystem.classes

import com.example.puvtrackingsystem.utils.calculateDistance
import com.example.puvtrackingsystem.utils.calculateTravelTime

class Coordinates(
    val latitude: Double,
    val longitude: Double
) {

    /**
     * Measures the distance from this coordinate to another
     * @param coordinates {Coordinates}
     */
    fun distanceTo(coordinates: Coordinates): Double {
        return calculateDistance(this.latitude, this.longitude, coordinates.latitude, coordinates.longitude);
    }

    /**
     * Measures the time it takes to travel from one coordinate to another
     * @param coordinates {Coordinates}
     * @param speed {number}
     */
    fun travelTimeTo(coordinates: Coordinates, speed: Double): Double {
        val distance = this.distanceTo(coordinates);
        return calculateTravelTime(distance, speed);
    }
}