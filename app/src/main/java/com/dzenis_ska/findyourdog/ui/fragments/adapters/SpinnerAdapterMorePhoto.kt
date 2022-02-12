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

typealias OnSelectPressedListener = (String) -> Unit

class SpinnerAdapterMorePhoto(
    private val breeds: List<String>,
    private val breedName: String,
//    private val onSelectPressedListener: OnSelectPressedListener
) : BaseAdapter()/*, View.OnClickListener*/
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
//        binding.root.tag = breed
        binding.textBreed.tag = breed
        return binding.root
    }

//    override fun onClick(v: View) {
////        val breed = v.tag as ItemSpinnerBreedBinding
////        val text = breed.textBreed.text.toString()
//        val text = v.tag as String
//        onSelectPressedListener.invoke(text)
//    }

    private fun createBinding(context: Context) : ItemSpinnerBreedBinding{
        val binding = ItemSpinnerBreedBinding.inflate(LayoutInflater.from(context))
//        binding.root.setOnClickListener(this)
//        binding.textBreed.setOnClickListener(this)
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