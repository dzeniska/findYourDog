package com.example.findyourdog

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.findyourdog.Ui.MainActivity
import com.example.findyourdog.ViewModel.BreedViewModel
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_one_photo.*

class OnePhotoFragment : Fragment() {
    lateinit var viewModel: BreedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(activity as MainActivity).get(BreedViewModel::class.java)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_one_photo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = getContext()
        viewModel.onePhoto.let {
            Picasso.with(context)
                .load(it)
                .placeholder(R.drawable.drawer_back_2)
                .error(R.drawable.ic_add_dog)
                .into(imgOnePhoto)
        }

    }


}