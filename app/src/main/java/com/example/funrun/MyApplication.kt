package com.example.funrun

import android.app.Application
import android.content.SharedPreferences
import com.example.funrun.room.RunDatabase
import com.example.funrun.room.RunEntity
import com.example.runlibrary.Run

class MyApplication : Application() {

    private lateinit var sharedPreferences: SharedPreferences
    lateinit var db: RunDatabase
        private set

    companion object {
        private const val PREFS_NAME = "AppSettings"
        private const val DARK_MODE = "dark_mode"
        private const val WEEKLY_GOAL = "weekly_goal"
    }

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        db = RunDatabase.getInstance(this)
    }

    // ─── Run CRUD ─────────────────────────────────────────────────────────────

    fun addRun(run: Run) {
        Thread {
            db.runDao().insertRun(run.toEntity())
        }.start()
    }

    fun deleteRun(index: Int) {
        Thread {
            val all = db.runDao().getAllRuns()
            if (index in all.indices) {
                db.runDao().deleteRunById(all[index].id)
            }
        }.start()
    }

    fun getAllRuns(): List<Run> {
        return db.runDao().getAllRuns().map { it.toRun() }
    }

    // ─── Preferences ──────────────────────────────────────────────────────────

    fun isDarkModeEnabled(): Boolean =
        sharedPreferences.getBoolean(DARK_MODE, false)

    fun setDarkMode(enabled: Boolean) =
        sharedPreferences.edit().putBoolean(DARK_MODE, enabled).apply()

    fun getWeeklyGoal(): Float =
        sharedPreferences.getFloat(WEEKLY_GOAL, 50.0f)

    fun setWeeklyGoal(goal: Float) =
        sharedPreferences.edit().putFloat(WEEKLY_GOAL, goal).apply()
}

fun Run.toEntity() = RunEntity(
    pace = pace,
    distance = distance,
    duration = duration,
    timestamp = timestamp
)

fun RunEntity.toRun() = Run(
    pace = pace,
    distance = distance,
    duration = duration,
    timestamp = timestamp
)