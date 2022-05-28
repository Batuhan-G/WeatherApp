package com.batuhangoren.weatherapp

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object Constants {

    const val APP_ID: String = "74137d9404524f5db3494838222702"
    const val BASE_URL: String = "http://api.weatherapi.com/v1/"
    const val METRIC_UNIT: String ="metric"

    fun isNetworkAvailable(context: Context) : Boolean{
        val connectivityManager = context.
        getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Checking version, if new phone or version, activeNetwork doesn't exist return false
        //and this network to check capabilities and if it is null return false
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }

    }
}