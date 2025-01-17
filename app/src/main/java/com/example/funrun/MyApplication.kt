package com.example.funrun

import android.app.Application
import android.content.SharedPreferences
import com.example.runlibrary.Run
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

class MyApplication:Application() {
    val runList: MutableList<Run> = mutableListOf()
    companion object {
        private const val PREFS_NAME = "AppSettings"
        private const val DARK_MODE = "dark_mode"
        private const val WEEKLY_GOAL = "weekly_goal"
        private const val DATA_FILE_NAME = "runs_data.json"
    }
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreate() {
        super.onCreate()
        // Initialize anything needed globally here
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        loadDataFromFile()
    }

    private fun saveDataToFile() {
        try {
            val json = Json.encodeToString(runList)
            val file = File(filesDir, DATA_FILE_NAME)
            file.writeText(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Deserialize and Load Data
    private fun loadDataFromFile() {
        try {
            val file = File(filesDir, DATA_FILE_NAME)
            if (file.exists()) {
                val json = file.readText()
                val data = Json.decodeFromString<List<Run>>(json)
                runList.clear()
                runList.addAll(data)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isDarkModeEnabled(): Boolean {
        return sharedPreferences.getBoolean(DARK_MODE, false)
    }

    fun setDarkMode(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(DARK_MODE, enabled).apply()
    }

    fun getWeeklyGoal(): Float {
        return sharedPreferences.getFloat(WEEKLY_GOAL, 50.0f)
    }

    fun setWeeklyGoal(goal: Float) {
        sharedPreferences.edit().putFloat(WEEKLY_GOAL, goal).apply()
    }
    // Add a run to the list
    fun addRun(run: Run) {
        runList.add(run)
        saveDataToFile()
    }
    fun updateRun(index: Int, updatedRun: Run) {
        if (index in runList.indices) {
            runList[index] = updatedRun
            saveDataToFile()
        }
    }
    fun deleteRun(index: Int) {
        if (index in runList.indices) {
            runList.removeAt(index)
            saveDataToFile()
        }
    }
    // Retrieve all runs
    fun getAllRuns(): List<Run> {
        return runList
    }


}