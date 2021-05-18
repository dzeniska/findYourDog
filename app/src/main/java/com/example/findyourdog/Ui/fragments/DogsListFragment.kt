package com.example.findyourdog.Ui.fragments

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.view.View.inflate
import android.widget.SearchView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.content.res.ColorStateListInflaterCompat.inflate
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.findyourdog.R
import com.example.findyourdog.RemoteModel.DogBreeds
import com.example.findyourdog.Ui.AdapterBreeds
import com.example.findyourdog.Ui.MainActivity
import com.example.findyourdog.ViewModel.BreedViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_dogs_list.*
import java.util.*


class DogsListFragment : Fragment() {

    lateinit var navController: NavController
    lateinit var adapter: AdapterBreeds
    lateinit var viewModel: BreedViewModel
    private val breeds = mutableListOf<DogBreeds>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                //in here you can do logic when backPress is clicked
                navController.popBackStack()
            }
        })
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(activity as MainActivity).get(BreedViewModel::class.java)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dogs_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = findNavController()
        initSearchView()

        val context = getContext()

        viewModel.showAllBreeds()

        viewModel.breedLive.observe(viewLifecycleOwner, Observer {
            breeds.clear()
            breeds.addAll(it)
            recyclerView?.adapter?.notifyDataSetChanged()
            tv_dog_list.alpha = 0.5f
        })

        adapter = AdapterBreeds(breeds, context, this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(activity)
        adapter.notifyDataSetChanged()
    }


    fun onBreedSelect(position:Int){
        viewModel.selectedBreed = breeds[position]
        viewModel.getItemImg(breeds[position].name?.toLowerCase(Locale.ROOT).toString())
        navController.navigate(R.id.oneBreedFragment)
    }
    fun initSearchView(){
        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                Log.d("!!!", newText!!)
                var txt = ""
                txt = newText
                viewModel.searchView(arrayOf("%$txt%"))
                return true
            }
        })
    }


}
