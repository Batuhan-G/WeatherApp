package com.batuhangoren.weatherapp.models

import java.io.Serializable

data class Air_quality(
    val so2: Double,
    val pm10: Double
): Serializable

