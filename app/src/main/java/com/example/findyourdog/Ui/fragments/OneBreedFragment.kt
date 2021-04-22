package com.example.findyourdog.Ui.fragments

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.findyourdog.R
import com.example.findyourdog.RemoteModel.DogBreeds
import com.example.findyourdog.Ui.ImgAdapter
import com.example.findyourdog.Ui.MainActivity
import com.example.findyourdog.ViewModel.BreedViewModel
import kotlinx.android.synthetic.main.fragment_one_breed.*
import java.util.*


class OneBreedFragment : Fragment(), AdapterView.OnItemSelectedListener {

    lateinit var navController: NavController
    lateinit var viewModel: BreedViewModel
    lateinit var breed: DogBreeds
    lateinit var textName: String
    lateinit var adapter: ImgAdapter
    var imgBreedList = mutableListOf<String>()
    val listBreed = arrayListOf<String>()
    var counter = 1
    private var dog = "Dog"
    var bool = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                //in here you can do logic when backPress is clicked
//                viewModel.imgBreedList.clear()
                navController.popBackStack()
            }
        })
    }

    override fun onDetach() {
        viewModel.imgBreedList.clear()
        super.onDetach()
    }

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

        //Spinner
        val adapterSpinner = ArrayAdapter(
            activity as MainActivity,
            android.R.layout.simple_spinner_item,
            listBreed
        )
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapterSpinner
        spinner.onItemSelectedListener = this
//        spinner.setPrompt("hgvhgv")
//        adapterSpinner.notifyDataSetChanged()


        adapter = ImgAdapter(imgBreedList, viewModel.selectedBreed!!, context, this)
        imgRecyclerView.adapter = adapter
        imgRecyclerView.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        //adapter.notifyDataSetChanged()


        viewModel.breedItemLive.observe(viewLifecycleOwner, Observer {
            Log.d("!!!observe", it.size.toString())
            imgBreedList.clear()
            imgBreedList.addAll(it)
            imgRecyclerView?.adapter?.notifyDataSetChanged()

        })

        viewModel.imgBreedList?.let {
        }

        floatingActionButton.setOnClickListener() {
            if (breed.isFavorite == 0) {
                viewModel.saveFavoriteData(breed.id, 1)
                breed.isFavorite = 1
                floatingActionButton.setImageResource(R.drawable.add_fav)
            } else {
                viewModel.saveFavoriteData(breed.id, 0)
                breed.isFavorite = 0
                floatingActionButton.setImageResource(R.drawable.add_fav_null)
            }
        }
        floatingActionButton2.setOnClickListener() {
            navController.navigate(R.id.editNoteFragment)
        }


        imgCut.setOnClickListener() {
            changePhoto()
        }

        viewModel.selectedBreed?.let { post ->
            breed = post
            isFavNow(post)
            if (post.ind == 1) {
                imageViewByteArray.visibility = View.VISIBLE
                try {
                    imageViewByteArray.setImageURI(Uri.parse(post.image.url))
                } catch (e: Exception) {
                    imageViewByteArray.setImageResource(R.drawable.drawer_back_5)
                }

            }

//            if(post.photo != null){
//                imageViewByteArray.visibility = View.VISIBLE
//                val bmpArray = post.photo
//                val bmp = BitmapFactory.decodeByteArray(bmpArray, 0, bmpArray.size)
//                imageViewByteArray.setImageBitmap(bmp)
//            }
            var origin = "origin: ${post.origin}"
            if (post.origin == null) {
                origin = ""
            }
            var description = "description: ${post.description}"
            if (post.description == null) {
                description = ""
            }
            var note = "note: ${post.note}"
            if (post.note == null) {
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
            spinner.setPrompt(post.name)
            textName = post.name.toString()
            if (!textName.contains(" ")) {
                imgCut.visibility = View.GONE
//                tvBreedDog.marginStart.minus(30)
            }

            viewModel.selectBreed.let {
                listBreed.clear()
//                listBreed.add(post.name?.toLowerCase(Locale.ROOT).toString())
                listBreed.add("select photos")
                listBreed.addAll(it)
                adapterSpinner.notifyDataSetChanged()
            }
            tvBreedDescription.text = breedDescription
        }
    }

    fun isFavNow(post: DogBreeds) {
        if (post.isFavorite == 0) {
            floatingActionButton.setImageResource(R.drawable.add_fav_null)
        } else {
            floatingActionButton.setImageResource(R.drawable.add_fav)
        }
    }

    override fun onPause() {
        imgRecyclerView.adapter?.notifyItemRangeRemoved(0, imgBreedList.size)
        super.onPause()
        viewModel.countFavorites()
        bool = true
    }

    fun onePhoto(position: Int) {
        val resp = imgBreedList[position]
        viewModel.getOnePhoto(resp)
        val photo = resp.substringAfterLast("/")
        viewModel.fileName = photo
//        val breed = resp.substringBeforeLast("/").substringAfterLast("/")
        Log.d("!!!pq", photo)
        navController.navigate(R.id.onePhotoFragment)
    }


    fun changePhoto() {
        val yourArray: List<String> = textName.split(" ")
        val trim = textName.toString()//African Hunting Dog
        var s: String
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


    override fun onNothingSelected(parent: AdapterView<*>?) {
//        dog = "Dog"
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//        (parent!!.getChildAt(0) as TextView).setTextColor(Color.GRAY)
//        (parent!!.getChildAt(0) as TextView).textSize = 14f
        if (!bool) {
            Log.d("!!!spinner", "!!!")
            if (parent != null) {
                dog = parent.getItemAtPosition(position).toString()
                Toast.makeText(
                    activity as MainActivity,
                    listBreed[position],
                    Toast.LENGTH_SHORT
                ).show()
                selectPhotoes(listBreed[position])
            }
        } else {
            bool = false
        }


    }


    fun selectPhotoes(textBreed: String) {
        val yourArray: List<String> = textBreed.split(" ")
        if (yourArray.size == 2) {
            viewModel.getItemImgDouble(yourArray[0].trim(), yourArray[1].trim())
        } else {
            viewModel.getItemImg(textBreed)
        }
    }


}
