package com.example.findyourdog.Ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.findyourdog.R
import com.example.findyourdog.Ui.MainActivity
import com.example.findyourdog.ViewModel.BreedViewModel
import kotlinx.android.synthetic.main.fragment_first.*

class FirstFragment : Fragment() {
    lateinit var viewModel: BreedViewModel
    lateinit var navController: NavController


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel =  ViewModelProvider(activity as MainActivity).get(BreedViewModel::class.java)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = findNavController()

        tv1.setOnClickListener(){
            navController.navigate(R.id.dogsListFragment)
        }
        tv2.setOnClickListener(){
            navController.navigate(R.id.addDogFragment)
        }
    }
}