package com.example.findyourdog.Ui.fragments

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.marginStart
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.findyourdog.R
import com.example.findyourdog.RemoteModel.DogBreeds
import com.example.findyourdog.RemoteModel.ImgBreed
import com.example.findyourdog.Ui.AdapterBreeds
import com.example.findyourdog.Ui.ImgAdapter
import com.example.findyourdog.Ui.MainActivity
import com.example.findyourdog.ViewModel.BreedViewModel
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_dogs_list.*
import kotlinx.android.synthetic.main.fragment_one_breed.*
import java.util.*


class OneBreedFragment : Fragment() {

    lateinit var navController: NavController
    lateinit var viewModel: BreedViewModel
    lateinit var breed: DogBreeds
    lateinit var textName: String
    lateinit var adapter: ImgAdapter
    var imgBreedList = mutableListOf<String>()
    val imgBreed = mutableListOf<String>()
    var counter = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(activity as MainActivity).get(BreedViewModel::class.java)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_one_breed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()

        val context = getContext()

        viewModel.breedItemLive.observe(viewLifecycleOwner, Observer {
            imgBreedList.clear()
            imgBreedList.addAll(it)
            Log.d("!!!dlf", imgBreedList.toString())
            Log.d("!!!dlf", imgBreedList.size.toString())
            imgRecyclerView?.adapter?.notifyDataSetChanged()
        })
        viewModel.breedsLive.observe(viewLifecycleOwner, Observer {
            Log.d("!!!breedLive", it.toString())
        })
        adapter = ImgAdapter(imgBreedList, viewModel.selectedBreed!!, context, this)
        imgRecyclerView.adapter = adapter
        imgRecyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        adapter.notifyDataSetChanged()

        floatingActionButton.setOnClickListener(){
            if(breed.isFavorite == 0){
                viewModel.saveFavoriteData(breed.id, 1)
                breed.isFavorite = 1
                floatingActionButton.setImageResource(R.drawable.add_fav)
            }else{
                viewModel.saveFavoriteData(breed.id, 0)
                breed.isFavorite = 0
                floatingActionButton.setImageResource(R.drawable.add_fav_null)
            }
        }
        floatingActionButton2.setOnClickListener(){
            navController.navigate(R.id.editNoteFragment)
        }



        viewModel.imgBreedList?.let {
            Log.d("!!!OBF", it.toString())
        }

