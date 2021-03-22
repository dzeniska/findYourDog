package com.example.findyourdog.ViewModel


import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.findyourdog.RemoteModel.BreedOfDogListPhoto
import com.example.findyourdog.RemoteModel.DogBreeds
import com.example.findyourdog.RemoteModel.ImgBreed
import com.example.findyourdog.RemoteModel.RandomImageClass
import com.example.findyourdog.Repository.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import java.io.InputStream
import javax.inject.Inject


class BreedViewModel (val repository: Repository) : ViewModel() {
    val scope = CoroutineScope(Dispatchers.IO)

    var count: Long = 0
    var breedImgs: ImgBreed? = null
    val imgBreedList = mutableListOf<String>()
    val selectBreed = arrayListOf<String>()
    var responseBody: ResponseBody? = null
    var fileName:String = ""

    val selectBreedLive: MutableLiveData<MutableList<String>> by lazy {
        MutableLiveData<MutableList<String>>()
    }

    var selectedBreed: DogBreeds? = null

    val breedLive: MutableLiveData<MutableList<DogBreeds>> by lazy {
        MutableLiveData<MutableList<DogBreeds>>()
    }
    val breedFavLive: MutableLiveData<MutableList<DogBreeds>> by lazy {
        MutableLiveData<MutableList<DogBreeds>>()
    }
    val breedItemLive: MutableLiveData<MutableList<String>> by lazy {
        MutableLiveData<MutableList<String>>()
    }
    val onePhoto: MutableLiveData<ByteArray> by lazy {
        MutableLiveData<ByteArray>()
    }

    //одного фото запрос
    fun getOnePhoto(url: String){
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
            count = data.size.toLong()
        }
    }
    fun selectBreed() {
        scope.launch {
            val optionsList: Map<String, List<String>> = mapOf()
            var s =  BreedOfDogListPhoto(0, optionsList, "")
            s = repository.getBreeds()
            val mapsBreed = s.message
            for (map in mapsBreed) {
                if(map.value.size == 0){
                    selectBreed.add(map.key)
                }else{
                    for(byBreed in map.value){
                        selectBreed.add("${map.key} ${byBreed}")
                    }
                }
            }
        }
    }
    fun oneDog(dog: DogBreeds) {
        scope.launch {
            repository.insOneDog(dog)
            breedLive.value?.add(dog)
        }
    }
    fun saveFavoriteData(id: Long, isSelected: Int){
        scope.launch{
            repository.saveFavoriteData(id, isSelected)
        }
    }
    fun saveNote(id: Long, note: String){
        scope.launch{
             repository.saveNote(id, note)
        }
    }

    fun selectFavorites(){
        scope.launch {
            val data = repository.selectFavorites()
            breedLive.postValue(data)
        }
    }
    fun countFavorites(){
        scope.launch {
            val data = repository.selectFavorites()
            breedFavLive.postValue(data)
        }
    }

    fun getItemImg(breed: String){
        scope.launch {
            val data = repository.getImgBreeds(breed.toString())
            breedImgs = data
            imgBreedList.clear()
            imgBreedList.add(selectedBreed?.image?.url.toString())
            imgBreedList.addAll(data.message)
            breedItemLive.postValue(imgBreedList)
        }
    }
    fun getItemImgDouble(breed: String, bybreed: String){

        scope.launch {
            val data = repository.getImgBreedsDouble(breed, bybreed)
            breedImgs = data
            imgBreedList.clear()
            imgBreedList.add(selectedBreed?.image?.url.toString())
            imgBreedList.addAll(data.message)
            breedItemLive.postValue(imgBreedList)
        }
    }
    fun searchView(breed: Array<String>){
        scope.launch {
            val data = repository.searchView(breed)
            breedLive.postValue(data)
        }
    }
    fun insertOnePost(newBreeds: DogBreeds) {
        scope.launch {
            repository.insertOnePost(newBreeds)
            breedLive.value?.add(newBreeds)
        }
    }
}