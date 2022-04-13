package com.batuhangoren.weatherapp.models

import java.io.Serializable

data class Forecast (
    val forecastday: List<ForecastDay>
): Serializable


