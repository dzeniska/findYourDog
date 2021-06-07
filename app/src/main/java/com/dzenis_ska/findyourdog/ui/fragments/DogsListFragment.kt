package com.dzenis_ska.findyourdog.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dzenis_ska.findyourdog.R
import com.dzenis_ska.findyourdog.RemoteModel.DogBreeds
import com.dzenis_ska.findyourdog.ui.AdapterBreeds
import com.dzenis_ska.findyourdog.ui.MainActivity
import com.dzenis_ska.findyourdog.ViewModel.BreedViewModel
import kotlinx.android.synthetic.main.fragment_dogs_list.*
import kotlinx.coroutines.*
import java.util.*


class DogsListFragment : Fragment() {

    lateinit var navController: NavController
    lateinit var adapter: AdapterBreeds
    lateinit var viewModel: BreedViewModel
    private val breeds = mutableListOf<DogBreeds>()
    private var job: Job? = null
    private var job2: Job? = null


    //    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//                //in here you can do logic when backPress is clicked
//                navController.popBackStack()
//            }
//        })
//    }
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

        viewModel.showAllBreeds()
        navController = findNavController()
        initSearchView()

        initSwipeRefresh()

        val context = context


        viewModel.breedLive.observe(viewLifecycleOwner, Observer {
            Log.d("!!!observeDLF", it.size.toString())
            if(it.size == 0){
                (activity as MainActivity).checkNetwork()

            } else{
                breeds.clear()
                breeds.addAll(it)
                recyclerView?.adapter?.notifyDataSetChanged()
                tv_dog_list.alpha = 0.3f
            }

        })

        adapter = AdapterBreeds(breeds, context, this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(activity)
        adapter.notifyDataSetChanged()
    }

    private fun initSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            job = CoroutineScope(Dispatchers.Main).launch {
                viewModel.showAllBreeds()
                sleepScope()
                swipeRefreshLayout.isRefreshing = false
            }

        }


        swipeRefreshLayout.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )
    }

    private suspend fun sleepScope() = withContext(Dispatchers.IO) {
        delay(3000)
    }


    fun onBreedSelect(position:Int){
        job2 = CoroutineScope(Dispatchers.Main).launch {

            goToFrag(position)
//            sleepScope()
            navController.navigate(R.id.oneBreedFragment)

        }


    }

    private suspend fun goToFrag(position: Int) = withContext(Dispatchers.IO) {

            viewModel.selectedBreed = breeds[position]
            viewModel.getItemImg(breeds[position].name?.toLowerCase(Locale.ROOT).toString() )



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
