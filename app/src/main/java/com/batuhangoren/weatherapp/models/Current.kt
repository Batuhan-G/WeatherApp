package com.batuhangoren.weatherapp.models

import java.io.Serializable
import java.sql.Timestamp

data class Current (
    val condition: Condition,
    val air_quality: Air_quality,
    val last_updated: String,       //it can be use Location/localtime
    val temp_c: Int,
    val wind_kph: Double,
    val pressure_mb: Double,
    val humidity: Int,
    val feelslike_c: Double,

):Serializable