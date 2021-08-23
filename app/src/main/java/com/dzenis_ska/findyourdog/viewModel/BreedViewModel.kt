package com.dzenis_ska.findyourdog.viewModel


import android.app.AlertDialog
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dzenis_ska.findyourdog.remoteModel.DogBreeds
import com.dzenis_ska.findyourdog.Repository.Repository
import com.dzenis_ska.findyourdog.remoteModel.firebase.AdShelter
import com.dzenis_ska.findyourdog.remoteModel.firebase.DbManager
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class BreedViewModel(val repository: Repository) : ViewModel() {



    private val scope = CoroutineScope(Dispatchers.IO)

    val  imgBreedList = mutableListOf<String>()
    val selectBreed = arrayListOf<String>()
    var fileName: String = ""
    var selectedBreed: DogBreeds? = null
    var locationManagerBool: Boolean = false
    var countSelectedPhoto: Int = 0
    var numPage: Int = 0
    var btnDelState: Boolean? = false


    //    val breedLive: MutableLiveData<MutableList<DogBreeds>> by lazy {
//        MutableLiveData<MutableList<DogBreeds>>()
//    }
    val breedLive = MutableLiveData<MutableList<DogBreeds>>()

//    val breedFavLive: MutableLiveData<MutableList<DogBreeds>> by lazy {
//        MutableLiveData<MutableList<DogBreeds>>()
//    }

    val breedFavLive = MutableLiveData<MutableList<DogBreeds>>()

    val breedItemLive: MutableLiveData<MutableList<String>> by lazy {
        MutableLiveData<MutableList<String>>()
    }

//    var breedItemLive = MutableLiveData<MutableList<String>>()

    val onePhoto: MutableLiveData<ByteArray> by lazy {
        MutableLiveData<ByteArray>()
    }
    var userUpdate = MutableLiveData<FirebaseUser?>()

    var signUpInValue = MutableLiveData<Int>()

    val dbManager = DbManager()
    var listShelter = ArrayList<AdShelter>()
    var shelter: AdShelter? = null
    val liveAdsDataAllShelter = MutableLiveData<ArrayList<AdShelter>>()
    val liveAdsDataAddShelter = MutableLiveData<AdShelter?>()
    val listPhoto = arrayListOf<String>()
    val dialog = MutableLiveData<AlertDialog>()

    fun listPhoto(listP: ArrayList<String>) {
        listPhoto.clear()
        listPhoto.addAll(listP)
    }

    fun uiUpdateMain(user: FirebaseUser?) {
        userUpdate.postValue(user)
    }

    fun openFragShelter(adShelter: AdShelter?){
        liveAdsDataAddShelter.value = adShelter
        adShelter?.let { adViewed(it) }
    }

    fun deleteAdShelter(adShelter: AdShelter?, writedDataCallback: WritedDataCallback){
        dbManager.deleteAdShelter(adShelter, object : DbManager.FinishWorkListener{
            override fun onFinish() {
                val updateList = liveAdsDataAllShelter.value
                updateList?.remove(adShelter)
                liveAdsDataAllShelter.postValue(updateList!!)
                writedDataCallback.writedData()
            }

        })
    }

    private fun adViewed(adShelter: AdShelter){
        dbManager.adViewed(adShelter, object : DbManager.FinishWorkListener{
            override fun onFinish() {
                val updatedList = liveAdsDataAllShelter.value
                val pos = updatedList?.indexOf(adShelter)
                Log.d("!!!views", "$pos")
                if(pos != -1) {
                    pos?.let{
                        val viewCounter = adShelter.viewsCounter.toInt() + 1
                        updatedList[pos] = updatedList[pos].copy(viewsCounter = viewCounter.toString())
                    }
                }
                liveAdsDataAllShelter.value = updatedList!!
            }
        })
    }
    fun deletePhoto(uri: String, callback:()-> Unit ){
        dbManager.deletePhoto(uri){
            callback()
        }
    }
    fun publishPhoto(adTemp: ByteArray, callback: (imageUri: Uri?)-> Unit) {
            dbManager.addPhotoToStorage(adTemp) { task ->
                if (task.isSuccessful) {
                    callback(task.result)
                    Log.d("!!!itTaskSuccessful2", "${task.result}")
                } else {
                    Log.d("!!!itTask", "${task}")
                }
            }
        }

    fun publishAdShelter(adTemp: AdShelter, dialogq: AlertDialog, callback: (text: String) -> Unit) {
        dbManager.publishAdShelter(adTemp){
//            getAllAds()
            dialog.value = dialogq
            callback("task")
        }
    }

    fun getAllAds(){
        //Вызывается два раза
        Log.d("!!!publishAdShelterDB", "qwerty")
        dbManager.getAllAds(object: DbManager.ReadDataCallback{
            override fun readData(list: ArrayList<AdShelter>) {
                liveAdsDataAllShelter.value = list
                listShelter.clear()
                listShelter.addAll(list)
            }
        })
    }

    //одного фото запрос
    fun getOnePhoto(url: String) {
        scope.launch {
//            repository.getOnePhoto(url)
            onePhoto.postValue(repository.getOnePhoto(url))
        }
    }

    //первый список с описанием пород
    fun showAllBreeds() {
        scope.launch {
            val data = repository.getAllBreeds()
            breedLive.postValue(data)
        }
    }

    fun selectBreed() {
        scope.launch {
//            val optionsList: Map<String, List<String>> = mapOf()
//            var s =  BreedOfDogListPhoto(0, optionsList, "")
            val s = repository.getBreeds()
            val mapsBreed = s.message
            for (map in mapsBreed) {
                if (map.value.size == 0) {
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
//            val ibl = mutableListOf<String>()
//                ibl.add(selectedBreed?.image?.url.toString())
            imgBreedList.add(selectedBreed?.image?.url.toString())
//            ibl.addAll(data.message)
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

    fun searchView(breed: Array<String>) {
        scope.launch {
            val data = repository.searchView(breed)
            breedLive.postValue(data)
        }
    }

    interface WritedDataCallback {
        fun writedData()
    }
}