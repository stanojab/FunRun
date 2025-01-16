package com.example.funrun

import android.app.Application
import com.example.runlibrary.Run

class MyApplication:Application() {
    val runList: MutableList<Run> = mutableListOf()
    override fun onCreate() {
        super.onCreate()
        // Initialize anything needed globally here
    }

    // Add a run to the list
    fun addRun(run: Run) {
        runList.add(run)
    }

    // Retrieve all runs
    fun getAllRuns(): List<Run> {
        return runList
    }
}