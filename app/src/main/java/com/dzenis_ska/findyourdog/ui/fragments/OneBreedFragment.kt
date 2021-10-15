package com.dzenis_ska.findyourdog.ui.fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dzenis_ska.findyourdog.R
import com.dzenis_ska.findyourdog.databinding.FragmentOneBreedBinding
import com.dzenis_ska.findyourdog.remoteModel.DogBreeds
import com.dzenis_ska.findyourdog.ui.fragments.adapters.ImgAdapter
import com.dzenis_ska.findyourdog.ui.MainActivity
import com.dzenis_ska.findyourdog.viewModel.BreedViewModel

class OneBreedFragment : Fragment(), AdapterView.OnItemSelectedListener {

    lateinit var navController: NavController
    val viewModel: BreedViewModel by activityViewModels()
    var rootElement: FragmentOneBreedBinding? = null
    lateinit var breed: DogBreeds
    lateinit var textName: String
    lateinit var adapter: ImgAdapter
    var imgBreedList = mutableListOf<String>()
    val listBreed = arrayListOf<String>()
    private var dog = "Dog"
    var bool = true

    override fun onDetach() {
        viewModel.imgBreedList.clear()
        super.onDetach()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootElement = FragmentOneBreedBinding.inflate(inflater)
        return rootElement!!.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()

        //Spinner
        val adapterSpinner = ArrayAdapter(
            activity as MainActivity,
            android.R.layout.simple_spinner_item,
            listBreed
        )
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        rootElement!!.spinner.adapter = adapterSpinner
        rootElement!!.spinner.onItemSelectedListener = this
//        spinner.setPrompt("hgvhgv")
//        adapterSpinner.notifyDataSetChanged()
        badConnection()

        viewModel.breedItemLive.observe(viewLifecycleOwner,{
            Log.d("!!!observe", it.size.toString())
            if (it.size != 0) {
                imgBreedList.clear()
                imgBreedList.addAll(it)
                adapter.updateImgAdapter(it)
            }
//                badConnectionEnd()
        })
        adapter = ImgAdapter(viewModel.selectedBreed!!, this)
        rootElement!!.imgRecyclerView.adapter = adapter
        rootElement!!.imgRecyclerView.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)

        rootElement!!.apply {
            floatingActionButton.setOnClickListener() {
                if (breed.isFavorite == 0) {
                    viewModel.saveFavoriteData(breed.id, 1)
                    breed.isFavorite = 1
                    floatingActionButton.setImageResource(R.drawable.paw_red_2)
                } else {
                    viewModel.saveFavoriteData(breed.id, 0)
                    breed.isFavorite = 0
                    floatingActionButton.setImageResource(R.drawable.paw_blue)
                }
            }
            floatingActionButton2.setOnClickListener() {
                navController.navigate(R.id.editNoteFragment)
            }
//        }


            viewModel.selectedBreed?.let { post ->
                breed = post
                isFavNow(post)
                if (post.ind == 1) {
                    imageViewByteArray.visibility = View.VISIBLE
                    try {
                        imageViewByteArray.setImageURI(Uri.parse(post.image.url))
                    } catch (e: Exception) {
                        imageViewByteArray.setImageResource(R.drawable.drawer_back_6)
                    }
                }


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

                viewModel!!.selectBreed.let {
                    listBreed.clear()
//                listBreed.add(post.name?.toLowerCase(Locale.ROOT).toString())
                    listBreed.add("more photos")
                    listBreed.addAll(it)
                    adapterSpinner.notifyDataSetChanged()
                }
                tvBreedDescription.text = breedDescription
            }
        }
    }

    fun isFavNow(post: DogBreeds) = with(rootElement!!) {
        if (post.isFavorite == 0) {
            floatingActionButton.setImageResource(R.drawable.paw_blue)
        } else {
            floatingActionButton.setImageResource(R.drawable.paw_red_2)
        }
    }

    override fun onPause() {
        Log.d("!!!observe", "onPause")
        super.onPause()
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
        viewModel.isAddSF = false

//        onePhotoFrag = OnePhotoFragment()
//        val fm = activity?.supportFragmentManager?.beginTransaction()
//
//        fm?.replace(R.id.fragOneBreed, onePhotoFrag!!)
//
//        fm?.commit()
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

    private fun selectPhotoes(textBreed: String) {
        val yourArray: List<String> = textBreed.split(" ")
        if (yourArray.size == 2) {
            viewModel.getItemImgDouble(yourArray[0].trim(), yourArray[1].trim())
        } else {
            viewModel.getItemImg(textBreed)
        }
    }

    private fun badConnection() = with(rootElement!!){
        progressBarOneBreedFrag.visibility = View.VISIBLE
        progressBarOneBreedFrag.animation = AnimationUtils.loadAnimation(context, R.anim.alpha)

        imgNoConnection.visibility = View.VISIBLE
        imgNoConnection.animation = AnimationUtils.loadAnimation(context, R.anim.alpha)
    }
    fun badConnectionEnd() = with(rootElement!!){
        progressBarOneBreedFrag.visibility = View.GONE
        imgNoConnection.visibility = View.GONE
    }
}