package com.batuhangoren.weatherapp.models

import java.io.Serializable

data class WeatherResponse (
    val location: Location,
    val current : Current,
    val forecast: Forecast
): Serializable