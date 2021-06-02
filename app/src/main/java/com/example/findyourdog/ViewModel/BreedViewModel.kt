package com.example.findyourdog.ViewModel


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.findyourdog.RemoteModel.BreedOfDogListPhoto
import com.example.findyourdog.RemoteModel.DogBreeds
import com.example.findyourdog.RemoteModel.ImgBreed
import com.example.findyourdog.Repository.Repository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class BreedViewModel(val repository: Repository) : ViewModel() {

    private val scope = CoroutineScope(Dispatchers.IO)

    var breedImgs: ImgBreed? = null
    val imgBreedList = mutableListOf<String>()
    val selectBreed = arrayListOf<String>()
    var fileName: String = ""
    var selectedBreed: DogBreeds? = null


    //    val breedLive: MutableLiveData<MutableList<DogBreeds>> by lazy {
//        MutableLiveData<MutableList<DogBreeds>>()
//    }
    val breedLive = MutableLiveData<MutableList<DogBreeds>>()

//    val breedFavLive: MutableLiveData<MutableList<DogBreeds>> by lazy {
//        MutableLiveData<MutableList<DogBreeds>>()
//    }

    val breedFavLive = MutableLiveData<MutableList<DogBreeds>>()

//    val breedItemLive: MutableLiveData<MutableList<String>> by lazy {
//        MutableLiveData<MutableList<String>>()
//    }

    var breedItemLive = MutableLiveData<MutableList<String>>()

    val onePhoto: MutableLiveData<ByteArray> by lazy {
        MutableLiveData<ByteArray>()
    }
    var userUpdate = MutableLiveData<FirebaseUser>()

    var signUpInValue = MutableLiveData<Int>()

    //одного фото запрос
    fun getOnePhoto(url: String) {
        scope.launch {
            repository.getOnePhoto(url)
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

    //    fun oneDog(dog: DogBreeds) {
//        scope.launch {
//            repository.insOneDog(dog)
//            breedLive.value?.add(dog)
//        }
//    }
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
            breedImgs = data
            imgBreedList.clear()
            imgBreedList.add(selectedBreed?.image?.url.toString())
            imgBreedList.addAll(data.message)

            breedItemLive.postValue(imgBreedList)
        }
    }

    fun getItemImgDouble(breed: String, bybreed: String) {

        scope.launch {
            val data = repository.getImgBreedsDouble(breed, bybreed)
            breedImgs = data
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
//    fun insertOnePost(newBreeds: DogBreeds) {
//        scope.launch {
//            repository.insertOnePost(newBreeds)
//            breedLive.value?.add(newBreeds)
//        }
//    }

    fun uiUpdateMain(user: FirebaseUser?) {
        userUpdate.postValue(user!!)
    }

    fun signUpIn(signUpIn: Int) {
        signUpInValue.postValue(signUpIn)
    }
}