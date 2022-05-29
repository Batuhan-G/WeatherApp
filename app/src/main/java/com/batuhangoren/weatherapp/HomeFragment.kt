package com.batuhangoren.weatherapp

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.batuhangoren.weatherapp.databinding.FragmentHomeBinding
import com.batuhangoren.weatherapp.viewModels.HomeViewModel
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.squareup.picasso.Picasso

class HomeFragment : Fragment() {

    private lateinit var mFusedLocationClient : FusedLocationProviderClient
    private var mProgressDialog: Dialog? =null
    private var _binding: FragmentHomeBinding? =null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by activityViewModels()
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel

        binding.lifecycleOwner = viewLifecycleOwner
        binding.executePendingBindings()

        sharedPreferences = requireActivity()
            .getSharedPreferences("currentCity", Context.MODE_PRIVATE)

        val currentCity = sharedPreferences.getString("city", "")

        if (!currentCity.isNullOrEmpty()) {
            viewModel.setCurrentCity(currentCity)
            observe()
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

            if(!isLocationEnabled()){
                Snackbar.make(
                    requireView(),
                    getString(R.string.location_provider),
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
                                    getString(R.string.lcoation_permission),
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
        } else {
            val action = HomeFragmentDirections.actionHomeFragmentToSelectCityFragment()
            findNavController().navigate(action)
        }

        return binding.root
    }

    private fun observe(){
        viewModel.icons.observe(viewLifecycleOwner){
            it?.let {
                if (it.isNotEmpty()){
                    Picasso.get().load(it[0]).into(binding.weather0Icon)
                    Picasso.get().load(it[1]).into(binding.weather1Icon)
                    Picasso.get().load(it[2]).into(binding.weather2Icon)
                }
            }
        }
        viewModel.hideProgressDialog.observe(viewLifecycleOwner) {
            it?.let {
                if (it) {
                    hideProgressDialog()
                }
            }
        }
        viewModel.showCustomProgressDialog.observe(viewLifecycleOwner) {
            it?.let {
                if (it) {
                    showCustomProgressDialog()
                }
            }
        }
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
                getString(R.string.no_internet_connection),
                Snackbar.LENGTH_SHORT
            ).show()

        }
    }

    private fun showRationalDialogForPermissions(){
        AlertDialog.Builder(requireContext())
            .setMessage(getString(R.string.permission_alert))
            .setPositiveButton(
                getString(R.string.goToSettings)
            ){_,_ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)

                    val uri = Uri.fromParts("package", requireActivity().packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }catch (e: ActivityNotFoundException){
                    e.printStackTrace()
                }
            }
            .setNegativeButton(getString(R.string.cancel)){ dialog, _ ->
                dialog.dismiss()
            }.show()
    }

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

    private fun hideProgressDialog(){

        if (mProgressDialog != null){
            mProgressDialog!!.dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.setShowCustomDialog(null)
    }
}