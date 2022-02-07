package com.dzenis_ska.findyourdog.ui.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
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
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import com.dzenis_ska.findyourdog.R
import com.dzenis_ska.findyourdog.databinding.FragmentMapsBinding
import com.dzenis_ska.findyourdog.remoteModel.firebase.AdForMap
import com.dzenis_ska.findyourdog.remoteModel.firebase.AdShelter
import com.dzenis_ska.findyourdog.ui.MainActivity
import com.dzenis_ska.findyourdog.ui.fragments.adapters.MapPhotoAdapter
import com.dzenis_ska.findyourdog.ui.utils.CheckNetwork
import com.dzenis_ska.findyourdog.viewModel.BreedViewModel
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList


class MapsFragment : Fragment(), OnMapReadyCallback, LocationListener,
    GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener,
    GoogleMap.OnInfoWindowClickListener,
    GoogleMap.OnMarkerClickListener
{

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
    var keyMarker = ""
    var mapFragment: SupportMapFragment? = null
    var dialogF: AlertDialog? = null
    val listMarkers = arrayListOf<AdForMap>()
    val listAdShelter = arrayListOf<AdShelter>()
    val listAdShelterForAllSh = arrayListOf<AdShelter>()
    val listAdShelterMainPhoto = mutableListOf<String>()
    var job2: Job? = null
    var job3: Job? = null
    val cs = ConstraintSet()

    var badgeImage: ImageView? = null


    var currentItem = R.id.action_done




    //для определения последней локации
    private var fusedLocationClient: FusedLocationProviderClient? = null

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
//
//        InitBackStack.initBackStack(navController)
//        (activity as AppCompatActivity).setSupportActionBar(rootElement!!.toolbar)

        (activity as AppCompatActivity)
            .supportActionBar?.title =
            if (!viewModel.isMyMarkers)
                resources.getString(R.string.map)
            else
                resources.getString(R.string.my_ads)

        mapFragment = childFragmentManager.findFragmentById(R.id.mapFr) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        //инициализация переменной для получения последней локации
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context as Context)
        viewModel.dialog.observe(viewLifecycleOwner, {dialog->
            dialogF = dialog
        })

        viewModel.liveAdsDataForMapAdapter.observe(viewLifecycleOwner,{listAS ->
            Log.d("!!!on", "viewModel.liveAdsDataForMapAdapter - ${listAS.size}")
            updateAdapter(listAS)
            rootElement!!.progressBarMap.visibility = View.GONE
            if(keyMarker.isNotEmpty()) {
                listAS.forEach listAS@{ adSh->
                    if (adSh.key == keyMarker) {
                        val index = listAS.indexOfFirst{it.key == adSh.key}
                        if(job3 == null) {
                            job3 = CoroutineScope(Dispatchers.Main).launch {
                                var height = rootElement!!.rcViewMapPhoto.height
                                constraint(height.minus(10))
                                delay(10)
                                scrollToPos(index)
                                delay(5000)
                                constraint(ConstraintSet.MATCH_CONSTRAINT)
                                scrollToPos(index)
                                rootElement!!.progressBarMap.visibility = View.GONE
                                job3 = null
                            }
                        }
                        return@listAS
                    }
                }
            }
        })

        viewModel.liveAdsDataAllMarkers.observe(viewLifecycleOwner,{ list ->
            Log.d("!!!on", "viewModel.liveAdsDataForMapFr _ ${list.size}")
            if(list.size == 0) CheckNetwork.check(activity as MainActivity)
            if (::mMap.isInitialized) {
                if(listMarkers.isEmpty()){
                    listMarkers.clear()
                    listMarkers.addAll(list)
                }
                showAllMarkers(list, null)
            }
        })
        init()
        initClick()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.sample_menu, menu)
        val item = menu.findItem(R.id.action_done)
//        if(!isEmailVeryfied()) item.icon = resources.getDrawable(R.drawable.ic_replay, context?.theme)
//        if(viewModel.isMyMarkers) item.icon = resources.getDrawable(R.drawable.ic_all, context?.theme)
        if(!isEmailVeryfied()) {
            menu.clear()
//            item.title = resources.getString(R.string.update_ads)
            return@onCreateOptionsMenu
        }
        if(viewModel.isMyMarkers) item.title = resources.getString(R.string.all_ads)
        else item.title = resources.getString(R.string.my_ads)
    }
