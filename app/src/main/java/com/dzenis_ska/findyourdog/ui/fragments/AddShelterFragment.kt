package com.dzenis_ska.findyourdog.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
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
import com.dzenis_ska.findyourdog.ui.utils.ProgressDialog
import com.dzenis_ska.findyourdog.ui.utils.SortListPhoto
import com.dzenis_ska.findyourdog.ui.utils.imageManager.ImageManager
import com.dzenis_ska.findyourdog.ui.utils.imageManager.ImagePicker
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.*
import kotlin.random.Random
import android.R.string




class AddShelterFragment : Fragment(), OnMapReadyCallback, LocationListener,
    GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener {
    val viewModel: BreedViewModel by activityViewModels()
    lateinit var breed: DogBreeds
    lateinit var vpAdapter: VpAdapter

    var rootElement: FragmentAddShelterBinding? = null

    var tlm: TabLayoutMediator? = null

    lateinit var navController: NavController
    var pref: SharedPreferences? = null
    private var counter: Long = 1000

    private lateinit var mMap: GoogleMap
    private lateinit var mapView: MapView

    private val cs = ConstraintSet()
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
    var imageIndex = 0
    val photoArrayList = mutableListOf<String>()
    var adapterArraySize = 0
    var sizeDog: Int = 1
    var telNum = "+"

    private lateinit var locationManager: LocationManager
    private val locationPermissionCode = 200


    //для определения последней локации
    private lateinit var fusedLocationClient: FusedLocationProviderClient

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

        val mapViewBundle = savedInstanceState?.getBundle(MAPVIEW_BUNDLE_KEY)
        mapView = rootElement!!.mapView
        mapView.onCreate(mapViewBundle)
        mapView.getMapAsync(this)
        mapView.requestDisallowInterceptTouchEvent(false)

        //инициализация переменной для получения последней локации
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context as Context)

        val dialog = ProgressDialog.createProgressDialog(activity as MainActivity, ProgressDialog.ADD_SHELTER_FRAGMENT)
        initViewModel()
        initRecyclerView()
        onClick(this, dialog)
    }

    fun hideAddShelterButton(bool: Boolean) {
        rootElement!!.apply{
            fabAddShelter.visibility = if (!bool) View.GONE else View.VISIBLE
            if(viewModel.btnDelState == false) {
                fabDeleteShelter.visibility = if (!bool) View.GONE else View.VISIBLE
            }else{
                fabDeleteShelter.visibility = View.GONE
            }
        }
    }
    private fun hidePhotoButtons(b: Boolean){
        if(!b){
        rootElement!!.apply {
                fabReplaceImage.visibility = View.GONE
                fabAddImage.visibility = View.GONE
                fabDeleteImage.visibility = View.GONE
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun initViewModel() {
        //Открываем AddShelterFragment и передаём данные
        viewModel.liveAdsDataAddShelter.observe(viewLifecycleOwner, { adShelter ->
            rootElement!!.apply {
                if (adShelter != null) {
                    if (adShelter.uid == viewModel.dbManager.auth.uid) {
                        fillFrag(adShelter, true)
                        boolEditOrNew = true
                        fabDeleteShelter.isVisible = true
                        ibGetLocation.isVisible = true
                    } else {
                        fillFrag(adShelter, false)
                    }
//                setMarker((adShelter.lat)!!.toDouble(), (adShelter.lng)!!.toDouble(), 11f)
                }
            }
        })
    }

    private fun fillFrag(adShelter: AdShelter, isEnabled: Boolean) {
        val listPhoto = adShelter.photoes

        rootElement!!.apply {
            if(listPhoto == null){
                imgAddPhoto.setImageDrawable(
                    ContextCompat.getDrawable(
                        context as MainActivity,
                        R.drawable.ic_no_one_photo
                    )
                )
            }else{
                CoroutineScope(Dispatchers.Main).launch {
                    adapterArraySize = listPhoto.size
                    val listUri = arrayListOf<Uri>()
                    viewModel.listPhoto(listPhoto)
                    listPhoto.forEach {
//                        val listUri = ImageManager.fromStorageToByteArray(it, activity as MainActivity)
                        listUri.add(it.toUri())
                        Log.d("!!!listUri", "1")
                    }
                    Log.d("!!!listUri", "2")
                    vpAdapter.updateAdapter(listUri, true)
                }
            }
            edTelNum.setText(adShelter.tel)
            edTelNum.isEnabled = isEnabled

            edName.setText(adShelter.name)
            edName.isEnabled = isEnabled
            tvGender.text = adShelter.gender
            tvGender.isClickable = isEnabled
            tvSize.text = adShelter.size
            tvSize.isClickable = isEnabled
            edDescription.setText(adShelter.description)
            edDescription.isEnabled = isEnabled
            shLat = (adShelter.lat)!!.toDouble()
            shLng = (adShelter.lng)!!.toDouble()
            hidePhotoButtons(isEnabled)
            if (!isEnabled) {

                fabAddShelter.visibility = View.GONE
                ivTel.visibility = View.VISIBLE

                clMain.background =
                    resources.getDrawable(R.drawable.background_write_fragment)
            } else {
                Log.d("!!!!", "${adShelter}")
                adShelterToEdit = adShelter
            }
            viewModel.openFragShelter(null)
        }
    }


    private fun fillAdShelter(photoUrlList: ArrayList<String>): AdShelter {
        var adShelter: AdShelter
        rootElement!!.apply {
            adShelter = AdShelter(
                edName.text.toString(),
                edTelNum.text.toString(),
                tvGender.text.toString(),
                tvSize.text.toString(),
                targetLat.toString(),
                targetLng.toString(),
                edDescription.text.toString(),
                photoUrlList,
                "empty",
                viewModel.dbManager.db.push().key,
                viewModel.dbManager.auth.uid,
                (Random.nextInt(0, 360)).toFloat()
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
        if (zoom != 11f) {
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

    private fun onClick(addShelterFragment: AddShelterFragment, dialog: AlertDialog) {
        rootElement!!.apply {
            imgAddPhoto.setOnClickListener() {
                fullScreen(250, 0.50f)
                imgAddPhoto.alpha = 0.8f
                hideAddShelterButton(false)
                ImagePicker.choosePhotoes(
                    activity as MainActivity,
                    addShelterFragment,
                    5,
                    ADD_PHOTO
                )
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
                val listDel = SortListPhoto.subStringDel(viewModel.listPhoto)
                if (listDel.size != 0) listDel.forEach { deletePhoto(it) }
                viewModel.deleteAdShelter(
                    adShelterToEdit,
                    object : BreedViewModel.WritedDataCallback {
                        override fun writedData() {
                            navController.popBackStack(R.id.addShelterFragment, true)
                            navController.navigate(R.id.mapsFragment)
                        }
                    })
            }

            fabAddShelter.setOnClickListener() {
                    publishImagesAd(dialog)
            }
            vp2.setOnClickListener {
                fullScreen(250, 0.50f)
            }
            ibFullScreen.setOnClickListener() {
                if (fs == 250) fullScreen(1000, 0.22f)
                else fullScreen(250, 0.50f)
            }
            ibGetLocation.setOnClickListener {
                getLocation()
                ibGetLocation.visibility = View.GONE
            }
            tvGender.setOnClickListener{
                if(tvGender.text == resources.getString(R.string.gendMan))
                    tvGender.text = resources.getText(R.string.gendWoMan)
                else tvGender.text = resources.getString(R.string.gendMan)
            }
            tvSize.setOnClickListener{
                when(sizeDog){
                    0-> {tvSize.text = resources.getString(R.string.sizeS)
                        sizeDog = 1}
                    1-> {tvSize.text = resources.getString(R.string.sizeM)
                        sizeDog = 2}
                    2-> {tvSize.text = resources.getString(R.string.sizeL)
                        sizeDog = 0}
                }
            }
            ivTel.setOnClickListener {
                call()
            }
//            edTelNum.requestFocus()
//                        edTelNum.setText(resources.getText(R.string.tel_num_shelter_enter))
//            edTelNum.post { edTelNum.setSelection(5) }

//            edTelNum.addTextChangedListener { editable ->
//                when (edTelNum.text.length) {
//                    8 -> {
//                        val text = edTelNum.text.toString()
//                        edTelNum.text.clear()
//                        edTelNum.setText("${text} ")
//                        edTelNum.post { edTelNum.setSelection(edTelNum.text.length) }
//                    }
//                }
//            }
        }
    }

    private fun publishImagesAd(dialog: AlertDialog) {
        Log.d("!!!transpImage1", "$imageIndex")
        dialog.show()
        val vpArray = vpAdapter.arrayPhoto
        val listPhotoForDel = SortListPhoto.listPhotoForDel(viewModel.listPhoto, vpArray)
        if (listPhotoForDel.size != 0 && viewModel.btnDelState == false) listPhotoForDel.forEach { deletePhoto(it) }
        if(vpAdapter.arrayPhoto.size != 0) {
            addPhoto(vpAdapter.arrayPhoto[imageIndex], dialog)
        }else{
            addPhoto(null, dialog)
        }
    }

    private fun addPhoto(uri: Uri?, dialog: AlertDialog) {
        if (vpAdapter.arrayPhoto.size == imageIndex) {
            publishAdShelter(fillAdShelter(photoArrayList as ArrayList<String>), dialog)
            return
        }
        Log.d("!!!transpImageNewUri", "${uri}")
        if(uri != null) oldOrNew(uri, dialog)
    }

    private fun oldOrNew(it: Uri, dialog: AlertDialog) {
        if (it.toString().contains("content")) {
            val arrayListUri = arrayListOf<Uri>()
            arrayListUri.add(it)
            CoroutineScope(Dispatchers.Main).launch {
                Log.d("!!!transpImageNew1", "1")
                val arrayListByteArray =
                    ImageManager.imageResize(arrayListUri, activity as MainActivity)
                Log.d("!!!transpImageNew2", "jopa")
                Log.d("!!!transpImageNew2", "${arrayListByteArray.stream().count()}")
                if(arrayListByteArray.size == 0){
                    imageIndex++
                    if (vpAdapter.arrayPhoto.size == imageIndex) {
                        addPhoto(null, dialog)
                    } else {
                        addPhoto(vpAdapter.arrayPhoto[imageIndex], dialog)
                    }
//                    photoArrayList.add("https://firebasestorage.googleapis.com/v0/b/findyourdog-6fa93.appspot.com/o/storage%2FdnUUHb4of0WAoF4xuDlV0J95hBy1%2Fimage_1631654334261?alt=media&token=d66cffec-f403-498c-b2e6-30afa473db41")
                    Toast.makeText(context, "Не удалось загрузить фото, возможно они повреждены", Toast.LENGTH_LONG).show()
                } else {
                    viewModel.publishPhoto(arrayListByteArray[0]) { uri ->

                        if (uri != null) {
                            photoArrayList.add(uri.toString())
                            Log.d("!!!transpImageNew3", "${uri.toString()}")
                            imageIndex++
                            if (vpAdapter.arrayPhoto.size == imageIndex) {
                                addPhoto(null, dialog)
                            } else {
                                addPhoto(vpAdapter.arrayPhoto[imageIndex], dialog)
                            }
                            Log.d("!!!transpImageNew3", "3")
                        }
                    }
                }
            }
        } else {
            photoArrayList.add(it.toString())
            imageIndex++
            if(vpAdapter.arrayPhoto.size == imageIndex) {
                addPhoto(null, dialog)
            }else{
                addPhoto(vpAdapter.arrayPhoto[imageIndex], dialog)
            }

            Log.d("!!!transpImageNew5", "${it}")
        }
    }

    private fun deletePhoto(s: String) {
        viewModel.deletePhoto(s){
            Log.d("!!!deletePhotoExeption", "Deleted")
        }
    }

    private fun publishAdShelter(adTemp: AdShelter, dialog: AlertDialog) {
        if (boolEditOrNew == true) {
//            Log.d("!!!uid", "${adTemp.uid} , ${viewModel.dbManager.auth.uid}")
            viewModel.publishAdShelter(
                adTemp
                    .copy(key = adShelterToEdit?.key,
                        markerColor = adShelterToEdit?.markerColor,
                        lat = shLat.toString(),
                        lng = shLng.toString()
                    ), dialog){
//                Log.d("!!!uidIt", "${it}}")
                navController.popBackStack(R.id.addShelterFragment, true)
                navController.popBackStack(R.id.mapsFragment, true)
                navController.navigate(R.id.mapsFragment)
            }
        } else {
//            Log.d("!!!uid2", "${adTemp.uid} , ${viewModel.dbManager.auth.uid}")
            viewModel.publishAdShelter(adTemp, dialog) {
//                Log.d("!!!uidIt2", "${it}")
//                viewModel.getAllAds()
                navController.popBackStack(R.id.addShelterFragment, true)
                navController.popBackStack(R.id.mapsFragment, true)
                navController.navigate(R.id.mapsFragment)
            }
        }
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

    private fun initRecyclerView() {
        vpAdapter = VpAdapter(this)
        rootElement!!.apply {
            vp2.adapter = vpAdapter
            tlm = TabLayoutMediator(tabLayout, vp2) { _, _ -> }
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
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
        if (shLat != null) {
//            if(viewModel.btnDelState == false)
                setMarker(shLat!!, shLng!!, 11f)
//            else
//                setMarker(shLat!!, shLng!!, 12f)
        } else {
//            setMarker(shLat!!, shLng!!, 12f)
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
        const val ADD_PHOTO = 10
        const val ADD_IMAGE = 20
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
    private fun call() = with(rootElement!!){
        val callUri = "tel:${edTelNum.text}"
        val iCall = Intent(Intent.ACTION_DIAL)
        iCall.data = callUri.toUri()
        startActivity(iCall)
    }

    override fun onMyLocationButtonClick(): Boolean {
        return false
    }

    override fun onMyLocationClick(p0: Location) {

    }

    override fun onProviderDisabled(provider: String) {
        // nop
    }

    override fun onProviderEnabled(provider: String) {
        // nop
    }
}


