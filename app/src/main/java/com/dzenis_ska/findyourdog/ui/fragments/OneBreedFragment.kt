package com.dzenis_ska.findyourdog.ui.fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dzenis_ska.findyourdog.R
import com.dzenis_ska.findyourdog.databinding.FragmentOneBreedBinding
import com.dzenis_ska.findyourdog.remoteModel.DogBreeds
import com.dzenis_ska.findyourdog.ui.fragments.adapters.ImgAdapter
import com.dzenis_ska.findyourdog.ui.MainActivity
import com.dzenis_ska.findyourdog.ui.fragments.adapters.SpinnerAdapterMorePhoto
import com.dzenis_ska.findyourdog.viewModel.BreedViewModel

class OneBreedFragment : Fragment()/*, AdapterView.OnItemSelectedListener*/ {

    lateinit var navController: NavController
    val viewModel: BreedViewModel by activityViewModels()
    var binding: FragmentOneBreedBinding? = null
    lateinit var breed: DogBreeds
    lateinit var textName: String
    lateinit var adapter: ImgAdapter
    var spinnerAdapter: SpinnerAdapterMorePhoto? = null

    var imgBreedList = mutableListOf<String>()
    private val listBreed = arrayListOf<String>()
    var bool = true
    var breedName = ""

    override fun onDetach() {
        viewModel.imgBreedList.clear()
        super.onDetach()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOneBreedBinding.inflate(inflater)
        return binding!!.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()

        badConnection()

        viewModel.breedItemLive.observe(viewLifecycleOwner, {
            Log.d("!!!observe", it.size.toString())
            if (it.size != 0) {
                imgBreedList.clear()
                imgBreedList.addAll(it)
                adapter.updateImgAdapter(it)
            }
        })
        adapter = ImgAdapter(viewModel.selectedBreed!!, this)
        binding!!.imgRecyclerView.adapter = adapter
        binding!!.imgRecyclerView.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)

        binding!!.apply {
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


                val origin = if (post.origin.isNullOrBlank()) "" else
                    "${res(R.string.origin)} ${post.origin}"

                val description = if (post.description.isNullOrBlank()) "" else
                    "${res(R.string.description)} ${post.description}"


                val note = if (post.note == null) "" else
                    "${res(R.string.note)} ${post.note}"

                val breedDescription = """
                |${note}
                |   
                |${res(R.string.temperament)} ${post.temperament}
                |
                |${res(R.string.weight)} ${post.weight.metric}${res(R.string.kg)} 
                |${res(R.string.height)} ${post.height.metric}${res(R.string.sm)}
                |
                |${origin}
                |
                |${description}
                |
                |""".trimMargin()

                breedName = post.name.toString()

                tvBreedDog.text = breedName
                spinner.prompt = breedName

                textName = post.name.toString()
                if (!textName.contains(" ")) {
                    imgCut.visibility = View.GONE
                }

                viewModel.selectBreed.let {
                    listBreed.clear()
                    listBreed.add(res(R.string.more_photos))
                    listBreed.addAll(it)
                    initSpinner()
                }
                tvBreedDescription.text = breedDescription
            }
        }
    }

    private fun initSpinner() = with(binding!!) {
        //Spinner
        spinnerAdapter = SpinnerAdapterMorePhoto(listBreed, breedName)
//        {
//            Toast.makeText(
//                activity as MainActivity,
//                it,
//                Toast.LENGTH_SHORT
//            ).show()
//        }
        spinner.adapter = spinnerAdapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (!bool) {
                    Log.d("!!!spinner", "!!!")
                    if (parent != null) {
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
            override fun onNothingSelected(parent: AdapterView<*>?) { Log.d("!!!onNothingSelected", "onNothingSelected")            }
        }
    }

    private fun isFavNow(post: DogBreeds) = with(binding!!) {
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

    }

    private fun selectPhotoes(textBreed: String) {
        val yourArray: List<String> = textBreed.split(" ")
        if (yourArray.size == 2) {
            viewModel.getItemImgDouble(yourArray[0].trim(), yourArray[1].trim())
        } else {
            viewModel.getItemImg(textBreed)
        }
    }

    private fun badConnection() = with(binding!!) {
        progressBarOneBreedFrag.visibility = View.VISIBLE
        progressBarOneBreedFrag.animation = AnimationUtils.loadAnimation(context, R.anim.alpha)

        imgNoConnection.visibility = View.VISIBLE
        imgNoConnection.animation = AnimationUtils.loadAnimation(context, R.anim.alpha)
    }

    fun badConnectionEnd() = with(binding!!) {
        progressBarOneBreedFrag.visibility = View.GONE
        imgNoConnection.visibility = View.GONE
    }
}