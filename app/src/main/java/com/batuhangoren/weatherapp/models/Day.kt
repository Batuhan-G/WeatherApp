package com.batuhangoren.weatherapp.models

import java.io.Serializable

data class Day (
    val condition: Condition,
    val avgtemp_c: Double,
    val maxtemp_c: Double,
    val mintemp_c: Double,
    val maxwind_kph: Double,
    val daily_chance_of_rain: Double
):Serializable


