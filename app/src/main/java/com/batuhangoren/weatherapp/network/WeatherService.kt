package com.batuhangoren.weatherapp.network

import com.batuhangoren.weatherapp.models.WeatherResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {

    @GET("forecast.json?key=74137d9404524f5db3494838222702&q=London&days=3&aqi=yes&alerts=no")
    fun getWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String,
        @Query("appid") appid: String?,

        ): Call<WeatherResponse>
}