package com.example.runlibrary

import kotlinx.serialization.Serializable


@Serializable
class Run(
    val pace: Double,
    val distance: Double,
    val duration: Long,
    val timestamp: Long
) {

}