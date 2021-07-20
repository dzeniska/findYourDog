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
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dzenis_ska.findyourdog.R
import com.dzenis_ska.findyourdog.remoteModel.DogBreeds
import com.dzenis_ska.findyourdog.ui.fragments.adapters.ImgAdapter
import com.dzenis_ska.findyourdog.ui.MainActivity
import com.dzenis_ska.findyourdog.viewModel.BreedViewModel
import kotlinx.android.synthetic.main.fragment_one_breed.*


class OneBreedFragment : Fragment(), AdapterView.OnItemSelectedListener {

var onePhotoFrag: OnePhotoFragment? = null
    lateinit var navController: NavController
    var viewModel: BreedViewModel? = null
    lateinit var breed: DogBreeds
    lateinit var textName: String
    lateinit var adapter: ImgAdapter
    var imgBreedList = mutableListOf<String>()
    val listBreed = arrayListOf<String>()
    private var dog = "Dog"
    var bool = true


//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//                //in here you can do logic when backPress is clicked
////                viewModel.imgBreedList.clear()
//                navController.popBackStack()
//            }
//        })
//    }

    override fun onDetach() {
        viewModel?.imgBreedList?.clear()
        super.onDetach()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

            Log.d("!!!observe", "it.size.toString()")
            viewModel = ViewModelProvider(activity as MainActivity).get(BreedViewModel::class.java)

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_one_breed, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
        val context = context

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

        badConnection()

//        viewModel.getItemImg(viewModel.selectedBreed?.name?.toLowerCase(Locale.ROOT).toString())

            viewModel?.breedItemLive?.observe(viewLifecycleOwner, Observer {
                Log.d("!!!observe", it.size.toString())
                if (it.size != 0) {
                    imgBreedList.clear()
                    imgBreedList.addAll(it)
                    imgRecyclerView?.adapter?.notifyDataSetChanged()
                }
//                badConnectionEnd()
            })
        adapter = ImgAdapter(imgBreedList, viewModel?.selectedBreed!!, context, this)
        imgRecyclerView.adapter = adapter
        imgRecyclerView.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        adapter.notifyDataSetChanged()


        floatingActionButton.setOnClickListener() {
            if (breed.isFavorite == 0) {
                viewModel!!.saveFavoriteData(breed.id, 1)
                breed.isFavorite = 1
                floatingActionButton.setImageResource(R.drawable.paw_red_2)
            } else {
                viewModel!!.saveFavoriteData(breed.id, 0)
                breed.isFavorite = 0
                floatingActionButton.setImageResource(R.drawable.paw_blue)
            }
        }
        floatingActionButton2.setOnClickListener() {
            navController.navigate(R.id.editNoteFragment)
        }

        viewModel!!.selectedBreed?.let { post ->
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

    fun isFavNow(post: DogBreeds) {
        if (post.isFavorite == 0) {
            floatingActionButton.setImageResource(R.drawable.paw_blue)
        } else {
            floatingActionButton.setImageResource(R.drawable.paw_red_2)
        }
    }

    override fun onPause() {
//        imgRecyclerView.adapter?.notifyItemRangeRemoved(0, imgBreedList.size)
        Log.d("!!!observe", "onPause")
//        viewModel.breedItemLive.removeObserver(context as AppCompatActivity)
        super.onPause()

        bool = true
    }

    fun onePhoto(position: Int) {
        val resp = imgBreedList[position]
        viewModel?.getOnePhoto(resp)
        val photo = resp.substringAfterLast("/")
        viewModel?.fileName = photo
//        val breed = resp.substringBeforeLast("/").substringAfterLast("/")
        Log.d("!!!pq", photo)
        navController.navigate(R.id.onePhotoFragment)

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
            viewModel?.getItemImgDouble(yourArray[0].trim(), yourArray[1].trim())
        } else {
            viewModel?.getItemImg(textBreed)
        }
    }

    private fun badConnection(){
        progressBarOneBreedFrag.visibility = View.VISIBLE
        progressBarOneBreedFrag.animation = AnimationUtils.loadAnimation(context, R.anim.alpha)

        imgNoConnection.visibility = View.VISIBLE
        imgNoConnection.animation = AnimationUtils.loadAnimation(context, R.anim.alpha)
    }
    fun badConnectionEnd(){
        progressBarOneBreedFrag.visibility = View.GONE
        imgNoConnection.visibility = View.GONE
    }

    fun <T> MutableLiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        observe(lifecycleOwner, object : Observer<T> {
            override fun onChanged(t: T?) {
                observer.onChanged(t)
                if (t != null)
                    removeObserver(this)
            }
        })
    }

}

