package com.dzenis_ska.findyourdog.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.*
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.dzenis_ska.findyourdog.R
import com.dzenis_ska.findyourdog.databinding.FragmentAddShelterBinding
import com.dzenis_ska.findyourdog.remoteModel.*
import com.dzenis_ska.findyourdog.remoteModel.firebase.AdShelter
import com.dzenis_ska.findyourdog.remoteModel.firebase.FBAuth
import com.dzenis_ska.findyourdog.ui.MainActivity
import com.dzenis_ska.findyourdog.ui.fragments.adapters.VpAdapter
import com.dzenis_ska.findyourdog.ui.utils.*
import com.dzenis_ska.findyourdog.ui.utils.imageManager.ImageManager
import com.dzenis_ska.findyourdog.viewModel.BreedViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.*
import java.util.*
import kotlin.random.Random


class AddShelterFragment : Fragment(), OnMapReadyCallback, LocationListener,
    GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener {

    val viewModel: BreedViewModel by activityViewModels()
    private val fbAuth = FBAuth()
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
    private val photoArrayList = mutableListOf<String>()
    var adapterArraySize = 0
    var sizeDog: Int = 1
    var ltlng: Boolean = true

    var plague: Long? = null
    var rabies: Long? = null
    var isSave: Boolean? = null
    private lateinit var locationManager: LocationManager


    //для определения последней локации
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var requestPhoto = 0

    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
            updatePermissionsState(it as MutableMap<String, Boolean>)
        }

    private var imagePicker: ImagePicker? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("!!!onCreateView", "AddShelterFragment")
        rootElement = FragmentAddShelterBinding.inflate(inflater)
        return rootElement!!.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d("!!!onSaveInstanceState", "AddShelterFragment")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("!!!onViewCreated", "AddShelterFragment")
        navController = findNavController()
        setHasOptionsMenu(true)


        val mapViewBundle = savedInstanceState?.getBundle(MAPVIEW_BUNDLE_KEY)
        Log.d("!!!SupportMapFragment", "SupportMapFragmentASF")
        mapView = rootElement!!.mapView
        mapView.onCreate(mapViewBundle)
        mapView.getMapAsync(this)
        mapView.requestDisallowInterceptTouchEvent(false)

        //инициализация переменной для получения последней локации
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context as Context)
        getLocation()
        val dialog = ProgressDialog.createProgressDialog(activity as MainActivity, ProgressDialog.ADD_SHELTER_FRAGMENT)
        initViewModel()
        initRecyclerView()
        onClick(dialog)
        initBackStack()

        imagePicker = ImagePicker(activity?.activityResultRegistry!!, viewLifecycleOwner) { fileUri ->
            Log.d("!!!imagePicker", "${fileUri} ")
            if(fileUri != null){
                when (requestPhoto) {
                    ADD_PHOTO -> vpAdapter.updateAdapter(listOf(fileUri), false)
                    ADD_IMAGE -> vpAdapter.updateAdapterForSinglePhoto(listOf(fileUri))
                    REPLACE_IMAGE -> vpAdapter.replaceItemAdapter(listOf(fileUri))
                }
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        Log.d("!!!onCreateOptionsMenu", "onCreateOptionsMenu")
        inflater.inflate(R.menu.menu_shelter_frag, menu)
        val item = menu.findItem(R.id.isFav)
        if(viewModel.adShelteAfterPhotoViewed != null) {
            if(viewModel.adShelteAfterPhotoViewed?.isFav!!) item.icon = resources.getDrawable(R.drawable.paw_red_2)
        } else {
            menu.clear()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val dog = viewModel.adShelteAfterPhotoViewed
        return when(item.itemId){
            R.id.isFav -> {
                dog?.let {
                    onFavClicked(it) { isFavorite ->
                        if (isFavorite) {
                            val adShelter =
                                viewModel.adShelteAfterPhotoViewed?.copy(isFav = isFavorite)
                            viewModel.adShelteAfterPhotoViewed = adShelter
                            item.icon = resources.getDrawable(R.drawable.paw_red_2)
                        } else {
                            val adShelter =
                                viewModel.adShelteAfterPhotoViewed?.copy(isFav = isFavorite)
                            viewModel.adShelteAfterPhotoViewed = adShelter
                            item.icon = resources.getDrawable(R.drawable.paw_blue)
                        }
                    }
                }
                true
            }
            R.id.dialogInfo -> {
                val dogS = viewModel.adShelteAfterPhotoViewed
                val info = """
                    имя: ${dogS?.name} 
                    количество звонков: ${dogS?.callsCounter}
                    количество просмотров: ${dogS?.viewsCounter}
                    избранное: ${dogS?.favCounter}
                """.trimIndent()
                Toast.makeText(context, info, Toast.LENGTH_LONG).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun onFavClicked(dog: AdShelter, callback: (isFav: Boolean) -> Unit){
        viewModel.onFavClick(dog){
            callback(it)
        }
    }

    fun hideAddPhoto(b: Boolean) = with(rootElement!!){
        imgAddPhoto.visibility = if(!b) View.GONE else View.VISIBLE
        clEditPhoto.visibility = if(!b) View.VISIBLE else View.GONE
    }

    //for choose photo from fragment
    fun hideAddShelterButton(bool: Boolean) {
        rootElement!!.apply {
            when (bool) {
                false -> {
                    constraintLayout2.visibility = View.GONE
                    scrollView.visibility = View.GONE
                }
                else -> {
                    constraintLayout2.visibility = View.VISIBLE
                    scrollView.visibility = View.VISIBLE
                }
            }
            fabAddShelter.visibility = if (!bool) View.GONE else View.VISIBLE
            if (viewModel.btnDelState == false) {
                fabDeleteShelter.visibility = if (!bool) View.GONE else View.VISIBLE
            } else {
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
            Log.d("!!!onviewModelASF", "AddShelterFragment ${adShelter?.description}")
            rootElement!!.apply {
                if(viewModel.adShelteAfterPhotoViewed != null){
                    if (viewModel.adShelteAfterPhotoViewed!!.uid == viewModel.dbManager.mAuth.uid) {
                        fillFrag(viewModel.adShelteAfterPhotoViewed!!, true)
                        boolEditOrNew = true
                        fabDeleteShelter.isVisible = true
                        ibGetLocation.isVisible = true
                    } else {
                        fillFrag(viewModel.adShelteAfterPhotoViewed!!, false)
                    }
                } else if (adShelter != null) {
                    viewModel.adShelteAfterPhotoViewed = adShelter
                    if (adShelter.uid == viewModel.dbManager.mAuth.uid) {
                        fillFrag(adShelter, true)
                        boolEditOrNew = true
                        fabDeleteShelter.isVisible = true
                        ibGetLocation.isVisible = true
                    } else {
                        fillFrag(adShelter, false)
                    }
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
            (activity as AppCompatActivity).supportActionBar?.title = adShelter.name

            val pl = adShelter.vaccination?.get(DialogCalendar.PLAGUE)
            val rab = adShelter.vaccination?.get(DialogCalendar.RABIES)

            if(pl != "null") {
                plague = pl?.toLong()
                tvPlague.text = "От чумы привита"
            }
            if(rab != "null") {
                imIsVaccine.setImageResource(R.drawable.ic_done)
                rabies = rab?.toLong()
                tvRabies.text = "От бешенства привита"
            }


            edTelNum.setText(adShelter.tel)
            edTelNum.isEnabled = isEnabled

            edName.setText(adShelter.name)
            edName.isEnabled = isEnabled

            Log.d("!!!breed", "${adShelter.breed}")
            edBreed.setText(adShelter.breed)
            edBreed.isEnabled = isEnabled

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

                isSave = isEnabled

                fabAddShelter.visibility = View.GONE
                ivTel.visibility = View.VISIBLE

                clMain.background =
                    resources.getDrawable(R.drawable.background_write_fragment)
            } else {
                Log.d("!!!!", "${adShelter}")
                adShelterToEdit = adShelter
            }
//            viewModel.openFragShelter(null)
        }
    }


    private fun fillAdShelter(photoUrlList: ArrayList<String>): AdShelter {

        var adShelter: AdShelter
        val breed = if(rootElement!!.edBreed.text.isNotEmpty()) rootElement!!.edBreed.text.toString() else "без породы"
        val time = System.currentTimeMillis().toString()
        rootElement!!.apply {
            adShelter = AdShelter(
                edName.text.toString(),
                edTelNum.text.toString(),
                tvGender.text.toString(),
                tvSize.text.toString(),
                targetLat.toString(),
                targetLng.toString(),
                edDescription.text.toString(),
                breed,
                "empty",
                mapOf("plague" to plague.toString(), "rabies" to rabies.toString()),
                photoUrlList,
                "empty",
                viewModel.dbManager.db.push().key,
                viewModel.dbManager.mAuth.uid,
                (Random.nextInt(0, 360)).toFloat(),
                time
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
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
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
            }
        }
    }

    @SuppressLint("RestrictedApi")
    private fun onClick(dialog: AlertDialog) {

        rootElement!!.apply {
            imgAddPhoto.setOnClickListener() {
                fullScreen(250, 0.50f)
                requestPhoto = ADD_PHOTO
                requestPermissionsForMediaStore()
            }

            fabAddImage.setOnClickListener() {
                fullScreen(250, 0.50f)
                requestPhoto = ADD_IMAGE
                requestPermissionsForMediaStore()
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
                requestPhoto = REPLACE_IMAGE
                requestPermissionsForMediaStore()
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
                ltlng = false
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
                viewModel.adCalled()
                call()
            }
            tvVaccine.setOnClickListener {
                if(fbAuth.mAuth.currentUser?.uid == viewModel.adShelteAfterPhotoViewed?.uid ||
                    viewModel.btnDelState == true)
                        ibPlague.isVisible = !ibPlague.isVisible
            }
            ibPlague.setOnClickListener {
                if(plague == null) plague = viewModel.adShelteAfterPhotoViewed?.vaccination?.get(DialogCalendar.PLAGUE)?.toLong()
                DialogCalendar.createDialogCalendar(activity as MainActivity,
                    this@AddShelterFragment,
                    DialogCalendar.PLAGUE,
                    plague.toString(), isSave)
            }
            ibRabies.setOnClickListener {
                if(rabies == null) rabies = viewModel.adShelteAfterPhotoViewed?.vaccination?.get(DialogCalendar.RABIES)?.toLong()
                DialogCalendar.createDialogCalendar(
                    activity as MainActivity,
                    this@AddShelterFragment,
                    DialogCalendar.RABIES,
                    rabies.toString(), isSave)
            }
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
        Log.d("!!!photoUrlList1", "${uri}")
        if (vpAdapter.arrayPhoto.size == imageIndex) {
            val adTemp = fillAdShelter(photoArrayList as ArrayList<String>)
            publishAdShelter(adTemp, dialog)
            return
        }

        if(uri != null) oldOrNew(uri, dialog)
    }

    private fun oldOrNew(it: Uri, dialog: AlertDialog) {

        if (!it.toString().contains("https:")) {
            val arrayListUri = arrayListOf<Uri>()
            arrayListUri.add(it)
            CoroutineScope(Dispatchers.Main).launch {
                Log.d("!!!transpImageNew1", "1")
                val arrayListByteArray =
                    ImageManager.imageResize(arrayListUri, activity as MainActivity)
                Log.d("!!!transpImageNew2", "jopa")

                if(arrayListByteArray.size == 0){
                    Log.d("!!!transpImageNew2", "jopa ${arrayListByteArray.size}")
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
        viewModel.deletePhoto(s)
    }

    private fun publishAdShelter(adTemp: AdShelter, dialog: AlertDialog) {


        if (boolEditOrNew == true) {
//            Log.d("!!!uid", "${adTemp.uid} , ${viewModel.dbManager.auth.uid}")

                var lt = shLat.toString()
                var lg = shLng.toString()
            if(!ltlng) {
                lt = adTemp.lat.toString()
                lg = adTemp.lng.toString()
            }
            viewModel.publishAdShelter(
                adTemp
                    .copy(key = adShelterToEdit?.key,
                        markerColor = adShelterToEdit?.markerColor,
                        lat = lt,
                        lng = lg,
                        time = adShelterToEdit?.time.toString()
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
        Log.d("!!!onResume", "AddShelterFragment")
        pref = this.activity?.getSharedPreferences("FUCK", 0)
        counter = pref?.getLong("counter", 5000)!!
        mapView.onResume()
    }

    override fun onDestroy() {
        Log.d("!!!onDestroy", "AddShelterFragment")
        mapView.onDestroy()
        super.onDestroy()
    }

    override fun onDestroyView() {
        Log.d("!!!onDestroyView", "AddShelterFragment")
        super.onDestroyView()
        if (viewModel.locationManagerBool) {
            locationManager.removeUpdates(this)
            viewModel.locationManagerBool = false
        }
//        viewModel.adShelteAfterPhotoViewed = null
        rootElement = null
    }

    override fun onStart() {
        Log.d("!!!onStart", "AddShelterFragment")
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        Log.d("!!!onStop", "AddShelterFragment")
        mapView.onStop()

        super.onStop()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.d("!!!", "onMapReady")
        mMap = googleMap
        if (shLat != null) setMarker(shLat!!, shLng!!, 11f)

        mMap.setOnMyLocationButtonClickListener(this)
        mMap.setOnMyLocationClickListener(this)
    }

    override fun onPause() {
        Log.d("!!!onPause", "AddShelterFragment")
        mapView.onPause()
        if (viewModel.locationManagerBool) {
            locationManager.removeUpdates(this)
            viewModel.locationManagerBool = false
        }
        super.onPause()
    }

    override fun onLowMemory() {
        Log.d("!!!onLowMemory", "AddShelterFragment")
        super.onLowMemory()
        mapView.onLowMemory()
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

    fun vaccineDataEdit(
        vaccine: String,
        timeInMillis: Long,
        _year: Int,
        _month: Int,
        _dayOfMonth: Int
    )  = with(rootElement!!){
        when(vaccine){
            DialogCalendar.PLAGUE-> {
                plague = timeInMillis
                val text = tvPlague.text.toString()
                tvPlague.text = "$text $_year.${_month+1}.$_dayOfMonth"
            }
            DialogCalendar.RABIES-> {
                rabies = timeInMillis
                val text = tvRabies.text.toString()
                tvRabies.text = "$text $_year.${_month+1}.$_dayOfMonth"
            }
        }
    }

    fun toFragOnePhoto(uri: Uri) = with(rootElement!!) {

        val instState = viewModel.adShelteAfterPhotoViewed?.copy(
            tel = edTelNum.text.toString(),
            name = edName.text.toString(),
            breed = edBreed.text.toString(),
            gender = tvGender.text.toString(),
            size = tvSize.text.toString(),
            description = edDescription.text.toString(),
            vaccination = mapOf("plague" to plague.toString(), "rabies" to rabies.toString())
        )
        viewModel.adShelteAfterPhotoViewed = instState
        navController.navigate(R.id.onePhotoFragment)
        viewModel.getOnePhoto(uri.toString())
    }

    @SuppressLint("RestrictedApi")
    private fun initBackStack() {
        InitBackStack.initBackStack(navController)
    }

    override fun onDetach() {
        super.onDetach()
        viewModel.adShelteAfterPhotoViewed = null

    }

    private fun updatePermissionsState(permMap: MutableMap<String, Boolean>) {
        var countPerm = 0
        permMap.forEach{map->
            Log.d("!!!perm", "${map.key} _ ${map.value}")
            if(map.value != true) {
                Toast.makeText(context as MainActivity, "Необходимы разрешения для использования фоток!", Toast.LENGTH_LONG).show()
                return@updatePermissionsState
            } else {
                countPerm++
            }
            if(countPerm == 2) imagePicker?.selectImage()
        }
    }

    private fun requestPermissionsForMediaStore() {
        requestPermissions.launch(permissions)
    }


    companion object {
        private const val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"
        const val ADD_PHOTO = 10
        const val ADD_IMAGE = 20
        const val REPLACE_IMAGE = 40
                private val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
                )
    }
}


