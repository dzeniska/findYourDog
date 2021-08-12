package com.dzenis_ska.findyourdog.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.dzenis_ska.findyourdog.R
import com.dzenis_ska.findyourdog.remoteModel.*
import com.dzenis_ska.findyourdog.ui.MainActivity
import com.dzenis_ska.findyourdog.viewModel.BreedViewModel
import com.dzenis_ska.findyourdog.databinding.FragmentAddShelterBinding
import com.dzenis_ska.findyourdog.remoteModel.firebase.AdShelter
import com.dzenis_ska.findyourdog.ui.fragments.adapters.VpAdapter
import com.dzenis_ska.findyourdog.ui.utils.imageManager.ImageManager
import com.dzenis_ska.findyourdog.ui.utils.imageManager.ImagePicker
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_add_shelter.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import kotlin.random.Random

class AddShelterFragment : Fragment(), OnMapReadyCallback, LocationListener,
    GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener {
    val viewModel: BreedViewModel by activityViewModels()
    lateinit var breed: DogBreeds
    lateinit var textName: String
    lateinit var vpAdapter: VpAdapter

    var rootElement: FragmentAddShelterBinding? = null

    var tlm: TabLayoutMediator? = null

    lateinit var navController: NavController
    var pref: SharedPreferences? = null
    private var counter: Long = 1000

    private lateinit var mMap: GoogleMap
    private lateinit var mapView: MapView

    val cs = ConstraintSet()
    var fs = 250
    var lat: Double = 0.0
    var lng: Double = 0.0
    var lastLat: Double = 0.0
    var lastLng: Double = 0.0
    var targetLat: Double = 0.0
    var targetLng: Double = 0.0
    var shLat: Double? = null
    var shLng: Double? = null
    var adShelterToEdit: AdShelter? = null
    var boolEditOrNew: Boolean? = false

    var job: Job? = null

    private lateinit var locationManager: LocationManager
    private val locationPermissionCode = 200

    //для определения последней локации
    private lateinit var fusedLocationClient: FusedLocationProviderClient

//    var launcherMultiSelectImage: ActivityResultLauncher<Intent>? = null
//    var launcherSingleSelectImage: ActivityResultLauncher<Intent>? = null
//    var launcherReplaceSelectedImage: ActivityResultLauncher<Intent>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootElement = FragmentAddShelterBinding.inflate(inflater)
        return rootElement!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
//        val mapFragment = childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment?
//        mapFragment?.getMapAsync(this)
        val mapViewBundle = savedInstanceState?.getBundle(MAPVIEW_BUNDLE_KEY)
        mapView = rootElement!!.mapView
        mapView.onCreate(mapViewBundle)
        mapView.getMapAsync(this)
        mapView.requestDisallowInterceptTouchEvent(false)

        //инициализация переменной для получения последней локации
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context as Context)

        init()
        initViewModel()
        initRecyclerView()
        onClick(this)

    }
    fun hideAddShelterButton(bool:Boolean){
        rootElement!!.fabAddShelter.visibility = if(!bool)  View.GONE else View.VISIBLE
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun initViewModel() {

//        виёв модель не катит или покатило
        viewModel.liveAdsDataAddShelter.observe(viewLifecycleOwner,{adShelter ->
            rootElement.apply {
                if (adShelter != null) {
                    if(adShelter.uid == viewModel.dbManager.auth.uid){
                        fillFrag(adShelter, true)
                        boolEditOrNew = true
                        fabDeleteShelter.isVisible = true
                    }else {
                        fillFrag(adShelter, false)
                    }
//                setMarker((adShelter.lat)!!.toDouble(), (adShelter.lng)!!.toDouble(), 11f)
                }
            }
        })
    }

    private fun fillFrag(adShelter: AdShelter, isEnabled: Boolean) {
        rootElement.apply {
            imgAddPhoto.setImageDrawable(
                ContextCompat.getDrawable(
                    context as MainActivity,
                    R.drawable.ic_no_one_photo
                )
            )
            edTelNum.setText(adShelter.tel)
            edTelNum.isEnabled = isEnabled
            edDescription.setText(adShelter.description)
            edDescription.isEnabled = isEnabled

            if(!isEnabled) {
                shLat = (adShelter.lat)!!.toDouble()
                shLng = (adShelter.lng)!!.toDouble()
                fabAddShelter.visibility = View.GONE
                clMain.background =
                    resources.getDrawable(R.drawable.background_write_fragment)
            }else{
                Log.d("!!!!", "${adShelter}")
                adShelterToEdit = adShelter
            }
            viewModel.openFragShelter(null)
        }
    }

    private fun fillAdShelter(): AdShelter {
        val adShelter: AdShelter
        rootElement.apply {
                adShelter = AdShelter(
                    "",
                    edTelNum.text.toString(),
                    targetLat.toString(),
                    targetLng.toString(),
                    edDescription.text.toString(),
                    "",
                    viewModel.dbManager.db.push().key,
                    viewModel.dbManager.auth.uid,
                    (Random.nextInt(0,  360)).toFloat()
                )
            }
        return adShelter
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        Log.d("!!!", "getLoc")
        // получаем последнюю локацию
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
//                    Log.d("!!!loc", "${location.longitude} ${location.latitude}")
                    Toast.makeText(context, "Find your last location!", Toast.LENGTH_LONG).show()
                    setMarker(location.latitude, location.longitude, 10f)
                    lastLat = location.latitude
                    lastLng = location.longitude
                    // Got last known location. In some rare situations this can be null.
                } else {
                    Toast.makeText(context, "No last location!", Toast.LENGTH_LONG).show()
                }
            }

        locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        viewModel.locationManagerBool = true
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 5f, this)
    }

    private fun setMarker(lat: Double, lng: Double, zoom: Float) {
        var target = LatLng(lat, lng)
        mMap.clear()

        val circleOptions = CircleOptions()
            .center(LatLng(lat, lng))
            .radius(50.0)
            .fillColor(resources.getColor(R.color.fill_color))
            .strokeColor(resources.getColor(R.color.main_background))
            .strokeWidth(10f)

        mMap.addCircle(circleOptions)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(target, zoom))
        val marker = mMap.addMarker(
            MarkerOptions().position(target).title("Примерно тут!)").draggable(
                false
            )
        )
        if(zoom != 11f) {
            mMap.setOnCameraMoveListener {
                marker!!.position = mMap.cameraPosition.target //to center in map
                target = marker.position

                //здесь сохраняем данные местоположения
                targetLat = target.latitude
                targetLng = target.longitude
//            Log.d("!!!target", "target $target")
            }
        }
    }

    private fun onClick(addShelterFragment: AddShelterFragment) {
        rootElement!!.apply {
            imgAddPhoto.setOnClickListener() {
                fullScreen(250, 0.50f)
                imgAddPhoto.alpha = 0.8f
                hideAddShelterButton(false)
                ImagePicker.choosePhotoes(activity as MainActivity, addShelterFragment, 5, ADD_PHOTO)
            }
            fabAddImage.setOnClickListener() {
                fullScreen(250, 0.50f)
                hideAddShelterButton(false)
                ImagePicker.choosePhotoes(
                    activity as MainActivity,
                    addShelterFragment,
                    5 - vpAdapter.arrayPhoto.size,
                    ADD_IMAGE
                )
            }
            fabDeleteImage.setOnClickListener() {
                fullScreen(250, 0.50f)
                CoroutineScope(Dispatchers.Main).launch {
                    val num = viewModel.numPage
                    vpAdapter.removeItemAdapter(num)
                }
            }
            fabReplaceImage.setOnClickListener() {
                fullScreen(250, 0.50f)
                hideAddShelterButton(false)
                ImagePicker.choosePhotoes(
                    activity as MainActivity,
                    addShelterFragment,
                    1,
                    REPLACE_IMAGE
                )
            }
            
            fabDeleteShelter.setOnClickListener { 
                viewModel.deleteAdShelter(adShelterToEdit, object : BreedViewModel.WritedDataCallback{
                    override fun writedData() {
                        navController.popBackStack(R.id.addShelterFragment, true)
                        navController.navigate(R.id.mapsFragment)

                    }

                })
            }
            
            fabAddShelter.setOnClickListener() {
                job = CoroutineScope(Dispatchers.Main).launch {
                    val list = ImageManager.imageResize( vpAdapter.arrayPhoto, activity as MainActivity)
                    Log.d("!!!resize","${list}")
                    val adShelter = fillAdShelter()
                    val byteArray = prepareImageBiteArray(list[0])
                    viewModel.publishPhoto(byteArray, adShelter, object : BreedViewModel.WritedDataCallback{
                        override fun writedData() {
                            Log.d("susses", "susses")
                        }
                    })
                }



                /*val adTemp = fillAdShelter()
                if(boolEditOrNew == true){
                    Log.d("!!!uid", "${adTemp.uid} , ${viewModel.dbManager.auth.uid}")

                    viewModel.publishAdShelter(adTemp.copy(key = adShelterToEdit?.key, markerColor = adShelterToEdit?.markerColor ), object : BreedViewModel.WritedDataCallback{
                        override fun writedData() {
                            navController.popBackStack(R.id.addShelterFragment, true)
                            navController.navigate(R.id.mapsFragment)
                        }
                    })
                }else {
                    Log.d("!!!uid", "${adTemp.uid} , ${viewModel.dbManager.auth.uid}")
                    viewModel.publishAdShelter(adTemp, object : BreedViewModel.WritedDataCallback {
                        override fun writedData() {
                            navController.popBackStack(R.id.addShelterFragment, true)
                            navController.navigate(R.id.mapsFragment)
                        }
                    })
                }*/
            }
            vp2.setOnClickListener {
                fullScreen(250, 0.50f)
            }
            ibFullScreen.setOnClickListener() {
                if (fs == 250) fullScreen(1000, 0.22f)
                else fullScreen(250, 0.50f)
            }
            ibGetLocation.setOnClickListener{
                getLocation()
                ibGetLocation.visibility = View.GONE
            }
        }
    }
    private fun prepareImageBiteArray(bitMap: Bitmap): ArrayList<ByteArray>{
        val outStream = ByteArrayOutputStream()
        bitMap.compress(Bitmap.CompressFormat.JPEG, 25, outStream)
        val list = ArrayList<ByteArray>()
        list.add(outStream.toByteArray())
        return list
    }

    fun fullScreen(height: Int, percent: Float) {
        if (fs != height) {
            cs.clone(rootElement!!.clDescr)
            cs.constrainHeight(R.id.mapView, height)
            cs.applyTo(rootElement!!.clDescr)
            rootElement!!.guideline4.setGuidelinePercent(percent)
            if (height == 1000) rootElement!!.ibFullScreen.setImageResource(R.drawable.ic_close_fullscreen)
            else rootElement!!.ibFullScreen.setImageResource(R.drawable.ic_open_in_full)
            fs = height
        }
    }


    private fun init() {

    }

    private fun initRecyclerView() {
        vpAdapter = VpAdapter(this)
        rootElement!!.apply {
            edTelNum.requestFocus()

            vp2.adapter = vpAdapter
            tlm = TabLayoutMediator(tabLayout, vp2) { tab, position -> }
            tlm?.attach()

            vp2.registerOnPageChangeCallback(
                object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        viewModel.numPage = position
//                        Log.d("!!!", "${position}")
                    }
                }
            )
        }
    }

    fun tabLayoutMediator(b: Boolean) {
        if (b) {
            rootElement!!.tabLayout.visibility = View.VISIBLE

        } else {
//            tlm?.detach()
            rootElement!!.tabLayout.visibility = View.GONE

        }
    }

    //    counter++
