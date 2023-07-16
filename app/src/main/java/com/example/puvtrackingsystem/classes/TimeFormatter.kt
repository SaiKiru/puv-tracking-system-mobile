package com.example.puvtrackingsystem.classes


class TimeFormatter(hours: Double) {
    val _hours: Int
    val _minutes: Int
    val _seconds: Int

    init {
        _hours = hours.toInt()
        _minutes = (hours * 60).toInt() % 60
        _seconds = (hours * 60 * 60).toInt() % 60
    }

    fun getFormattedTime(): String {
        val hoursString = if (_hours == 1) "hour" else "hours"
        val minutesString = if (_minutes == 1) "minute" else "minutes"

        return buildString {
            if (_hours > 0) {
                append("$_hours $hoursString ")
            }
            append("$_minutes $minutesString")
        }
    }
}