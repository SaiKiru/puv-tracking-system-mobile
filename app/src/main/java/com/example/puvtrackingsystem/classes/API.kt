package com.example.puvtrackingsystem.classes

import HttpGetRequestAsyncTask

object API {
    val BASE_URL = "https://script.google.com/macros/s/AKfycbz3k6LYUMBQmJ_GxXkkoCMIgj2PE0qSp8YvueVhitcl54inM6E90KEqKspib-24jZkX/exec"

    fun getAllPUVData(
        callback: ((String) -> Unit)? = null,
        errorHandler: (() -> Unit)? = null
    ) {
        HttpGetRequestAsyncTask(callback, errorHandler)
            .execute("$BASE_URL?action=getAllPUVData")
    }

    fun getBufferTimes(
        callback: ((String) -> Unit)? = null,
        errorHandler: (() -> Unit)? = null
    ) {
        HttpGetRequestAsyncTask(callback, errorHandler)
            .execute("$BASE_URL?action=getBufferTimes")
    }

    fun getPUVSummary(
        callback: ((String) -> Unit)? = null,
        errorHandler: (() -> Unit)? = null
    ) {
        HttpGetRequestAsyncTask(callback, errorHandler)
            .execute("$BASE_URL?action=getPUVSummary")
    }

    fun getStopNodeStats(
        stopName: String,
        callback: ((String) -> Unit)? = null,
        errorHandler: (() -> Unit)? = null
    ) {
        HttpGetRequestAsyncTask(callback, errorHandler)
            .execute("$BASE_URL?action=getStopNodeStats&stopName=${stopName}")
    }
}