//    saveData(counter)
    fun saveData(i: Long) {
        val edit = pref?.edit()
        edit?.putLong("counter", i)
        edit?.apply()
    }



    override fun onResume() {
        super.onResume()
        Log.d("!!!", "onResume")
        pref = this.activity?.getSharedPreferences("FUCK", 0)
        counter = pref?.getLong("counter", 5000)!!
        mapView.onResume()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
        rootElement = null
    }


    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        mapView.onStop()
        if (viewModel.locationManagerBool) {
            locationManager.removeUpdates(this)
            viewModel.locationManagerBool = false
        }
        super.onStop()
    }


    override fun onMapReady(googleMap: GoogleMap) {
        Log.d("!!!", "onMapReady")
        mMap = googleMap
        if(shLat != null){
            setMarker(shLat!!, shLng!!, 11f)
        }else{
            getPermission()
        }

        mMap.setOnMyLocationButtonClickListener(this)
        mMap.setOnMyLocationClickListener(this)
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
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity as MainActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionCode
            )
        } else {
            getLocation()
//            mMap.isMyLocationEnabled = true
        }
    }

    override fun onPause() {
        mapView.onPause()
        if (viewModel.locationManagerBool) {
            locationManager.removeUpdates(this)
            viewModel.locationManagerBool = false
        }
        super.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    companion object {
        private const val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"
        private const val LOCATION_PERMISSION_REQUEST_CODE_1 = 2
        const val ADD_PHOTO = 10
        const val ADD_IMAGE = 20
        const val DELETE_IMAGE = 30
        const val REPLACE_IMAGE = 40
    }

    override fun onLocationChanged(location: Location) {
        Log.d("!!!", "onLocationChanged")
        lat = location.latitude
        lng = location.longitude
        setMarker(lat, lng, 17f)
        if (lat != lastLat && lng != lastLng) {
            locationManager.removeUpdates(this)
            viewModel.locationManagerBool = false
            rootElement!!.ibGetLocation.visibility = View.VISIBLE
        }
    }

    override fun onMyLocationButtonClick(): Boolean {
        return false
    }

    override fun onMyLocationClick(p0: Location) {

    }

//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        if (requestCode != AddShelterFragment.LOCATION_PERMISSION_REQUEST_CODE_1) {
//            return
//        }
//        if (requestCode == locationPermissionCode) {
//            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(activity as MainActivity, "Permission Granted", Toast.LENGTH_SHORT)
//                    .show()
//            } else {
//                Toast.makeText(activity as MainActivity, "Permission Denied", Toast.LENGTH_SHORT)
//                    .show()
//            }
//        }
//    }

}


