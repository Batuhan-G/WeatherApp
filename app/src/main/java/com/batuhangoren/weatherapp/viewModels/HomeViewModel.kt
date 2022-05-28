package com.batuhangoren.weatherapp.viewModels

import android.os.Build
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.batuhangoren.weatherapp.Constants
import com.batuhangoren.weatherapp.models.ForecastDay
import com.batuhangoren.weatherapp.models.WeatherResponse
import com.batuhangoren.weatherapp.network.WeatherService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HomeViewModel: ViewModel() {

    private val _icons = MutableLiveData<MutableList<String>>()
    val icons :LiveData<MutableList<String>> get() = _icons

    private val _dates = MutableLiveData<MutableList<String>>()
    val dates :LiveData<MutableList<String>> get() = _dates

    private val _airTypes = MutableLiveData<MutableList<String>>()
    val airTypes :LiveData<MutableList<String>> get() = _airTypes

    private val _tempsOfOtherDays = MutableLiveData<MutableList<Double>>()
    val tempsOfOtherDays :LiveData<MutableList<Double>> get() = _tempsOfOtherDays

    private val _rains = MutableLiveData<MutableList<String>>()
    val rains :LiveData<MutableList<String>> get() = _rains

    private val _winds = MutableLiveData<MutableList<String>>()
    val winds :LiveData<MutableList<String>> get() = _winds

    private val _weatherList = MutableLiveData<WeatherResponse>()
    val weatherList :LiveData<WeatherResponse> get() = _weatherList

    private val _hideProgressDialog = MutableLiveData<Boolean>()
    val hideProgressDialog :LiveData<Boolean> get() = _hideProgressDialog

    private val _showCustomProgressDialog = MutableLiveData<Boolean>()
    val showCustomProgressDialog :LiveData<Boolean> get() = _showCustomProgressDialog

    private val _showSnackbar = MutableLiveData<Boolean>()
    val showSnackbar :LiveData<Boolean> get() = _showSnackbar

    private fun setupUI(weatherList: WeatherResponse){

        var counter = 0
        val datesList = mutableListOf<String>()
        val airTypes = mutableListOf<String>()
        val tempsOfOtherDays = mutableListOf<Double>()
        val icons = mutableListOf<String>()
        val rains = mutableListOf<String>()
        val windys = mutableListOf<String>()

            for(i in weatherList.forecast.forecastday){

            Log.i("Weather Name",weatherList.forecast.toString())
                icons.add(counter, i.day.condition.icon)
            //_icons.value?.put(counter, i.day.condition.icon)
            datesList.add(counter, i.date)
            rains.add(counter, i.day.daily_chance_of_rain.toString()+"%")
            tempsOfOtherDays.add(counter, i.day.avgtemp_c)
            airTypes.add(counter,i.day.condition.text)
            windys.add(counter, i.day.maxwind_kph.toString()+"km/h")
            dayConverter(i, datesList, counter)
            counter++
        }
        _dates.value = datesList
        _airTypes.value = airTypes
        _tempsOfOtherDays.value = tempsOfOtherDays
        _icons.value = icons
        _rains.value = rains
        _winds.value = windys

//        printIcons(icons, binding.weather0Icon,binding.weather1Icon, binding.weather2Icon)
//
//        binding.date0Text.text = dates[0]
//        binding.date1Text.text = dates[1]
//        binding.date2Text.text = dates[2]
//
//        binding.rain0Text.text = rains[0]
//        binding.rain1Text.text = rains[1]
//        binding.rain2Text.text = rains[2]
//
//        binding.airTypeText.text = airTypes[0]
//        binding.airType1Text.text = airTypes[1]
//        binding.airType2Text.text = airTypes[2]
//
//        binding.tempC0Text.text= weatherList.current.temp_c.toString()+"°c"
//        val temp1 = String.format("%.0f",tempsOfOtherDays[1])+"°c"
//        val temp2 = String.format("%.0f",tempsOfOtherDays[2])+"°c"
//        binding.tempC1Text.text = temp1
//        binding.tempC2Text.text = temp2
//
//        binding.windy1Text.text = winds[1]
//        binding.windy2Text.text = winds[2]
//
//        binding.cityText.text = weatherList.location.name
//
//        binding.sunRiseText.text = weatherList.forecast.forecastday[0].astro.sunrise
//        binding.sunSetText.text = weatherList.forecast.forecastday[0].astro.sunset
//        binding.humidityText.text = weatherList.current.humidity.toString()
//        binding.windyText.text = weatherList.current.wind_kph.toString()+"km/h"
//        binding.maxMinTemp0Text.text = weatherList.forecast.forecastday[0].day.maxtemp_c.toString()+"°c/"+weatherList.forecast.forecastday[0].day.mintemp_c.toString()+"°c"
//        binding.feelsText.text = weatherList.current.feelslike_c.toString()
//
//        val so2 = String.format("%.1f",weatherList.current.air_quality.so2)
//        val pm10 = String.format("%.1f",weatherList.current.air_quality.pm10)
//        binding.so2Text.text = so2
//        binding.pm10Text.text = pm10

    }

    private fun dayConverter(i: ForecastDay, list: MutableList<String>, counter: Int) {
        val day: DateTimeFormatter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        val local: LocalDate = LocalDate.parse(i.date, day)
        list[counter] = local.dayOfWeek.toString()
    }

     fun getLocationWeatherDetails(latitude: Double, longitude: Double){

        val retrofit : Retrofit = Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service: WeatherService = retrofit
            .create(WeatherService::class.java)

        val listCall: Call<WeatherResponse> = service.getWeather(

            latitude, longitude, Constants.METRIC_UNIT, Constants.APP_ID
        )

        // showCustomProgressDialog()
        _showCustomProgressDialog.postValue(true)

        listCall.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {

                if(response.isSuccessful){

                    //   hideProgressDialog()
                    _hideProgressDialog.postValue(true)

                    _weatherList.value = response.body()

                    _weatherList.value?.let { setupUI(it) }

                    Log.i("Response Result","${_weatherList.value}")
                }else{
                    val rc = response.code()
                    when(rc){
                        400 ->{
                            Log.e("Error 400","Bad Connection")
                        }
                        404 ->{
                            Log.e("Error 404","Not Found")

                        }else ->{
                        Log.e("Error","Generic Error")
                    }
                    }
                }
            }
            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {

                Log.e("Error",t.message.toString())
                // hideProgressDialog()
                _hideProgressDialog.postValue(true)
            }
        })
    }
}