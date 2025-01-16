package com.example.funrun

import android.app.Application
import android.content.SharedPreferences
import com.example.runlibrary.Run

class MyApplication:Application() {
    val runList: MutableList<Run> = mutableListOf()
    companion object {
        private const val PREFS_NAME = "AppSettings"
        private const val DARK_MODE = "dark_mode"
        private const val WEEKLY_GOAL = "weekly_goal"
    }
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreate() {
        super.onCreate()
        // Initialize anything needed globally here
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
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
    }

    // Retrieve all runs
    fun getAllRuns(): List<Run> {
        return runList
    }


}