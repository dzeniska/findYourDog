package com.dzenis_ska.findyourdog.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import com.dzenis_ska.findyourdog.R
import com.dzenis_ska.findyourdog.databinding.FragmentMapsBinding
import com.dzenis_ska.findyourdog.remoteModel.firebase.AdShelter
import com.dzenis_ska.findyourdog.remoteModel.firebase.FBAuth
import com.dzenis_ska.findyourdog.ui.MainActivity
import com.dzenis_ska.findyourdog.ui.fragments.adapters.MapPhotoAdapter
import com.dzenis_ska.findyourdog.ui.utils.CheckNetwork
import com.dzenis_ska.findyourdog.viewModel.BreedViewModel
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.*


class MapsFragment : Fragment(), OnMapReadyCallback, LocationListener,
    GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener, GoogleMap.OnInfoWindowClickListener,
    GoogleMap.OnMarkerClickListener {

    val viewModel: BreedViewModel by activityViewModels()
    private var rootElement: FragmentMapsBinding? = null
    lateinit var navController: NavController
    private var permissionDenied = false
    private lateinit var locationManager: LocationManager
    private val locationPermissionCode = 200
    private lateinit var mMap: GoogleMap
    var adapter: MapPhotoAdapter? = null
    var marker: Marker? = null
    var addCircle: Circle? = null
    var lat: Double = 0.0
    var lng: Double = 0.0
    var lastLat: Double = 0.0
    var lastLng: Double = 0.0
    var mapFragment: SupportMapFragment? = null
    var dialogF: AlertDialog? = null
    val listAdShelter = arrayListOf<AdShelter>()
    val listAdShelterForAllSh = arrayListOf<AdShelter>()
    val listAdShelterMainPhoto = mutableListOf<String>()
    var job2: Job? = null
    var job3: Job? = null
    val cs = ConstraintSet()

    var isEditing: Boolean = false
//    private val check: CheckNetwork = CheckNetwork()

    //для определения последней локации
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        Log.d("!!!on", "onCreateViewMF")
        rootElement = FragmentMapsBinding.inflate(inflater)
        return rootElement!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("!!!on", "onViewCreatedMF")
        navController = findNavController()

        mapFragment = childFragmentManager.findFragmentById(R.id.mapFr) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        //инициализация переменной для получения последней локации
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context as Context)

        viewModel.dialog.observe(viewLifecycleOwner, {dialog->
            dialogF = dialog
        })


        viewModel.liveAdsDataAllShelter.observe(viewLifecycleOwner,{list ->
            val list1 = list
            if(list.size == 0) CheckNetwork.check(activity as MainActivity)
            if (::mMap.isInitialized) {
                getAllMarkers(list1, null)
                listAdShelter.clear()
                listAdShelter.addAll(list1)
            }
        })

        init()
        initClick()

    }
