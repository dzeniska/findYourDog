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


class BreedViewModel(val repository: Repository) : ViewModel() {
    val scope = CoroutineScope(Dispatchers.IO)

    var onePhoto: String = ""
    var count: Long = 0
    var breedImgs: ImgBreed? = null
    val imgBreedList = mutableListOf<String>()


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
    val breedsLive: MutableLiveData<MutableList<BreedOfDogListPhoto>> by lazy {
        MutableLiveData<MutableList<BreedOfDogListPhoto>>()
    }



    fun showAllBreeds() {
        scope.launch {
            val data = repository.getAllBreeds()
            breedLive.postValue(data)
            count = data.size.toLong()
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
            val data = repository.getImgBreeds(breed)
            //Log.d("!!!RM", data.toString())
            breedImgs = data
            imgBreedList.clear()
            imgBreedList.add(selectedBreed?.image?.url.toString())
            imgBreedList.addAll(data.message)
            Log.d("!!!RM", imgBreedList.size.toString())
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
    fun breedList() {
        scope.launch {
            val allBreeds = repository.getBreeds()
            Log.d("!!!jopa", allBreeds.toString())
            //breedsLive.value?.addAll(allBreeds)
        }
    }




}