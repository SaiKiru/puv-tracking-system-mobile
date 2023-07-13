package com.example.puvtrackingsystem.classes

import java.util.Calendar
import java.util.Date

class PUV(
    time: String,
    latitude: Double,
    longitude: Double,
    val speed: Double,
    month: Int,
    day: Int,
    year: Int,
    val boardingCount: Int,
    val alightingCount: Int,
    val passengersOnboard: Int,
    val nextStop: Int = 1,
    val node: String = "",
) {
    lateinit var coordinates: Coordinates
    lateinit var dateTime: Date
    
    init {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, day, time.substringBefore(":").toInt(), time.substringAfter(":").toInt())
        this.dateTime = calendar.time
        this.coordinates = Coordinates(latitude, longitude)
    }

    /** Combine PUV data with another PUV. This averages the speed and positions of minute movements
     *  @param puv {PUV}
     */
    fun combine(puv: PUV): PUV {
        val midpoint = Coordinates(
            (this.coordinates.latitude + puv.coordinates.latitude) / 2,
            (this.coordinates.longitude + puv.coordinates.longitude) / 2
        )

        val averageSpeed = (this.speed + puv.speed) / 2;

        return PUV(
            "${this.dateTime.hours}:${this.dateTime.minutes}",
            midpoint.latitude,
            midpoint.longitude,
            averageSpeed,
            this.dateTime.month + 1,
            this.dateTime.date,
            this.dateTime.year,
            puv.boardingCount,
            puv.alightingCount,
            puv.passengersOnboard,
        );
    }
}