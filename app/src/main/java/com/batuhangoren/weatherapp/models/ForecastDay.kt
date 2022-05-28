package com.batuhangoren.weatherapp.models

import java.io.Serializable

data class ForecastDay (
    val date: String,
    val day: Day,
    val astro: Astro,
): Serializable