        viewModel.selectedBreed?.let{post ->
            breed = post
            isFavNow(post)
            if(post.ind == 1){
                imageViewByteArray.visibility = View.VISIBLE
                imageViewByteArray.setImageURI(Uri.parse(post.image.url))
            }
            imgCut.setOnClickListener(){
                Log.d("!!!OBF", "it.toString()")
                changePhoto()
            }
//            if(post.photo != null){
//                imageViewByteArray.visibility = View.VISIBLE
//                val bmpArray = post.photo
//                val bmp = BitmapFactory.decodeByteArray(bmpArray, 0, bmpArray.size)
//                imageViewByteArray.setImageBitmap(bmp)
//            }
            var origin = "origin: ${post.origin}"
            if(post.origin == null) {
                origin = ""
            }
            var description = "description: ${post.description}"
            if(post.description == null) {
                description = ""
            }
            var note = "note: ${post.note}"
            if(post.note == null) {
                note = ""
            }
            val breedDescription = """
                |${note}
                |${description}
                |temperament: ${post.temperament}
                |weight: ${post.weight.metric} 
                |height: ${post.height.metric}
                |${origin}
                |
                |""".trimMargin()
            tvBreedDog.text = post.name
            textName = post.name.toString()
            if(!textName.contains(" ")){
                imgCut.visibility = View.GONE
//                tvBreedDog.marginStart.minus(30)
            }
            tvBreedDescription.text = breedDescription
        }

    }

    fun isFavNow(post: DogBreeds){
        if(post.isFavorite == 0){
            floatingActionButton.setImageResource(R.drawable.add_fav_null)
        }else{
            floatingActionButton.setImageResource(R.drawable.add_fav)
        }
    }
    override fun onPause() {
        super.onPause()
        viewModel.countFavorites()
    }

    fun onePhoto(position: Int){
        viewModel.onePhoto = imgBreedList[position]
        Log.d("!!!", viewModel.onePhoto)
        navController.navigate(R.id.onePhotoFragment)
    }


    fun changePhoto() {
        val yourArray: List<String> = textName.split(" ")
        val trim = textName.toString()//African Hunting Dog
        var s: String
        Log.d("!!!trim", yourArray.size.toString())
        if (yourArray.size == 2) {
            when (counter) {
                0 -> {
                    viewModel.getItemImg(trim.toLowerCase(Locale.ROOT).toString())
                    Toast.makeText(context, "Просмотр ${trim}", Toast.LENGTH_LONG).show()
                    counter++
                }
                1 -> {
                    viewModel.getItemImg(yourArray[0].toLowerCase(Locale.ROOT).toString())
                    Toast.makeText(context, "Просмотр ${yourArray[0]}", Toast.LENGTH_LONG).show()
                    counter++
                }
                2 -> {
                    viewModel.getItemImg(yourArray[1].toLowerCase(Locale.ROOT).toString())
                    Toast.makeText(context, "Просмотр ${yourArray[1]}", Toast.LENGTH_LONG).show()
                    counter++
                }
                3 -> {
                    s = "${yourArray[1]} ${yourArray[0]}"
                    viewModel.getItemImg(s.toLowerCase(Locale.ROOT).toString())
                    Toast.makeText(context, "Просмотр ${s}", Toast.LENGTH_LONG).show()
                    counter = 0
                }
            }
        } else if (yourArray.size == 3) {
            when (counter) {
                0 -> {
                    viewModel.getItemImg(trim.toLowerCase(Locale.ROOT).toString())
                    Toast.makeText(context, "Просмотр ${trim}", Toast.LENGTH_LONG).show()
                    counter++
                }
                1 -> {
                    viewModel.getItemImg(yourArray[0].toLowerCase(Locale.ROOT).toString())
                    Toast.makeText(context, "Просмотр ${yourArray[0]}", Toast.LENGTH_LONG).show()
                    counter++
                }
                2 -> {
                    viewModel.getItemImg(yourArray[1].toLowerCase(Locale.ROOT).toString())
                    Toast.makeText(context, "Просмотр ${yourArray[1]}", Toast.LENGTH_LONG).show()
                    counter++
                }
                3 -> {
                    s = yourArray[2]
                    if (s.contains('(')) {
                        s = s.substringAfter("(")
                        s = s.substringBefore(")")
                    }
                    viewModel.getItemImg(s.toLowerCase(Locale.ROOT).toString())
                    Toast.makeText(context, "Просмотр ${s}", Toast.LENGTH_LONG).show()
                    counter = 0
                }
            }
        } else if (yourArray.size == 4) {
            when (counter) {
                0 -> {
                    viewModel.getItemImg(trim.toLowerCase(Locale.ROOT).toString())
                    Toast.makeText(context, "Просмотр ${trim}", Toast.LENGTH_LONG).show()
                    counter++
                }
                1 -> {
                    viewModel.getItemImg(yourArray[0].toLowerCase(Locale.ROOT).toString())
                    Toast.makeText(context, "Просмотр ${yourArray[0]}", Toast.LENGTH_LONG).show()
                    counter++
                }
                2 -> {
                    viewModel.getItemImg(yourArray[1].toLowerCase(Locale.ROOT).toString())
                    Toast.makeText(context, "Просмотр ${yourArray[1]}", Toast.LENGTH_LONG).show()
                    counter++
                }
                3 -> {
                    s = yourArray[2]
                    if (s.contains('(')) {
                        s = s.substringAfter("(")
                        s = s.substringBefore(")")
                    }
                    viewModel.getItemImg(s.toLowerCase(Locale.ROOT).toString())
                    Toast.makeText(context, "Просмотр ${s}", Toast.LENGTH_LONG).show()
                    counter++
                }
                4 -> {
                    s = yourArray[3]
                    if (s.contains('(')) {
                        s = s.substringAfter("(")
                        s = s.substringBefore(")")
                    }
                    viewModel.getItemImg(s.toLowerCase(Locale.ROOT).toString())
                    Toast.makeText(context, "Просмотр ${s}", Toast.LENGTH_LONG).show()
                    counter = 0
                }
            }
        } else if (yourArray.size == 5) {
            when (counter) {
                0 -> {
                    viewModel.getItemImg(trim.toLowerCase(Locale.ROOT).toString())
                    Toast.makeText(context, "Просмотр ${trim}", Toast.LENGTH_LONG).show()
                    counter++
                }
                1 -> {
                    viewModel.getItemImg(yourArray[0].toLowerCase(Locale.ROOT).toString())
                    Toast.makeText(context, "Просмотр ${yourArray[0]}", Toast.LENGTH_LONG).show()
                    counter++
                }
                2 -> {
                    viewModel.getItemImg(yourArray[1].toLowerCase(Locale.ROOT).toString())
                    Toast.makeText(context, "Просмотр ${yourArray[1]}", Toast.LENGTH_LONG).show()
                    counter++
                }
                3 -> {
                    s = yourArray[2]
                    if (s.contains('(')) {
                        s = s.substringAfter("(")
                        s = s.substringBefore(")")
                    }
                    viewModel.getItemImg(s.toLowerCase(Locale.ROOT).toString())
                    Toast.makeText(context, "Просмотр ${s}", Toast.LENGTH_LONG).show()
                    counter++
                }
                4 -> {
                    s = yourArray[3]
                    if (s.contains('(')) {
                        s = s.substringAfter("(")
                        s = s.substringBefore(")")
                    }
                    viewModel.getItemImg(s.toLowerCase(Locale.ROOT).toString())
                    Toast.makeText(context, "Просмотр ${s}", Toast.LENGTH_LONG).show()
                    counter++
                }
                5 -> {
                    s = yourArray[4]
                    if (s.contains('(')) {
                        s = s.substringAfter("(")
                        s = s.substringBefore(")")
                    }
                    viewModel.getItemImg(s.toLowerCase(Locale.ROOT).toString())
                    Toast.makeText(context, "Просмотр ${s}", Toast.LENGTH_LONG).show()
                    counter = 0
                }
            }
        }
    }
