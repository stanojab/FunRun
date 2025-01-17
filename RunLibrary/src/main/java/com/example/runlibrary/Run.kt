package com.example.runlibrary

import kotlinx.serialization.Serializable


@Serializable
class Run(
    val pace: Float,
    val distance: Double,
    val duration: Long,
    val timestamp: Long
) {

}