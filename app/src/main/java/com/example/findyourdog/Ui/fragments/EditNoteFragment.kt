package com.example.findyourdog.Ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.findyourdog.R
import com.example.findyourdog.RemoteModel.DogBreeds
import com.example.findyourdog.Ui.MainActivity
import com.example.findyourdog.ViewModel.BreedViewModel
import kotlinx.android.synthetic.main.fragment_edit_note.*

class EditNoteFragment : Fragment() {
    lateinit var navController: NavController
    lateinit var viewModel: BreedViewModel
    lateinit var breed: DogBreeds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//                //in here you can do logic when backPress is clicked
////                navController.popBackStack(R.id.addDogFragment, true)
////                navController.popBackStack(R.id.dogsListFragment, true)
//                navController.popBackStack(R.id.oneBreedFragment, true)
////                navController.popBackStack(R.id.favoritesFragment, true)
//              //  navController.popBackStack(R.id.editNoteFragment, true)
//            }
//        })
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewModel = ViewModelProvider(activity as MainActivity).get(BreedViewModel::class.java)
        return inflater.inflate(R.layout.fragment_edit_note, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()



        floatingActionButton3.setOnClickListener(){
            edNoteEdit.text.toString()?.let {
                viewModel.saveNote(breed.id, it)
                if(it.length > 0) {
                    viewModel.selectedBreed?.note = it
                }else{
                    viewModel.selectedBreed?.note = null
                }
            }
            navController.popBackStack(R.id.editNoteFragment, true)
           // navController.navigate(R.id.oneBreedFragment)
        }
        viewModel.selectedBreed?.let {post->
            breed = post
            edNoteEdit.setText(breed.note)
        }
    }


}