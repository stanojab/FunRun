package com.example.funrun.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RunDao {

    @Insert
    fun insertRun(run: RunEntity): Long

    @Query("SELECT * FROM runs ORDER BY timestamp DESC")
    fun getAllRuns(): List<RunEntity>

    @Query("DELETE FROM runs WHERE id = :id")
    fun deleteRunById(id: Int)

    @Query("DELETE FROM runs")
    fun deleteAllRuns()
}