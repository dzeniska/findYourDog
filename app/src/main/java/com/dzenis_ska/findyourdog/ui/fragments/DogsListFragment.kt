package com.dzenis_ska.findyourdog.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.SearchView
import androidx.core.view.size
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dzenis_ska.findyourdog.R
import com.dzenis_ska.findyourdog.databinding.FragmentDogsListBinding
import com.dzenis_ska.findyourdog.remoteModel.DogBreeds
import com.dzenis_ska.findyourdog.ui.fragments.adapters.AdapterBreeds
import com.dzenis_ska.findyourdog.ui.MainActivity
import com.dzenis_ska.findyourdog.ui.utils.CheckNetwork
import com.dzenis_ska.findyourdog.ui.utils.InitBackStack
import com.dzenis_ska.findyourdog.viewModel.BreedViewModel
import kotlinx.coroutines.*
import java.util.*

class DogsListFragment : Fragment() {
    var rootElement: FragmentDogsListBinding? = null
    val viewModel: BreedViewModel by activityViewModels()
    var navController: NavController? = null
    var adapter: AdapterBreeds? = null
    private val breeds = mutableListOf<DogBreeds>()
    private var job: Job? = null

    //    private val check: CheckNetwork = CheckNetwork()
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
    ): View {
        rootElement = FragmentDogsListBinding.inflate(inflater)
        return rootElement!!.root
    }

    @SuppressLint("StringFormatInvalid")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()
        isFavRecycler(viewModel.isFav)
        navController = findNavController()
        initSearchView()
        initSwipeRefresh()
        initClick()
        initBackStack()
//        rootElement!!.tvDogList.text = getString(R.string.hi, "${getCounterValue()}")


        viewModel.breedLive.observe(viewLifecycleOwner, {
            if (it.size == 0) CheckNetwork.check(activity as MainActivity)
            breeds.clear()
            breeds.addAll(it)
            adapter?.updateAdapterBreeds(it)
            rootElement!!.tvDogList.alpha = 0.3f
        })
    }

    private fun initAdapter() {
        adapter = AdapterBreeds(context, this)
        rootElement!!.apply {
            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(activity)
        }
    }

    private fun isFavRecycler(boolean: Boolean) = with(rootElement!!) {
        if (boolean) {
            viewModel.selectFavorites()
            fabLike.setImageResource(R.drawable.paw_red_2)
        } else {
            viewModel.showAllBreeds()
            fabLike.setImageResource(R.drawable.paw_blue)
        }
    }

    private fun initSwipeRefresh() = with(rootElement!!) {
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

    private fun initSearchView() {
        rootElement!!.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                viewModel.searchView(arrayOf("%$newText%"), newText)
                return true
            }
        })
    }

    private fun initClick() = with(rootElement!!) {
        fabLike.setOnClickListener() {
            viewModel.isFav = !viewModel.isFav
            isFavRecycler(viewModel.isFav)
        }
    }

    private suspend fun sleepScope() = withContext(Dispatchers.IO) {
        delay(3000)
    }

    fun onBreedSelect(position: Int) {
        viewModel.selectedBreed = breeds[position]
        viewModel.getItemImg(breeds[position].name?.lowercase(Locale.ROOT).toString())
        navController?.navigate(R.id.oneBreedFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        rootElement = null
    }

    @SuppressLint("RestrictedApi")
    private fun initBackStack() {
        navController?.let { InitBackStack.initBackStack(it) }
    }
}
