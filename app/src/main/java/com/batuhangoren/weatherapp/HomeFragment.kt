package com.batuhangoren.weatherapp

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.ImageView
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.getSystemService
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import com.batuhangoren.weatherapp.databinding.FragmentHomeBinding
import com.batuhangoren.weatherapp.models.ForecastDay
import com.batuhangoren.weatherapp.models.WeatherResponse
import com.batuhangoren.weatherapp.network.WeatherService
import com.batuhangoren.weatherapp.viewModels.HomeViewModel
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HomeFragment : Fragment() {

    private lateinit var mFusedLocationClient : FusedLocationProviderClient
    private var mProgressDialog: Dialog? =null
    private var _binding: FragmentHomeBinding? =null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.executePendingBindings()
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        if(!isLocationEnabled()){
            Snackbar.make(
                requireView(),
                "Please turn on your location provider.",
                Snackbar.LENGTH_SHORT
            ).show()

            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }else{
            Dexter.withActivity(requireActivity())
                .withPermissions(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report!!.areAllPermissionsGranted()){
                           requestLocationData()
                        }

                        if (report.isAnyPermissionPermanentlyDenied){
                            Snackbar.make(
                                requireView(),
                                "You have denied location permission." +
                                        "Please enable them as it is mandatory for the app to work",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: MutableList<PermissionRequest>?,
                        token: PermissionToken?
                    ) {
                        showRationalDialogForPermissions()
                    }
                }).onSameThread().check()

        }

        return binding.root
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationData(){

        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest,mLocationCallback,
            Looper.myLooper()!!
        )
    }

    private val mLocationCallback = object : LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation
            val latitude = mLastLocation.latitude
            Log.i("Current Latitude","$latitude")

            val longitude = mLastLocation.longitude
            Log.i("Current Longitude","$latitude")
            getLocationWeatherDetails(latitude, longitude)
        }
    }


    private fun getLocationWeatherDetails(latitude: Double, longitude: Double){

        if(Constants.isNetworkAvailable(requireContext())){

           viewModel.getLocationWeatherDetails(latitude, longitude)

        }else{
            Snackbar.make(requireView(),
                "No internet connection available.",
                Snackbar.LENGTH_SHORT
            ).show()

        }
    }

    private fun showRationalDialogForPermissions(){
        AlertDialog.Builder(requireContext())
            .setMessage("It looks like you have turned off permissions" +
                    " required for this feature. It can be enable under Application Settings")
            .setPositiveButton(
                "Go to SETTINGS"
            ){_,_ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    // Which settings should be opened
                    val uri = Uri.fromParts("package", requireActivity().packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }catch (e: ActivityNotFoundException){
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel"){ dialog,_ ->
                dialog.dismiss()
            }.show()
    }

    //Access to the system location services
    private fun isLocationEnabled(): Boolean{

        val locationManager: LocationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun showCustomProgressDialog() {
        mProgressDialog = Dialog(requireContext())

        mProgressDialog!!.setContentView(R.layout.dialog_progress_bar)

        mProgressDialog!!.show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.action_refresh->{
                requestLocationData()
                true
            }else ->  super.onOptionsItemSelected(item)
        }
    }

    private fun hideProgressDialog(){

        if (mProgressDialog != null){
            mProgressDialog!!.dismiss()
        }
    }


    private fun setupUI(weatherList: WeatherResponse){
        val icons: HashMap<Int, String> = hashMapOf()
        val dates: HashMap<Int, String> = hashMapOf()
        val airTypes: HashMap<Int, String> = hashMapOf()
        val tempsOfOtherDays: HashMap<Int, Double> = hashMapOf()
        val rains: HashMap<Int, String> = hashMapOf()
        val winds:HashMap<Int, String> = hashMapOf()
        var counter = 0
        for(i in weatherList.forecast.forecastday){
            Log.i("Weather Name",weatherList.forecast.toString())
            icons[counter] = i.day.condition.icon
            dates[counter] = i.date
            tempsOfOtherDays[counter] = i.day.avgtemp_c
            airTypes[counter] = i.day.condition.text
            rains[counter] = i.day.daily_chance_of_rain.toString()+"%"
            winds[counter] = i.day.maxwind_kph.toString()+"km/h"
            dayConverter(i, dates, counter)
            counter++
        }

        printIcons(icons, binding.weather0Icon,binding.weather1Icon, binding.weather2Icon)

        binding.date0Text.text = dates[0]
        binding.date1Text.text = dates[1]
        binding.date2Text.text = dates[2]

        binding.rain0Text.text = rains[0]
        binding.rain1Text.text = rains[1]
        binding.rain2Text.text = rains[2]

        binding.airTypeText.text = airTypes[0]
        binding.airType1Text.text = airTypes[1]
        binding.airType2Text.text = airTypes[2]

        binding.tempC0Text.text= weatherList.current.temp_c.toString()+"°c"
        val temp1 = String.format("%.0f",tempsOfOtherDays[1])+"°c"
        val temp2 = String.format("%.0f",tempsOfOtherDays[2])+"°c"
        binding.tempC1Text.text = temp1
        binding.tempC2Text.text = temp2

        binding.windy1Text.text = winds[1]
        binding.windy2Text.text = winds[2]

        binding.cityText.text = weatherList.location.name

        binding.sunRiseText.text = weatherList.forecast.forecastday[0].astro.sunrise
        binding.sunSetText.text = weatherList.forecast.forecastday[0].astro.sunset
        binding.humidityText.text = weatherList.current.humidity.toString()
        binding.windyText.text = weatherList.current.wind_kph.toString()+"km/h"
        binding.maxMinTemp0Text.text = weatherList.forecast.forecastday[0].day.maxtemp_c.toString()+"°c/"+weatherList.forecast.forecastday[0].day.mintemp_c.toString()+"°c"
        binding.feelsText.text = weatherList.current.feelslike_c.toString()

        val so2 = String.format("%.1f",weatherList.current.air_quality.so2)
        val pm10 = String.format("%.1f",weatherList.current.air_quality.pm10)
        binding.so2Text.text = so2
        binding.pm10Text.text = pm10

    }

    private fun printIcons(icons: HashMap<Int, String>, weather0: ImageView, weather1: ImageView, weather2: ImageView) {
        Picasso.get().load("http:${icons[0]}").into(weather0)
        Picasso.get().load("http:${icons[1]}").into(weather1)
        Picasso.get().load("http:${icons[2]}").into(weather2)
    }

    private fun dayConverter(i: ForecastDay, hashMap: HashMap<Int, String>, counter: Int) {
        val day: DateTimeFormatter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        val local: LocalDate = LocalDate.parse(i.date, day)
        hashMap[counter] = local.dayOfWeek.toString()
    }

}