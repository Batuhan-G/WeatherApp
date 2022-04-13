package com.batuhangoren.weatherapp.models

import java.io.Serializable

data class Condition (
    val text: String,
    val icon: String,
    val code: Int
): Serializable
