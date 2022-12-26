package com.dzenis_ska.findyourdog.ui.fragments.adapters

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.dzenis_ska.findyourdog.databinding.ItemSpinnerBreedBinding
import java.util.*

class SpinnerAdapterMorePhoto(
    private val breeds: List<String>,
    private val breedName: String
) : BaseAdapter()
{
    override fun getItem(position: Int): String {
        return breeds[position]
    }
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
    override fun getCount(): Int {
        return breeds.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding =
            convertView?.tag as ItemSpinnerBreedBinding? ?:
            createBinding(parent.context)
        val breed = getItem(position)
        binding.textBreed.text = breed
        if(compareBreedName(breed)){
            binding.textBreed.setTextColor(Color.YELLOW)
        } else {
            binding.textBreed.setTextColor(Color.WHITE)
        }
        binding.textBreed.tag = breed
        return binding.root
    }

    private fun createBinding(context: Context) : ItemSpinnerBreedBinding{
        val binding = ItemSpinnerBreedBinding.inflate(LayoutInflater.from(context))
        binding.root.tag = binding
        return binding
    }

    private fun compareBreedName(breed: String): Boolean {
        var isCompare = false
        breed.split(" ")
            .forEach { breedInner->
                breedName.split(" ")
                    .forEach { breedName->
                        Log.d("!!!compareTo", "${breedName.lowercase(Locale.getDefault())} _ $breedInner")
                        if(breedName.compareTo(breedInner, ignoreCase = true) == 0)
                            isCompare = true
                    }
            }
        return isCompare
    }

}