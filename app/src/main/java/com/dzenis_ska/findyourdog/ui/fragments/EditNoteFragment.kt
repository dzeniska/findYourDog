package com.dzenis_ska.findyourdog.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.dzenis_ska.findyourdog.R
import com.dzenis_ska.findyourdog.databinding.FragmentEditNoteBinding
import com.dzenis_ska.findyourdog.remoteModel.DogBreeds
import com.dzenis_ska.findyourdog.viewModel.BreedViewModel

class EditNoteFragment : Fragment() {
    var breed: DogBreeds? = null
    private var navController: NavController? = null
    val viewModel: BreedViewModel by activityViewModels()
    var rootElement: FragmentEditNoteBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootElement = FragmentEditNoteBinding.inflate(inflater)
        return rootElement!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()

        rootElement!!.apply {
            floatingActionButton3.setOnClickListener(){
                edNoteEdit.text.toString().let {
                    breed?.let { note -> viewModel.saveNote(note.id, it) }
                    if(it.isNotEmpty()) {
                        viewModel.selectedBreed?.note = it
                    }else{
                        viewModel.selectedBreed?.note = null
                    }
                }
                navController!!.popBackStack(R.id.editNoteFragment, true)
            }
        }
        viewModel.selectedBreed?.let {post->
            breed = post
            rootElement!!.edNoteEdit.setText(breed!!.note)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        rootElement = null
    }
}