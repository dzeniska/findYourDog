package com.dzenis_ska.findyourdog

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.dzenis_ska.findyourdog.ViewModel.BreedViewModel
import com.dzenis_ska.findyourdog.ui.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.fragment_maps.*


class MapsFragment : Fragment(), OnMapReadyCallback, LocationListener,
    GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener, GoogleMap.OnCameraMoveStartedListener {

    lateinit var viewModel: BreedViewModel
    private var permissionDenied = false
    private lateinit var locationManager: LocationManager
    private val locationPermissionCode = 200
    private lateinit var mMap: GoogleMap
    var lat: Double = 0.0
    var lng: Double = 0.0
    //для определения последней локации
    private lateinit var fusedLocationClient: FusedLocationProviderClient


//    private val callback = OnMapReadyCallback { googleMap ->
//        val sydney = LatLng(lat, lng)
//        googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(activity as MainActivity).get(BreedViewModel::class.java)
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)


//        getLocation()
        //инициализация переменной для получения последней локации
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        floatBtnGPS.setOnClickListener() {
            getLocation()
        }

    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        Log.d("!!!", "getLoc")
        // получаем последнюю локацию
        fusedLocationClient.lastLocation
                .addOnSuccessListener { location : Location? ->
                    Log.d("!!!loc", "${location?.longitude} ${location?.latitude}")
                    setMarker(location?.latitude!!, location?.longitude!!)
                    // Got last known location. In some rare situations this can be null.
                }

        locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        viewModel.locationManagerBool = true
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)



    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.d("!!!", "onMapReady")

        mMap = googleMap

        getPermission()

        mMap.setOnMyLocationButtonClickListener(this)
        mMap.setOnMyLocationClickListener(this)

        val sydney = LatLng(42.276871, 18.831676)
//        googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    private fun getPermission() {
        Log.d("!!!", "getPermission")
            if (!::mMap.isInitialized) return
        if (ActivityCompat.checkSelfPermission(
                context as MainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context as MainActivity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                activity as MainActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionCode
            )
        }else{

            getLocation()
//            mMap.isMyLocationEnabled = true

        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return
        }
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(activity as MainActivity, "Permission Granted", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(activity as MainActivity, "Permission Denied", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        Log.d("!!!", "onLocationChanged")
//        Toast.makeText(activity as MainActivity, "${location.latitude}", Toast.LENGTH_SHORT).show()
//        Log.d("!!!", "Lat: " + location.latitude + " , Lng: " + location.longitude)



        lat = location.latitude
        lng = location.longitude
        setMarker(lat, lng)

    }

    private fun setMarker(lat: Double, lng: Double) {
        var target = LatLng(lat, lng)

        mMap.clear()

        val circleOptions = CircleOptions()
            .center(LatLng(lat, lng))
            .radius(50.0)
            .fillColor(resources.getColor(R.color.fill_color))
            .strokeColor(resources.getColor(R.color.main_background))
            .strokeWidth(10f)

        mMap.addCircle(circleOptions)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(target, 17f))
        val marker = mMap.addMarker(
            MarkerOptions().position(target).title("Marker in Sydney").draggable(
                true
            )
        )
        mMap.setOnCameraMoveListener {
            marker!!.position = mMap.getCameraPosition().target //to center in map
            target = marker!!.position
//            Log.d("!!!tar", "$target")
//            locationManager.removeUpdates(this)
        }
    }


    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        val p = provider
        Log.d("!!!", "onStatusChanged")
    }

    override fun onProviderEnabled(provider: String) {
        Log.d("!!!", "provider enabled")
    }

    override fun onProviderDisabled(provider: String) {
        Log.d("!!!", "provider disabled")
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
        if(viewModel.locationManagerBool){
            locationManager.removeUpdates(this)
            viewModel.locationManagerBool = false
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onMyLocationButtonClick(): Boolean {
        Log.d("!!!", "onMyLocationButtonClick")
        return false
    }

    override fun onMyLocationClick(location: Location) {
        Toast.makeText(context as MainActivity, "Current location:\n$location", Toast.LENGTH_LONG).show()
    }

    override fun onCameraMoveStarted(reason: Int) {
        when (reason) {
            GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE -> {
                Log.d("!!!move", "REASON_GESTURE")
                Toast.makeText(activity as MainActivity, "REASON_GESTURE", Toast.LENGTH_SHORT)
                    .show()
            }
            GoogleMap.OnCameraMoveStartedListener.REASON_API_ANIMATION -> {
                Log.d("!!!move", "REASON_API_ANIMATION")
                Toast.makeText(activity as MainActivity, "REASON_API_ANIMATION", Toast.LENGTH_SHORT)
                    .show()
            }
            GoogleMap.OnCameraMoveStartedListener.REASON_DEVELOPER_ANIMATION -> {
                Log.d("!!!move", "REASON_DEVELOPER_ANIMATION")
                Toast.makeText(activity as MainActivity, "REASON_DEVELOPER_ANIMATION", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }


}