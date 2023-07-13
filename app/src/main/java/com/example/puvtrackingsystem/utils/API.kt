package com.example.puvtrackingsystem.utils

import HttpGetRequestAsyncTask

val BASE_URL = "https://script.google.com/macros/s/AKfycbziAMj5DxTcVB0FJyDjePXAawScbMYNM9UBXe5qA4ugWcWRtjrrToA375ljOxirgp7_/exec"

object API {
    fun getAllPUVData(
        callback: ((String) -> Unit)? = null,
        errorHandler: (() -> Unit)? = null
    ) {
        HttpGetRequestAsyncTask(callback, errorHandler)
            .execute("${BASE_URL}?action=getAllPUVData")
    }

    fun getBufferTimes(
        callback: (String) -> Unit,
        errorHandler: (() -> Unit)? = null
    ) {
        HttpGetRequestAsyncTask(callback, errorHandler)
            .execute("${BASE_URL}?action=getBufferTimes")
    }

    fun getPUVSummary(
        callback: ((String) -> Unit)? = null,
        errorHandler: (() -> Unit)? = null
    ) {
        HttpGetRequestAsyncTask(callback, errorHandler)
            .execute("${BASE_URL}?action=getPUVSummary")
    }

    fun getStopNodeStats(
        stopName: String,
        callback: (String) -> Unit,
        errorHandler: (() -> Unit)? = null
    ) {
        HttpGetRequestAsyncTask(callback, errorHandler)
            .execute("${BASE_URL}?action=getStopNodeStats&stopName=${stopName}")
    }
}