//
//
//
////        val trim0 = tvBreedDog.text.toString()//African Hunting Dog
////        val trim1 = trim0.substringBefore(" ")//African
////        val trim4 = trim0.substringBeforeLast(" ")//African Hunting
////        val trim3 = trim4.substringAfter(" ")//Hunting
////        val trim5 = trim0.substringAfter(" ")//Hunting Dog
////        val trim2 = trim0.substringAfterLast(" ")//Dog
////        when(counter){
////            0 -> {
////                viewModel.getItemImg(trim0.toLowerCase(Locale.ROOT).toString())
////                Toast.makeText(context, "Просмотр $trim0", Toast.LENGTH_LONG).show()
////            }
////            1 -> {
////                viewModel.getItemImg(trim1.toLowerCase(Locale.ROOT).toString())
////                Toast.makeText(context, "Просмотр $trim1", Toast.LENGTH_LONG).show()
////            }
////            2 -> {
////                viewModel.getItemImg(trim2.toLowerCase(Locale.ROOT).toString())
////                Toast.makeText(context, "Просмотр $trim2", Toast.LENGTH_LONG).show()
////            }
////            3 -> {
////                viewModel.getItemImg(trim3.toLowerCase(Locale.ROOT).toString())
////                Toast.makeText(context, "Просмотр $trim3", Toast.LENGTH_LONG).show()
////            }
////            4 -> {
////                viewModel.getItemImg(trim4.toLowerCase(Locale.ROOT).toString())
////                Toast.makeText(context, "Просмотр $trim4", Toast.LENGTH_LONG).show()
////            }
////            5 -> {
////                viewModel.getItemImg(trim5.toLowerCase(Locale.ROOT).toString())
////                Toast.makeText(context, "Просмотр $trim5", Toast.LENGTH_LONG).show()
////            }
////        }
////        Log.d("!!!trim", counter.toString())
////        counter++
////        if(counter == 5) counter = 0
//
//
//    }

}
