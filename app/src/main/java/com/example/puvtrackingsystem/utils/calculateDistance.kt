package com.example.puvtrackingsystem.utils

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

fun calculateDistance(lat1: Double, long1: Double, lat2: Double, long2: Double): Double {
    return _calculateDistance(
        Math.toRadians(lat1),
        Math.toRadians(long1),
        Math.toRadians(lat2),
        Math.toRadians(long2),
    );
}

private fun _calculateDistance(lat1: Double, long1: Double, lat2: Double, long2: Double): Double {
    val EARTH_RADIUS = 6371;
    val latDiff = lat2 - lat1;
    val longDiff = long2 - long1;

    val haversine = sin(latDiff / 2.0).pow(2) + cos(lat1) * cos(lat2) * sin(longDiff / 2.0).pow(2)
    val centralAngle = 2 * asin(sqrt(haversine));

    return EARTH_RADIUS * centralAngle;
}