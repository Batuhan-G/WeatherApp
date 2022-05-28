package com.batuhangoren.weatherapp

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import com.google.android.gms.location.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.batuhangoren.weatherapp.databinding.ActivityMainBinding
import com.batuhangoren.weatherapp.models.ForecastDay
import com.batuhangoren.weatherapp.models.WeatherResponse
import com.batuhangoren.weatherapp.network.WeatherService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.squareup.picasso.Picasso
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {

//    private lateinit var mFusedLocationClient : FusedLocationProviderClient
//    private var mProgressDialog: Dialog? =null
      private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val toolbar:MaterialToolbar = binding.toolbar
        setSupportActionBar(toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(binding.fragmentContainerView.id) as NavHostFragment

        val navController = navHostFragment.navController
        val drawer:DrawerLayout = binding.drawerLayout
        val builder = AppBarConfiguration.Builder(navController.graph)
        builder.setOpenableLayout(drawer)

        val appBarConfiguration = builder.build()
        toolbar.setupWithNavController(navController, appBarConfiguration)

        val navView: NavigationView = binding.navigationView
        NavigationUI.setupWithNavController(navView, navController)

        binding.bottomNavigation.setupWithNavController(navController)
        binding.navigationView.setupWithNavController(navController)
    }

//        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//
//        if(!isLocationEnabled()){
//            Toast.makeText(
//                this,
//                "Please turn on your location provider.",
//                Toast.LENGTH_SHORT
//            ).show()
//
//            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
//            startActivity(intent)
//        }else{
//           Dexter.withActivity(this)
//               .withPermissions(
//                   android.Manifest.permission.ACCESS_FINE_LOCATION,
//                   android.Manifest.permission.ACCESS_COARSE_LOCATION
//               )
//               .withListener(object : MultiplePermissionsListener{
//                   override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
//                       if (report!!.areAllPermissionsGranted()){
//                           requestLocationData()
//                       }
//
//                       if (report.isAnyPermissionPermanentlyDenied){
//                           Toast.makeText(
//                               this@MainActivity,
//                               "You have denied location permission." +
//                                    "Please enable them as it is mandatory for the app to work",
//                               Toast.LENGTH_SHORT
//                           ).show()
//                       }
//                   }
//
//                   override fun onPermissionRationaleShouldBeShown(
//                       permissions: MutableList<PermissionRequest>?,
//                       token: PermissionToken?
//                   ) {
//                      showRationalDialogForPermissions()
//                   }
//               }).onSameThread().check()
//
//        }
//    }
//
//    @SuppressLint("MissingPermission")
//    private fun requestLocationData(){
//
//        val mLocationRequest = LocationRequest()
//        mLocationRequest.priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
//
//        mFusedLocationClient.requestLocationUpdates(
//            mLocationRequest,mLocationCallback,
//            Looper.myLooper()!!
//        )
//    }
//
//    private val mLocationCallback = object : LocationCallback(){
//        override fun onLocationResult(locationResult: LocationResult) {
//            val mLastLocation: Location = locationResult.lastLocation
//            val latitude = mLastLocation.latitude
//            Log.i("Current Latitude","$latitude")
//
//            val longitude = mLastLocation.longitude
//            Log.i("Current Longitude","$latitude")
//            getLocationWeatherDetails(latitude, longitude)
//        }
//    }
//
//    private fun getLocationWeatherDetails(latitude: Double, longitude: Double){
//
//        if(Constants.isNetworkAvailable(this)){
//
//            val retrofit : Retrofit = Retrofit.Builder()
//                .baseUrl(Constants.BASE_URL)
//                .addConverterFactory(GsonConverterFactory.create())
//                .build()
//
//            val service: WeatherService = retrofit
//                .create<WeatherService>(WeatherService::class.java)
//
//            val listCall: Call<WeatherResponse> = service.getWeather(
//
//                latitude, longitude, Constants.METRIC_UNIT, Constants.APP_ID
//            )
//
//            showCustomProgressDialog()
//
//            listCall.enqueue(object : Callback<WeatherResponse>{
//                override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
//
//                    if(response.isSuccessful){
//
//                        hideProgressDialog()
//
//                        val weatherList: WeatherResponse? = response.body()
//                        if (weatherList != null) {
//                            setupUI(weatherList)
//                        }
//                        Log.i("Response Result","$weatherList")
//                    }else{
//                        val rc = response.code()
//                        when(rc){
//                            400 ->{
//                                Log.e("Error 400","Bad Connection")
//                            }
//                            404 ->{
//                                Log.e("Error 404","Not Found")
//
//                            }else ->{
//                                Log.e("Error","Generic Error")
//                            }
//                        }
//
//                    }
//                }
//              override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
//
//                    Log.e("Error",t.message.toString())
//                    hideProgressDialog()
//
//              }
//            })
//
//        }else{
//            Toast.makeText(this@MainActivity,
//                "No internet connection available.",
//                Toast.LENGTH_SHORT
//            ).show()
//
//
//        }
//    }
//
//    private fun showRationalDialogForPermissions(){
//        AlertDialog.Builder(this)
//            .setMessage("It looks like you have turned off permissions" +
//                    " required for this feature. It can be enable under Application Settings")
//            .setPositiveButton(
//                "Go to SETTINGS"
//            ){_,_ ->
//                try {
//                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
//                    // Which settings should be opened
//                    val uri = Uri.fromParts("package", packageName, null)
//                    intent.data = uri
//                    startActivity(intent)
//                }catch (e: ActivityNotFoundException){
//                    e.printStackTrace()
//                }
//            }
//            .setNegativeButton("Cancel"){ dialog,_ ->
//                dialog.dismiss()
//            }.show()
//    }
//
//        //Access to the system location services
//    private fun isLocationEnabled(): Boolean{
//
//        val locationManager: LocationManager =
//            getSystemService(Context.LOCATION_SERVICE) as LocationManager
//        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
//                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
//    }
//
//    private fun showCustomProgressDialog() {
//        mProgressDialog = Dialog(this)
//
//        mProgressDialog!!.setContentView(R.layout.dialog_progress_bar)
//
//        mProgressDialog!!.show()
//    }
//
//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.menu_main, menu)
//        return super.onCreateOptionsMenu(menu)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when(item.itemId){
//            R.id.action_refresh->{
//                requestLocationData()
//                true
//            }else ->  super.onOptionsItemSelected(item)
//        }
//    }
//
//    private fun hideProgressDialog(){
//
//        if (mProgressDialog != null){
//            mProgressDialog!!.dismiss()
//        }
//    }
//
//
//    private fun setupUI(weatherList: WeatherResponse){
//        val icons: HashMap<Int, String> = hashMapOf()
//        val dates: HashMap<Int, String> = hashMapOf()
//        val airTypes: HashMap<Int, String> = hashMapOf()
//        val tempsOfOtherDays: HashMap<Int, Double> = hashMapOf()
//        val rains: HashMap<Int, String> = hashMapOf()
//        val winds:HashMap<Int, String> = hashMapOf()
//        var counter = 0
//        for(i in weatherList.forecast.forecastday){
//            Log.i("Weather Name",weatherList.forecast.toString())
//            icons[counter] = i.day.condition.icon
//            dates[counter] = i.date
//            tempsOfOtherDays[counter] = i.day.avgtemp_c
//            airTypes[counter] = i.day.condition.text
//            rains[counter] = i.day.daily_chance_of_rain.toString()+"%"
//            winds[counter] = i.day.maxwind_kph.toString()+"km/h"
//            dayConverter(i, dates, counter)
//            counter++
//        }
//
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
//        binding.airPressureText.text = weatherList.current.pressure_mb.toString()+"mb"
//        binding.maxMinTemp0Text.text = weatherList.forecast.forecastday[0].day.maxtemp_c.toString()+"°c/"+weatherList.forecast.forecastday[0].day.mintemp_c.toString()+"°c"
//        binding.feelsText.text = weatherList.current.feelslike_c.toString()
//
//        val so2 = String.format("%.1f",weatherList.current.air_quality.so2)
//        val pm10 = String.format("%.1f",weatherList.current.air_quality.pm10)
//        binding.so2Text.text = so2
//        binding.pm10Text.text = pm10
//
//    }
//
//    private fun printIcons(icons: HashMap<Int, String>, weather0: ImageView, weather1: ImageView, weather2: ImageView) {
//        Picasso.get().load("http:${icons[0]}").into(weather0)
//        Picasso.get().load("http:${icons[1]}").into(weather1)
//        Picasso.get().load("http:${icons[2]}").into(weather2)
//    }
//
//    private fun dayConverter(i: ForecastDay, hashMap: HashMap<Int, String>, counter: Int) {
//        val day:DateTimeFormatter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            DateTimeFormatter.ofPattern("yyyy-MM-dd")
//        } else {
//            TODO("VERSION.SDK_INT < O")
//        }
//        val local:LocalDate= LocalDate.parse(i.date, day)
//        hashMap[counter] = local.dayOfWeek.toString()
//    }
}

