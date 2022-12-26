package com.dzenis_ska.findyourdog.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
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
import androidx.activity.result.ActivityResult
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
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.*
import java.io.File
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

    var pushKey = ""

    var countTempPhoto = 0

    var plague: Long? = null
    var rabies: Long? = null
    var isSave: Boolean? = null
    private lateinit var locationManager: LocationManager


    //для определения последней локации
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var requestPhoto = 0

    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if(it != null) updatePermissionsState(it as MutableMap<String, Boolean>)
        }

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                // Handle the Intent
                val fileUri = intent?.let { UCrop.getOutput(it) }
                if (fileUri != null) {
                    Log.d("!!!ADD_PHOTO", "$requestPhoto _ $fileUri _ ")
                    when (requestPhoto) {
                        ADD_PHOTO -> vpAdapter.updateAdapter(listOf(fileUri), false)
                        ADD_IMAGE -> vpAdapter.updateAdapterForSinglePhoto(listOf(fileUri))
                        REPLACE_IMAGE -> vpAdapter.replaceItemAdapter(listOf(fileUri))
                    }
                }
            }
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

        locationManager =
            activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager


        val mapViewBundle = savedInstanceState?.getBundle(MAPVIEW_BUNDLE_KEY)
        Log.d("!!!SupportMapFragment", "SupportMapFragmentASF")
        mapView = rootElement!!.mapView
        mapView.onCreate(mapViewBundle)
        mapView.getMapAsync(this)
        mapView.requestDisallowInterceptTouchEvent(false)

        //инициализация переменной для получения последней локации
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context as Context)
//        getLocation()

        initViewModel()
        initRecyclerView()
        onClick()


        imagePicker =
            ImagePicker(activity?.activityResultRegistry!!, viewLifecycleOwner) { fileUri ->
                val uriF = File(requireContext().cacheDir, "temp${countTempPhoto++}.tmp")
                if (fileUri != null) {
                    val uCrop = UCrop.of(fileUri, Uri.fromFile(uriF))
                        .withAspectRatio(9f, 9f)
//                    .withMaxResultSize(maxWidth, maxHeight)
                        .getIntent(requireContext())

                    startForResult.launch(uCrop)
                }
            }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        Log.d("!!!onCreateOptionsMenu", "onCreateOptionsMenu")
        inflater.inflate(R.menu.menu_shelter_frag, menu)
        val item = menu.findItem(R.id.isFav)
        if (viewModel.adShelteAfterPhotoViewed != null) {
            if (viewModel.adShelteAfterPhotoViewed?.isFav!!) item.icon =
                resources.getDrawable(R.drawable.paw_red_2, context?.theme)
        } else {
            menu.clear()
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val dog = viewModel.adShelteAfterPhotoViewed
        return when (item.itemId) {
            R.id.isFav -> {
                dog?.let {
                    onFavClicked(it) { isFavorite ->
                        if (isFavorite) {
                            val adShelter =
                                viewModel.adShelteAfterPhotoViewed?.copy(isFav = isFavorite)
                            viewModel.adShelteAfterPhotoViewed = adShelter
                            item.icon = resources.getDrawable(R.drawable.paw_red_2, context?.theme)
                        } else {
                            val adShelter =
                                viewModel.adShelteAfterPhotoViewed?.copy(isFav = isFavorite)
                            viewModel.adShelteAfterPhotoViewed = adShelter
                            item.icon = resources.getDrawable(R.drawable.paw_blue, context?.theme)
                        }
                    }
                }
                true
            }
            R.id.dialogInfo -> {
                val dogS = viewModel.adShelteAfterPhotoViewed
                val info = """
                    |${res(R.string.name)} ${dogS?.name} 
                    |${res(R.string.count_call)} ${dogS?.callsCounter}
                    |${res(R.string.count_views)} ${dogS?.viewsCounter}
                    |${res(R.string.favorite)} ${dogS?.favCounter}
                """.trimIndent()
                toastL(info)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onFavClicked(dog: AdShelter, callback: (isFav: Boolean) -> Unit) {
        viewModel.onFavClick(dog) {
            callback(it)
        }
    }

    fun hideAddPhoto(b: Boolean) = with(rootElement!!) {
        imgAddPhoto.visibility = if (!b) View.GONE else View.VISIBLE
        clEditPhoto.visibility = if (!b) View.VISIBLE else View.GONE
    }

    private fun hidePhotoButtons(b: Boolean) {
        if (!b) {
            rootElement!!.apply {
                fabReplaceImage.visibility = View.GONE
                fabAddImage.visibility = View.GONE
                fabDeleteImage.visibility = View.GONE
            }
        }
    }

    private fun initViewModel() {
        Log.d("!!!setMarker", "${viewModel.mapFragToAddShelterFragId}")
        when (viewModel.mapFragToAddShelterFragId) {
            ADD_DOG -> getLocation()
            SHOW_DOG -> {}
            CHOOSE_PHOTO -> {}
            else -> {}
        }

        //Открываем AddShelterFragment и передаём данные
        viewModel.liveAdsDataAddShelter.observe(viewLifecycleOwner) { adShelter ->

            rootElement!!.apply {
                if (adShelter != null) {
                    viewModel.adShelteAfterPhotoViewed = adShelter
                    if (adShelter.uid == viewModel.dbManager.mAuth.uid) {
                        fillFrag(adShelter, true)
                        boolEditOrNew = true
                        fabDeleteShelter.isVisible = true
                        (activity as AppCompatActivity).supportActionBar?.title =
                            resources.getString(R.string.editing)

                        ibGetLocation.isVisible = true
                    } else {
                        fillFrag(adShelter, false)
                        (activity as AppCompatActivity).supportActionBar?.title = adShelter.name
                    }
                } else {
                    (activity as AppCompatActivity).supportActionBar?.title =
                        resources.getString(R.string.created)
                }
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun fillFrag(adShelter: AdShelter, isEnabled: Boolean) {
        val listPhoto = adShelter.photoes

        rootElement!!.apply {
            if (listPhoto == null) {
                imgAddPhoto.setImageDrawable(
                    ContextCompat.getDrawable(
                        context as MainActivity,
                        R.drawable.ic_no_one_photo
                    )
                )
            } else {
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


            val pl = adShelter.vaccination?.get(DialogCalendar.PLAGUE)
            val rab = adShelter.vaccination?.get(DialogCalendar.RABIES)

            Log.d("!!!pl", "${pl}")
            if (!pl.isNullOrEmpty()) {
                plague = pl.toLong()
                tvPlague.text = "От чумы привита"
            }
            if (!rab.isNullOrEmpty()) {
                rabies = rab.toLong()
                tvRabies.text = "От бешенства привита"
            }
            if (!pl.isNullOrEmpty() && !rab.isNullOrEmpty()) imIsVaccine.setImageResource(R.drawable.ic_done)


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
                    resources.getDrawable(R.drawable.background_write_fragment, context?.theme)

            } else {
                Log.d("!!!!", "${adShelter}")
                adShelterToEdit = adShelter

            }
//            viewModel.openFragShelter(null)
        }
    }


    private fun fillAdShelter(photoUrlList: ArrayList<String>): AdShelter {

        var adShelter: AdShelter
        val breed =
            if (rootElement!!.edBreed.text.isNotEmpty()) rootElement!!.edBreed.text.toString() else resources.getString(R.string.dog_breed_cosmo)
        val time = System.currentTimeMillis().toString()
        rootElement!!.apply {
            val dbManager = viewModel.dbManager
            val mAuth = dbManager.mAuth
            adShelter = AdShelter(
                edName.text.toString(),
                edTelNum.text.toString(),
                tvGender.text.toString(),
                tvSize.text.toString(),
                targetLat/*.toString()*/,
                targetLng/*.toString()*/,
                edDescription.text.toString(),
                breed,
                mAuth.currentUser?.email,
                mapOf(
                    PLAGUE to (plague?.toString() ?: ""),
                    RABIES to (rabies?.toString() ?: "")
                ),
                photoUrlList,
                EMPTY,
//                dbManager.db.push().key,
                pushKey,
                mAuth.uid,
                (Random.nextInt(0, 360)).toFloat(),
                time
            )
        }
        return adShelter
    }


    @SuppressLint("MissingPermission")
    private fun getLocation() {
        Log.d("!!!getLocation", "getLoc")
        // получаем последнюю локацию
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
//                    Log.d("!!!loc", "${location.longitude} ${location.latitude}")
//                    toastLong(R.string.your_last_location)
                    setMarker(location.latitude, location.longitude, 10f)
                    lastLat = location.latitude
                    lastLng = location.longitude
                    rootElement!!.ibGetLocation.visibility = View.VISIBLE
                    // Got last known location. In some rare situations this can be null.
                } else {
//                    toastLong(R.string.no_last_location)
                    isLocEnabled()
                }
            }
    }

//    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("MissingPermission")
    private fun isLocEnabled(){
//    Log.d("!!!isLocEnabled", "${locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)}")
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000,
                5f,
                this
            )
            toastLong(R.string.location_search)
        } else {
            toastLong(R.string.gps_is_not_enabled)
            setMarker(53.9303802, 27.5054665, 9f)
            rootElement!!.ibGetLocation.visibility = View.VISIBLE
        }
    }

    private fun setMarker(lat: Double, lng: Double, zoom: Float) {
//        Log.d("!!!setMarker", "$zoom")
        var target = LatLng(lat, lng)
        mMap.clear()

        val circleOptions = CircleOptions()
            .center(LatLng(lat, lng))
            .radius(50.0)
            .fillColor(resources.getColor(R.color.fill_color, context?.theme))
            .strokeColor(resources.getColor(R.color.main_background, context?.theme))
            .strokeWidth(10f)

        mMap.addCircle(circleOptions)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(target, zoom))
        val marker = mMap.addMarker(
            MarkerOptions().position(target)
                .title("Примерно тут!)")
                .draggable(
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
    private fun onClick() {

        rootElement!!.apply {

            imgAddPhoto.setOnClickListener() {
                if (fabDeleteImage.isVisible) {
                    fullScreen(250, 0.50f)
                    requestPhoto = ADD_PHOTO
                    requestPermissionsForMediaStore()
                }
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
//                val listDel = SortListPhoto.subStringDel(viewModel.listPhoto)
                val dialogDelete = ProgressDialog.createProgressDialog(
                    activity as MainActivity,
                    ProgressDialog.DELETE_ADD_SHELTER
                )
                dialogDelete.show()
                val listDel = viewModel.listPhoto
                if (listDel.size != 0) listDel.forEach { deletePhoto(it) }
                viewModel.deleteAdShelter(adShelterToEdit, dialogDelete){
                    navController.popBackStack(R.id.addShelterFragment, true)
                    navController.popBackStack(R.id.mapsFragment, true)
                    navController.navigate(R.id.mapsFragment)
                }
            }

            fabAddShelter.setOnClickListener() {
                val dialog = ProgressDialog.createProgressDialog(
                    activity as MainActivity,
                    ProgressDialog.ADD_SHELTER_FRAGMENT
                )
                pushKey = viewModel.dbManager.db.push().key.toString()
                publishImagesAd(dialog)
            }
            vp2.setOnClickListener {
                fullScreen(250, 0.50f)
            }
            ibFullScreen.setOnClickListener() {
                if (fs == 250) fullScreen(1200, 0.22f)
                else fullScreen(250, 0.50f)
            }
            ibGetLocation.setOnClickListener {
                ltlng = false
//                getLocation()
                ibGetLocation.visibility = View.GONE
                isLocEnabled()
            }
            tvGender.setOnClickListener {
                if (tvGender.text == resources.getString(R.string.gendMan))
                    tvGender.text = resources.getText(R.string.gendWoMan)
                else tvGender.text = resources.getString(R.string.gendMan)
            }
            tvSize.setOnClickListener {
                when (sizeDog) {
                    0 -> {
                        tvSize.text = resources.getString(R.string.sizeS)
                        sizeDog = 1
                    }
                    1 -> {
                        tvSize.text = resources.getString(R.string.sizeM)
                        sizeDog = 2
                    }
                    2 -> {
                        tvSize.text = resources.getString(R.string.sizeL)
                        sizeDog = 0
                    }
                }
            }
            ivTel.setOnClickListener {
                viewModel.adCalled()
                call()
            }
            tvVaccine.setOnClickListener {
                if (fbAuth.mAuth.currentUser?.uid == viewModel.adShelteAfterPhotoViewed?.uid ||
                    viewModel.mapFragToAddShelterFragId == ADD_DOG) {
                    groupVaccine.isVisible = !groupVaccine.isVisible
                }
            }
            ibPlague.setOnClickListener {
                val plagueString =
                    viewModel.adShelteAfterPhotoViewed?.vaccination?.get(DialogCalendar.PLAGUE)
                if (!plagueString.isNullOrEmpty()) {
                    plague = plagueString.toLong()
                    DialogCalendar.createDialogCalendar(
                        activity as MainActivity,
                        this@AddShelterFragment,
                        DialogCalendar.PLAGUE,
                        plagueString, isSave
                    )
                } else {
                    DialogCalendar.createDialogCalendar(
                        activity as MainActivity,
                        this@AddShelterFragment,
                        DialogCalendar.PLAGUE,
                        null, isSave
                    )
                }
            }
            ibRabies.setOnClickListener {
                val rabiesString =
                    viewModel.adShelteAfterPhotoViewed?.vaccination?.get(DialogCalendar.RABIES)
                if (!rabiesString.isNullOrEmpty()) {
                    rabies = rabiesString.toLong()
                    DialogCalendar.createDialogCalendar(
                        activity as MainActivity,
                        this@AddShelterFragment,
                        DialogCalendar.RABIES,
                        rabiesString, isSave
                    )
                } else {
                    DialogCalendar.createDialogCalendar(
                        activity as MainActivity,
                        this@AddShelterFragment,
                        DialogCalendar.RABIES,
                        null, isSave
                    )
                }
            }
        }
    }


    private fun publishImagesAd(dialog: AlertDialog) {
        Log.d("!!!transpImage1", "$imageIndex")
        dialog.show()
        val vpArray = vpAdapter.arrayPhoto
        val listPhotoForDel = SortListPhoto.listPhotoForDel(viewModel.listPhoto, vpArray)
//        toastL("${listPhotoForDel.size} photos removed")
        if (listPhotoForDel.size != 0) listPhotoForDel.forEach {
            deletePhoto(it)
        }
        if (vpAdapter.arrayPhoto.size != 0) {
            addPhoto(vpAdapter.arrayPhoto[imageIndex], dialog)
        } else {
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

        if (uri != null) {
            if (boolEditOrNew == true)
                oldOrNew(uri, dialog, adShelterToEdit?.key)
            else
                oldOrNew(uri, dialog, pushKey)
        }
    }

    @SuppressLint("CommitPrefEdits")
    private fun oldOrNew(it: Uri, dialog: AlertDialog, key: String?) {

        if (!it.toString().contains("https:")) {
            val arrayListUri = arrayListOf<Uri>()
            arrayListUri.add(it)
            CoroutineScope(Dispatchers.Main).launch {
                Log.d("!!!transpImageNew1", "1")
                val arrayListByteArray =
                    ImageManager.imageResize(arrayListUri, activity as MainActivity)
                Log.d("!!!transpImageNew2", "jopa")

//                if (arrayListByteArray.size == 0) {
//                    Log.d("!!!transpImageNew2", "jopa ${arrayListByteArray.size}")
//                    imageIndex++
//                    if (vpAdapter.arrayPhoto.size == imageIndex) {
//                        addPhoto(null, dialog)
//                    } else {
//                        addPhoto(vpAdapter.arrayPhoto[imageIndex], dialog)
//                    }
//                    toastL(res(R.string.did_not_load_photos))
//                } else {

                    viewModel.publishPhoto(arrayListByteArray[0], key) { uri ->
                        if (uri != null) {
                            photoArrayList.add(uri.toString())
                            Log.d("!!!transpImageNew3", "${uri}")
                            imageIndex++
                            if (vpAdapter.arrayPhoto.size == imageIndex) {
                                addPhoto(null, dialog)
                            } else {
                                addPhoto(vpAdapter.arrayPhoto[imageIndex], dialog)
                            }
                            Log.d("!!!transpImageNew3", "3")
                        }
                    }
//                }
            }
        } else {
            photoArrayList.add(it.toString())
            imageIndex++
            if (vpAdapter.arrayPhoto.size == imageIndex) {
                addPhoto(null, dialog)
            } else {
                addPhoto(vpAdapter.arrayPhoto[imageIndex], dialog)
            }

            Log.d("!!!transpImageNew5", "${it}")
        }
    }

    private fun deletePhoto(s: String) {
        Toast.makeText(requireActivity(), s.substring(1, 47), Toast.LENGTH_LONG).show()
        viewModel.deletePhoto(s)
    }

    private fun publishAdShelter(adTemp: AdShelter, dialog: AlertDialog) {

        if (boolEditOrNew == true) {
            var lt = shLat/*.toString()*/
            var lg = shLng/*.toString()*/
            if (!ltlng) {
                lt = adTemp.lat/*.toString()*/
                lg = adTemp.lng/*.toString()*/
            }
            viewModel.publishAdShelter(
                adTemp
                    .copy(
                        key = adShelterToEdit?.key,
                        markerColor = adShelterToEdit?.markerColor,
                        lat = lt,
                        lng = lg,
                        time = adShelterToEdit?.time.toString()
                    ),
                dialog
            ) {
                navController.popBackStack(R.id.addShelterFragment, true)
                navController.popBackStack(R.id.mapsFragment, true)
                navController.navigate(R.id.mapsFragment)
            }
        } else {
            viewModel.publishAdShelter(adTemp, dialog) {
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
            if (height == 1200) rootElement!!.ibFullScreen.setImageResource(R.drawable.ic_close_fullscreen)
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
        locationManager.removeUpdates(this)
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
        locationManager.removeUpdates(this)
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
            rootElement!!.ibGetLocation.visibility = View.VISIBLE
        }
    }

    private fun call() = with(rootElement!!) {
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

    @SuppressLint("SetTextI18n")
    fun vaccineDataEdit(
        vaccine: String,
        timeInMillis: Long,
        _year: Int,
        _month: Int,
        _dayOfMonth: Int
    ) = with(rootElement!!) {
        when (vaccine) {
            DialogCalendar.PLAGUE -> {
                plague = timeInMillis
//                val text = tvPlague.text.toString()
                tvPlague.text = "$_year.${_month + 1}.$_dayOfMonth"
            }
            DialogCalendar.RABIES -> {
                rabies = timeInMillis
//                val text = tvRabies.text.toString()
                tvRabies.text = "$_year.${_month + 1}.$_dayOfMonth"
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
            vaccination = mapOf(
                PLAGUE to (plague?.toString() ?: ""),
                RABIES to (rabies?.toString() ?: "")
            )
        )
        viewModel.adShelteAfterPhotoViewed = instState
        navController.navigate(R.id.onePhotoFragment)

        viewModel.getOnePhoto(uri.toString())
    }


    override fun onDetach() {
        super.onDetach()
        viewModel.adShelteAfterPhotoViewed = null
    }
    private fun updatePermissionsState(permMap: MutableMap<String, Boolean>) {
        var countPerm = 0
        permMap.forEach { map ->
            Log.d("!!!perm", "${map.key} _ ${map.value}")
            if (map.value) countPerm++
        }
        if (countPerm >= 2) {
//            viewModel.mapFragToAddShelterFragId = CHOOSE_PHOTO
            imagePicker?.selectImage()
        }
        else
            toastLong(R.string.need_permissions_for_use_photo)
//                return@updatePermissionsState
    }

    private fun requestPermissionsForMediaStore() {
        requestPermissions.launch(permissions)
    }


    companion object {
        private const val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"
        const val ADD_PHOTO = 10
        const val ADD_IMAGE = 20
        const val REPLACE_IMAGE = 40

        const val ADD_DOG = 101
        const val SHOW_DOG = 102
        const val CHOOSE_PHOTO = 103


        const val PLAGUE = "plague"
        const val RABIES = "rabies"
        const val EMPTY = "empty"


        const val COUNT_PHOTO = "COUNT_PHOTO"

        private val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        )
    }
}


