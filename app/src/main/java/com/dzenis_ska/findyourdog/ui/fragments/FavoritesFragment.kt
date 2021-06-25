package com.dzenis_ska.findyourdog.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dzenis_ska.findyourdog.R
import com.dzenis_ska.findyourdog.RemoteModel.DogBreeds
import com.dzenis_ska.findyourdog.ui.utils.AdapterBreeds
import com.dzenis_ska.findyourdog.ui.MainActivity
import com.dzenis_ska.findyourdog.viewModel.BreedViewModel
import kotlinx.android.synthetic.main.fragment_favorites.*
import java.util.*


class FavoritesFragment : Fragment() {
    lateinit var navController: NavController
    lateinit var viewModel: BreedViewModel
    private val breeds = mutableListOf<DogBreeds>()
    lateinit var adapter: AdapterBreeds

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//                //in here you can do logic when backPress is clicked
//                navController.popBackStack(R.id.addDogFragment, true)
//                navController.popBackStack(R.id.dogsListFragment, true)
//                navController.popBackStack(R.id.oneBreedFragment, true)
//                navController.popBackStack(R.id.favoritesFragment, true)
//                navController.popBackStack(R.id.editNoteFragment, true)
//            }
//        })
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(activity as MainActivity).get(BreedViewModel::class.java)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
        val context = getContext()

        viewModel.selectFavorites()

        viewModel.breedLive.observe(viewLifecycleOwner, Observer {
            breeds.clear()
            breeds.addAll(it)
            recyclerViewFav?.adapter?.notifyDataSetChanged()
            tv_dog_list_fav.alpha = 0.5f
        })

        adapter = AdapterBreeds(breeds, context, this)
        recyclerViewFav.adapter = adapter
        recyclerViewFav.layoutManager = LinearLayoutManager(activity)
        adapter.notifyDataSetChanged()

    }
    fun onBreedFavSelect(position:Int){
        viewModel.selectedBreed = breeds[position]
        viewModel.getItemImg(
            breeds[position].name?.toLowerCase(Locale.ROOT).toString()
        )
        navController.navigate(R.id.oneBreedFragment)
    }
}