//
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_done -> {
                if(!isEmailVeryfied()){
                    viewModel.getAllMarkersForMap(){
//                        item.title = resources.getString(R.string.update_ads)
                    }
                }else{
                    viewModel.isMyMarkers = !viewModel.isMyMarkers
                    val allAds = resources.getString(R.string.all_ads)
                    val myAds = resources.getString(R.string.my_ads)
                    if(viewModel.isMyMarkers){
                        showMyMarkers()
                        item.title = allAds
                        (activity as AppCompatActivity).supportActionBar?.title = myAds

//                        item.icon = resources.getDrawable(R.drawable.ic_all, context?.theme)
                    }else{
                        viewModel.getAllMarkersForMap(){
                            if(it){
//                                item.icon = resources.getDrawable(R.drawable.ic_my, context?.theme)
                                item.title = myAds
                                (activity as AppCompatActivity).supportActionBar?.title = allAds
                                viewModel.getAllAdsForAdapter(lastLat, lastLng, viewModel.isMyMarkers)
                                val target = LatLng(lastLat, lastLng)
                                animateCamera(target, 10f)
                            }
                        }
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initClick() = with(rootElement!!){
        floatBtnGPS.setOnClickListener() {
            //todo ???
            floatBtnGPS.visibility = View.GONE
            getLocation()
        }

        floatBtnAddShelter.setOnClickListener() {
            //todo no uses? mapFragToAddShelterFragId
            viewModel.mapFragToAddShelterFragId = AddShelterFragment.ADD_DOG
            viewModel.openFragShelter(null)
            navController.navigate(R.id.addShelterFragment)
        }
    }

    private fun init(){
        if(viewModel.dbManager.mAuth.currentUser?.isAnonymous == false && viewModel.dbManager.mAuth.currentUser != null ){
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
    private fun isEmailVeryfied() = viewModel.dbManager.mAuth.currentUser!!.isEmailVerified

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        Log.d("!!!", "getLoc")
        dialogF?.dismiss()
        // получаем последнюю локацию
        fusedLocationClient?.lastLocation
                ?.addOnSuccessListener { location : Location? ->
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
//        viewModel.locationManagerBoolMF = true
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.d("!!!on", "onMapReadyMF")
        mMap = googleMap
        mMap.setOnMyLocationButtonClickListener(this)
        mMap.setOnMyLocationClickListener(this)
        mMap.setInfoWindowAdapter(CustomInfoWindowAdapter())
        mMap.setOnInfoWindowClickListener(this)
        mMap.setOnMarkerClickListener(this)
//        mMap.setMinZoomPreference(6.0f)
        mMap.setOnCameraIdleListener {}
        mMap.setOnCameraMoveListener {}
        viewModel.getAllMarkersForMap(){}
        getLocation()
    }

    /** Demonstrates customizing the info window and/or its contents.  */
    internal inner class CustomInfoWindowAdapter : GoogleMap.InfoWindowAdapter {
        // These are both view groups containing an ImageView with id "badge" and two
        // TextViews with id "title" and "snippet".
        private val window: View = layoutInflater.inflate(R.layout.custom_info_window, null)
        private val contents: View = layoutInflater.inflate(R.layout.custom_info_contents, null)

        override fun getInfoWindow(marker: Marker): View? {
            Log.d("!!!getInfoWindow", "marker ${marker.tag}")
            render(marker, window)
            return window
        }

        override fun getInfoContents(marker: Marker): View? {
            Log.d("!!!getInfoContents", "marker ${marker.tag}")
            render(marker, contents)
            return contents
        }

        private fun render(marker: Marker, view: View) {
            val badge = R.drawable.ic_waling_man_dog
            badgeImage = view.findViewById<ImageView>(R.id.badge)
            badgeImage?.setImageResource(badge)
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

    override fun onLocationChanged(location: Location) {
        Log.d("!!!", "onLocationChanged")
        lat = location.latitude
        lng = location.longitude
        setMarker(lat, lng, 10f)
        if(lat != lastLat && lng != lastLng) {
            locationManager.removeUpdates(this)
            rootElement!!.floatBtnGPS.visibility = View.VISIBLE
        }
    }

    private fun showMyMarkers() {
        if(listMarkers.isNotEmpty()){
            showAllMarkers(listMarkers, null)
            viewModel.getAllAdsForAdapter(lat, lng, viewModel.isMyMarkers)
        }
    }

    private fun showAllMarkers(list: List<AdForMap>, chose: String?){
        val listMyMarkers = arrayListOf<AdForMap>()

        if(viewModel.isMyMarkers) {
            val myUid = viewModel.dbManager.mAuth.currentUser?.uid
            list.forEach {
                if (it.uid == myUid) listMyMarkers.add(it)
            }
        } else {
            listMyMarkers.addAll(list)
        }

        mMap.clear()
        val uid = viewModel.dbManager.mAuth.uid
        for(item in listMyMarkers){
            val target = LatLng(item.lat!!.toDouble(), item.lng!!.toDouble())
            val marker = mMap.addMarker(
                MarkerOptions().position(target)
                    .icon(BitmapDescriptorFactory.defaultMarker(item.markerColor ?: 15f))
                    .title("${item.gender}")
                    .snippet(item.name)
                    .draggable(false)
            )

            marker!!.tag = item.key
            //todo ?
            if(marker.tag == chose) marker.showInfoWindow()
            if(item.uid == uid) {
                marker.alpha = 0.6f
            }
        }
        setHasOptionsMenu(true)
    }

    private fun updateAdapter(listMainPhoto: ArrayList<AdShelter>) {
        adapter?.updateAdapter(listMainPhoto)
    }

    override fun onInfoWindowClick(markerInfo: Marker) {
        viewModel.mapFragToAddShelterFragId = AddShelterFragment.SHOW_DOG
        var bool = false
        viewModel.liveAdsDataForMapAdapter.value!!.forEach forInfo@{ info->
            if(info.key == markerInfo.tag){
                bool = true
                viewModel.openFragShelter(info)
                navController.navigate(R.id.addShelterFragment)
//                Toast.makeText(context as MainActivity, "${info.key}", Toast.LENGTH_LONG).show()
                return@forInfo
            }
        }
        Log.d("!!!bool", "${bool}")
        if(bool == false) {
            badgeImage?.setImageResource(R.drawable.ic_broken_image)
            val targ = markerInfo.position
            getAdsForAdapter(targ.latitude, targ.longitude)
        }
    }

    fun animateCameraFromAdapter(adShelter: AdShelter){
        if(job2 == null) {
            job2 = CoroutineScope(Dispatchers.Main).launch {
                //todo
                val target =
                    LatLng(adShelter.lat!!.toDouble(), adShelter.lng!!.toDouble())
                animateCamera(target, 10f)
                delay(1500)
                animateCamera(target, 12f)
                job2 = null

                showAllMarkers(listMarkers, adShelter.key)
            }
        }
    }

    private fun animateCamera(target: LatLng, zoom: Float) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(target, zoom))
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
        animateCamera(target, zoom)
        getAdsForAdapter(lat, lng)
    }

    private fun getAdsForAdapter(lat: Double, lng: Double) {
        rootElement!!.progressBarMap.visibility = View.VISIBLE
        if(job3 == null) viewModel.getAllAdsForAdapter(lat, lng, viewModel.isMyMarkers)
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        Log.d("!!!onStatusChanged", "MF $provider")
    }

    override fun onProviderEnabled(provider: String) {
        Log.d("!!!onProviderEnabled", "provider enabled")
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
        locationManager.removeUpdates(this)
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
        job2 = null
        job3 = null
        rootElement = null
        super.onDestroyView()
    }

    override fun onPause() {
        Log.d("!!!on", "onPauseMF")
        keyMarker = ""
        mapFragment?.onPause()
        super.onPause()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mapFragment?.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
        mapFragment?.onDetach()
    }

    override fun onLowMemory() {
        Log.d("!!!on", "onDestroyViewMF")
        super.onLowMemory()
        mapFragment?.onLowMemory()
    }

    override fun onMarkerClick(m: Marker): Boolean {
//        if(!isMarkerClicked)
        listMarkers.forEach {
            if(it.key == m.tag){
                keyMarker = it.key.toString()



                if(it.lat != null && it.lng != null){
                    getAdsForAdapter(it.lat.toDouble(), it.lng.toDouble())
                }
            }
        }
        return false
    }

    private fun scrollToPos(index: Int) {
        rootElement!!.rcViewMapPhoto.scrollToPosition(index)
    }

    fun constraint(i: Int) {
            cs.clone(rootElement!!.clFM)
            cs.constrainWidth(R.id.rcViewMapPhoto, i)
            cs.applyTo(rootElement!!.clFM)
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
//        private val permissions = arrayOf(
//            Manifest.permission.CAMERA,
//            Manifest.permission.RECORD_AUDIO,
//            Manifest.permission.READ_PHONE_STATE
//        )
    }



}