package com.batuhangoren.weatherapp.models

import java.io.Serializable

data class Location (
    val name: String,
    val country: String,
    val localtime: String
): Serializable
