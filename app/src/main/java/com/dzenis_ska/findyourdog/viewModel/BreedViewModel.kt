package com.dzenis_ska.findyourdog.viewModel

import android.app.AlertDialog
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dzenis_ska.findyourdog.remoteModel.DogBreeds
import com.dzenis_ska.findyourdog.Repository.Repository
import com.dzenis_ska.findyourdog.remoteModel.firebase.AdForMap
import com.dzenis_ska.findyourdog.remoteModel.firebase.AdShelter
import com.dzenis_ska.findyourdog.remoteModel.firebase.DbManager
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BreedViewModel(private val repository: Repository) : ViewModel() {


    private val scope = CoroutineScope(Dispatchers.IO)

    val  imgBreedList = mutableListOf<String>()
    val selectBreed = arrayListOf<String>()
    var fileName: String = ""
    var selectedBreed: DogBreeds? = null
//    var locationManagerBoolMF: Boolean = false
    var numPage: Int = 0
    var isFav: Boolean = false


    var isMyMarkers = false

    var isFirstLaunchMapsFragment = true


    val breedLive = MutableLiveData<MutableList<DogBreeds>>()

    val breedFavLive = MutableLiveData<MutableList<DogBreeds>>()

    val breedItemLive: MutableLiveData<MutableList<String>> by lazy {
        MutableLiveData<MutableList<String>>()
    }

    private val _onePhoto = MutableLiveData<ByteArray>()
    val onePhoto: LiveData<ByteArray> = _onePhoto

    private val _showAd = MutableLiveData<Int>()
    val showAd: LiveData<Int> = _showAd



    var userUpdate = MutableLiveData<FirebaseUser?>()
    val dbManager = DbManager()
    val liveAdsDataForMapAdapter = MutableLiveData<ArrayList<AdShelter>>()
    val liveAdsDataAllMarkers = MutableLiveData<ArrayList<AdForMap>>()
    val liveAdsDataAddShelter = MutableLiveData<AdShelter?>()
    val listPhoto = arrayListOf<String>()
    val dialogMutableLiveData = MutableLiveData<AlertDialog>()
    var adShelteAfterPhotoViewed: AdShelter? = null

    var mapFragToAddShelterFragId = 0

//////////////////////////////////////////////////////////////////////

    fun listPhoto(listP: ArrayList<String>) {
        listPhoto.clear()
        listPhoto.addAll(listP)
    }

    fun uiUpdateMain(user: FirebaseUser?) {
        userUpdate.postValue(user)
    }


    fun openFragShelter(adShelter: AdShelter?){
        liveAdsDataAddShelter.value = adShelter
        adShelter?.let { adViewed(it, VIEWS_COUNTER) }
    }


    fun deleteAdShelter(adShelter: AdShelter?, dialog: AlertDialog, callback: (deleted: String) -> Unit){

        dialogMutableLiveData.value = dialog
        dbManager.deleteAdShelter(adShelter){
                val updateList = liveAdsDataForMapAdapter.value
                updateList?.remove(adShelter)
                liveAdsDataForMapAdapter.postValue(updateList!!)
            callback(it)
        }
    }

    private fun adViewed(adShelter: AdShelter, anyCounter: Int) {

        dbManager.adViewed(adShelter, anyCounter, object : DbManager.FinishWorkListener {
            override fun onFinish() {
                if (adShelteAfterPhotoViewed != null) {
                    when (anyCounter) {
                        VIEWS_COUNTER -> {
                            val viewCounter = adShelter.viewsCounter.toInt() + 1
                            adShelteAfterPhotoViewed =
                                adShelteAfterPhotoViewed?.copy(viewsCounter = viewCounter.toString())
                        }
                        CALLS_COUNTER -> {
                            val callCounter = adShelter.callsCounter.toInt() + 1
                            adShelteAfterPhotoViewed =
                                adShelteAfterPhotoViewed?.copy(
                                    callsCounter = callCounter.toString()
                                )
                        }
                    }
                }
            }
        })
    }
    fun adCalled(){
        adShelteAfterPhotoViewed?.let { adViewed(it, CALLS_COUNTER) }
    }
    fun deletePhoto(url: String){
        dbManager.deletePhoto(url)
    }
    fun publishPhoto(adTemp: ByteArray, key: String?, callback: (imageUri: Uri?)-> Unit) {
        Log.d("!!!itTask", "${adTemp}")
            dbManager.addPhotoToStorage(adTemp, key) { task ->
                if (task.isSuccessful) {
                    callback(task.result)
                    Log.d("!!!itTaskSuccessful2", "${task.result}")
                } else {
                    Log.d("!!!itTask", "${task}")
                }
            }
        }

    fun publishAdShelter(adTemp: AdShelter, dialog: AlertDialog, callback: (text: String) -> Unit) {
        dbManager.publishAdShelter(adTemp){
//            getAllAds()
            dialogMutableLiveData.value = dialog
            callback("task")
        }
    }

    fun getAllMarkersForMap(callback: (isFav: Boolean) -> Unit) {
        dbManager.getAllMarkersForMap(){
            if(it.isNotEmpty()) callback(true)
            else callback(false)
            liveAdsDataAllMarkers.value = it
        }
    }

    fun getAllAdsForAdapter(lat: Double, lng: Double, isMyMarkers: Boolean){
        dbManager.getAllAdsForAdapter(lat, lng, isMyMarkers){ listAdapter ->
            liveAdsDataForMapAdapter.value = listAdapter
        }
    }

    //одного фото запрос
    fun getOnePhoto(url: String) {
        scope.launch {
            _onePhoto.postValue(repository.getOnePhoto(url))

        }
    }

    //первый список с описанием пород
    fun showAllBreeds() {
        scope.launch {
            val data = repository.getAllBreeds()
            breedLive.postValue(data)
        }
    }

    fun     selectBreed() {
        scope.launch {
            val s = repository.getBreeds()
            val mapsBreed = s.message
            for (map in mapsBreed) {
                if (map.value.isEmpty()) {
                    selectBreed.add(map.key)
                } else {
                    for (byBreed in map.value) {
                        selectBreed.add("${map.key} ${byBreed}")
                    }
                }
            }
        }
    }

    fun saveFavoriteData(id: Long, isSelected: Int) {
        scope.launch {
            repository.saveFavoriteData(id, isSelected)
            countFavorites()
        }
    }
    //сохраняю заметку в БД
    fun saveNote(id: Long, note: String) {
        scope.launch {
            repository.saveNote(id, note)
        }
    }

    fun selectFavorites() {
        scope.launch {
            val data = repository.selectFavorites()
            breedLive.postValue(data)
        }
    }

    fun countFavorites() {
        scope.launch {
            val data = repository.selectFavorites()
            breedFavLive.postValue(data)
        }
    }

    fun getItemImg(breed: String) {
        scope.launch {
            val data = repository.getImgBreeds(breed)
            imgBreedList.clear()
            imgBreedList.add(selectedBreed?.image?.url.toString())
            imgBreedList.addAll(data.message)
            breedItemLive.postValue(imgBreedList)
        }
    }

    fun getItemImgDouble(breed: String, bybreed: String) {

        scope.launch {
            val data = repository.getImgBreedsDouble(breed, bybreed)
            imgBreedList.clear()
            imgBreedList.add(selectedBreed?.image?.url.toString())
            imgBreedList.addAll(data.message)
            breedItemLive.postValue(imgBreedList)
        }
    }

    fun searchView(breed: Array<String>, newText: String) {
        scope.launch {
            val data = repository.searchView(breed)
            if(!isFav) {
                breedLive.postValue(data)
            }else{
                val list = mutableListOf<DogBreeds>()
                data.forEach {
                    if(it.isFavorite == 1) list.add(it)
                }
                breedLive.postValue(list)
            }
        }
    }

    fun onFavClick(dog: AdShelter, callback: (isFav: Boolean) -> Unit) {
        dbManager.onFavClicked(dog){
            callback(it)
        }
    }


    interface WritedDataCallback {
        fun writedData()
    }
    companion object{
        const val VIEWS_COUNTER = 0
        const val CALLS_COUNTER = 1
    }
}