//    private fun updatePermissionsState() {
//        val permissionsStates: Map<String, Boolean> = permissions.associateWith { permission ->
//            ActivityCompat.checkSelfPermission(context!!, permission) == PackageManager.PERMISSION_GRANTED
//        }
//    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.sample_menu, menu)
        val item = menu.findItem(R.id.action_done)
        if(!isEmailVeryfied()) item.icon = resources.getDrawable(R.drawable.ic_replay)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d("!!!onOptionsItemSelected", "${isEditing}")
        return when (item.itemId) {
            R.id.action_settings -> {
                // navigate to settings screen
                true
            }
            R.id.action_done -> {
                if(!isEmailVeryfied()){
                    showAllAds()
                }else{
                    if(!isEditing){
                        showMyAds()
                        item.icon = resources.getDrawable(R.drawable.ic_all)
                    }else{
                        showAllAds()
                        item.icon = resources.getDrawable(R.drawable.ic_my)
                    }
                    isEditing = !isEditing
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun showAllAds(){
        listAdShelter.clear()
        listAdShelter.addAll(listAdShelterForAllSh)
        viewModel.getAllAds()
    }

    private fun showMyAds() {
        val fbAuth = FBAuth(this)
        val listAll = arrayListOf<AdShelter>()
        listAdShelterForAllSh.clear()
        listAdShelterForAllSh.addAll(listAdShelter)
        listAdShelter.forEach {
            if(fbAuth.mAuth.uid == it.uid) listAll.add(it)
        }
        if(listAll.size != 0) {
            listAdShelter.clear()
            listAdShelter.addAll(listAll)
            getAllMarkers(listAll, null)
        }
    }

    private fun initClick() = with(rootElement!!){
        floatBtnGPS.setOnClickListener() {
            getLocation()
            floatBtnGPS.visibility = View.GONE
        }
        floatBtnAddShelter.setOnClickListener() {
            viewModel.btnDelState = true
            viewModel.openFragShelter(null)
            navController.navigate(R.id.addShelterFragment)
        }
    }

    private fun init(){
        if(viewModel.dbManager.auth.currentUser?.isAnonymous == false && viewModel.dbManager.auth.currentUser != null ){
            if(isEmailVeryfied()) {

                rootElement?.floatBtnAddShelter?.visibility = View.VISIBLE
            }
        }

        adapter = MapPhotoAdapter(this)
        rootElement!!.rcViewMapPhoto.adapter = adapter
        rootElement!!.rcViewMapPhoto.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(rootElement!!.rcViewMapPhoto)

    }
    private fun isEmailVeryfied() = viewModel.dbManager.auth.currentUser!!.isEmailVerified

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        Log.d("!!!", "getLoc")
        dialogF?.dismiss()
        // получаем последнюю локацию
        fusedLocationClient.lastLocation
                .addOnSuccessListener { location : Location? ->
                    if(location != null) {
                        Log.d("!!!loc", "${location.longitude} ${location.latitude}")
//                        Toast.makeText(context, "Find your last location!", Toast.LENGTH_LONG).show()
                        setMarker(location.latitude, location.longitude, 6f)
                        lastLat = location.latitude
                        lastLng = location.longitude
                        // Got last known location. In some rare situations this can be null.
                    }else{
                        Toast.makeText(context, "No last location!", Toast.LENGTH_LONG).show()
                    }
                }

        locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        viewModel.locationManagerBool = true
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.d("!!!on", "onMapReady")
        mMap = googleMap
        mMap.setOnMyLocationButtonClickListener(this)
        mMap.setOnMyLocationClickListener(this)
        mMap.setInfoWindowAdapter(CustomInfoWindowAdapter())
        mMap.setOnInfoWindowClickListener(this)
        mMap.setOnMarkerClickListener(this)

        getPermission()
    }




    /** Demonstrates customizing the info window and/or its contents.  */
    internal inner class CustomInfoWindowAdapter : GoogleMap.InfoWindowAdapter {

        // These are both view groups containing an ImageView with id "badge" and two
        // TextViews with id "title" and "snippet".
        private val window: View = layoutInflater.inflate(R.layout.custom_info_window, null)
        private val contents: View = layoutInflater.inflate(R.layout.custom_info_contents, null)

        override fun getInfoWindow(marker: Marker): View? {

            render(marker, window)
            return window
        }

        override fun getInfoContents(marker: Marker): View? {
            render(marker, contents)
            return contents
        }

        private fun render(marker: Marker, view: View) {
//            val badge = R.drawable.add_photo
            val badge = R.drawable.ic_waling_man_dog
//            var uri = ""
//            listAdShelter.forEach {
//                if (it.key == marker.tag) {
//                    uri = it.photoes?.get(0) ?: ""
//                }
//            }

            view.findViewById<ImageView>(R.id.badge).setImageResource(badge)

            // Set the title and snippet for the custom info window
            val title: String? = marker.title
            val titleUi = view.findViewById<TextView>(R.id.title)

            if (title != null) {
                // Spannable string allows us to edit the formatting of the text.
                titleUi.text = SpannableString(title).apply {
                    setSpan(ForegroundColorSpan(Color.RED), 0, length, 0)
                }
            } else {
                titleUi.text = ""
            }

            val snippet: String? = marker.snippet
            val snippetUi = view.findViewById<TextView>(R.id.snippet)
            if (snippet != null && snippet.length > 0) {
                snippetUi.text = SpannableString(snippet).apply {
                    setSpan(ForegroundColorSpan(Color.MAGENTA), 0, 1, 0)
//                    setSpan(ForegroundColorSpan(Color.BLUE), 1, snippet.length, 0)
                }
            } else {
                snippetUi.text = ""
            }
        }
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
//            getAllMarkers()
            viewModel.getAllAds()
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
        lat = location.latitude
        lng = location.longitude

        setMarker(lat, lng, 10f)
        if(lat != lastLat && lng != lastLng) {
            locationManager.removeUpdates(this)
            viewModel.locationManagerBool = false
            rootElement!!.floatBtnGPS.visibility = View.VISIBLE
        }
    }

    private fun getAllMarkers(list: List<AdShelter>, chose: String?){
        mMap.clear()
        val listMainPhoto = arrayListOf<String>()
        for(item in list){
            val target = LatLng(item.lat!!.toDouble(), item.lng!!.toDouble())
            val marker = mMap.addMarker(
                MarkerOptions().position(target)
                    .icon(BitmapDescriptorFactory.defaultMarker(item.markerColor?:15f))
                    .title("${item.gender}")
                    .snippet(item.name)
                    .draggable(false)

//                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.paw_mark))
            )

            marker!!.tag = item.key
            if(marker.tag == chose) marker.showInfoWindow()
            listMainPhoto.add(item.photoes?.get(0) ?: "empty")
            if(item.uid == viewModel.dbManager.auth.uid) {
//                marker.showInfoWindow()
                marker.alpha = 0.6f
            }
        }

        adapter?.updateAdapter(listMainPhoto)
        setHasOptionsMenu(true)

    }
    override fun onInfoWindowClick(markerInfo: Marker) {
        isEditing = false
        for(info in viewModel.listShelter){
            if(info.key == markerInfo.tag){
                if(viewModel.btnDelState == true) viewModel.btnDelState = false
                viewModel.openFragShelter(info)
                navController.navigate(R.id.addShelterFragment)
                Toast.makeText(context as MainActivity, "${info.key}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun animateCamera(pos: Int){
        if(job2 == null) {
            job2 = CoroutineScope(Dispatchers.Main).launch {
                getAllMarkers(listAdShelter, listAdShelter[pos].key)
                val target =
                    LatLng(listAdShelter[pos].lat!!.toDouble(), listAdShelter[pos].lng!!.toDouble())
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(target, 10f))
                delay(1500)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(target, 16f))
                job2 = null
            }
        }
    }

    private fun setMarker(lat: Double, lng: Double, zoom: Float) {
        val target = LatLng(lat, lng)
        marker?.remove()
        addCircle?.remove()
        val circleOptions = CircleOptions()
            .center(LatLng(lat, lng))
            .radius(350.0)
            .fillColor(resources.getColor(R.color.fill_color))
            .strokeColor(resources.getColor(R.color.main_background))
            .strokeWidth(10f)

        addCircle = mMap.addCircle(circleOptions)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(target, zoom))
//        marker = mMap.addMarker(
//            MarkerOptions().position(target).title("Marker in Sydney").draggable(
//                false
//            )
//        )
        mMap.setOnCameraMoveListener {
//            marker?.position = mMap.getCameraPosition().target //to center in map
//            target = marker!!.position
            //здесь сохраняем данные местоположения
//            Log.d("!!!target", "target $target")
        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        Log.d("!!!", "onStatusChanged $provider")
    }

    override fun onProviderEnabled(provider: String) {
        Log.d("!!!", "provider enabled")
    }

    override fun onProviderDisabled(provider: String) {
        Log.d("!!!", "provider disabled")
    }

    override fun onStart() {
        Log.d("!!!on", "onStartMF")
        super.onStart()
        mapFragment?.onStart()
    }

    override fun onStop() {
        job3?.cancel()
        mapFragment?.onStop()
        super.onStop()
        Log.d("!!!on", "onStopMF")
        if(viewModel.locationManagerBool){
            locationManager.removeUpdates(this)
            viewModel.locationManagerBool = false
        }
    }

    override fun onMyLocationButtonClick(): Boolean {
        Log.d("!!!", "onMyLocationButtonClick")
        return false
    }

    override fun onMyLocationClick(location: Location) {
        Toast.makeText(context as MainActivity, "Current location:\n$location", Toast.LENGTH_LONG).show()
    }

    override fun onResume() {
        Log.d("!!!on", "onResumeMF")
        super.onResume()
        mapFragment?.onResume()

    }

    override fun onDestroy() {
        Log.d("!!!", "onDestroyMF")
        mapFragment?.onDestroy()
        super.onDestroy()
    }

    override fun onDestroyView() {
        Log.d("!!!on", "onDestroyView")
        rootElement = null
        super.onDestroyView()
    }

    override fun onPause() {
        Log.d("!!!on", "onPauseMF")
        mapFragment?.onPause()
        super.onPause()
    }

    override fun onLowMemory() {
        Log.d("!!!on", "onDestroyViewMF")
        super.onLowMemory()
        mapFragment?.onLowMemory()
    }

    override fun onMarkerClick(m: Marker): Boolean {
        Log.d("!!!marker", "${m.tag}")
        clickMarker(m)
        return false
    }

    private fun clickMarker(m: Marker) {
        listAdShelter.forEach {
            if (it.key == m.tag) {
                val index = listAdShelter.indexOf(it)
                if(job3 == null) {
                    job3 = CoroutineScope(Dispatchers.Main).launch {
                        var height = rootElement!!.rcViewMapPhoto.height
                        constraint(height.minus(10))
                        delay(10)
                        scrollToPos(index)
                        delay(2000)
                        constraint(ConstraintSet.MATCH_CONSTRAINT)
                        job3 = null
                    }
                }
            }
        }
    }

    private fun scrollToPos(index: Int) {
        rootElement!!.rcViewMapPhoto.smoothScrollToPosition(index)
    }

    private fun constraint(i: Int) {
            cs.clone(rootElement!!.clFM)
            cs.constrainWidth(R.id.rcViewMapPhoto, i)
            Log.d("!!!markInd", "${i}")
            cs.applyTo(rootElement!!.clFM)
    }
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}