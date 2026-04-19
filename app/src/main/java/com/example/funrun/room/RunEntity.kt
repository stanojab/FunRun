package com.example.funrun.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "runs")
data class RunEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val pace: Double,
    val distance: Double,
    val duration: Long,
    val timestamp: Long
)