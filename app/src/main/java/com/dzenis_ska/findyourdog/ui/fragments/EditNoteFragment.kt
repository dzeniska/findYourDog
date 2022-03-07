package com.dzenis_ska.findyourdog.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
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
    var binding: FragmentEditNoteBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditNoteBinding.inflate(inflater)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()

        binding!!.apply {
            floatingActionButton3.setOnClickListener(){
                saveNote()
            }
        }
        viewModel.selectedBreed?.let {post->
            breed = post
            binding!!.edNoteEdit.setText(breed!!.note)
        }
    }

    private fun saveNote(){
        binding!!.edNoteEdit.text.toString().let {
            breed?.let { note -> viewModel.saveNote(note.id, it) }
            if(it.isNotEmpty()) {
                viewModel.selectedBreed?.note = it
            }else{
                viewModel.selectedBreed?.note = null
            }
        }
        navController!!.popBackStack(R.id.editNoteFragment, true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}