package com.dzenis_ska.findyourdog.ui.fragments.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.dzenis_ska.findyourdog.R
import com.dzenis_ska.findyourdog.databinding.ItemLayoutForMapFrBinding
import com.dzenis_ska.findyourdog.ui.fragments.MapsFragment
import com.dzenis_ska.findyourdog.ui.utils.CropSquareTransformation
import com.dzenis_ska.findyourdog.viewModel.BreedViewModel
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso

class MapPhotoAdapter(val mapFr: MapsFragment): RecyclerView.Adapter<MapPhotoAdapter.MPHolder>() {
    val listPhoto = arrayListOf<String>()
    class MPHolder(val rootElement: ItemLayoutForMapFrBinding): RecyclerView.ViewHolder(rootElement.root) {
        @SuppressLint("ResourceAsColor")
        fun setData(uri: String, mapFr: MapsFragment) {
            val imageView = rootElement.ivMapFrAdapter as ImageView
            Picasso.get()
                .load(uri)
                .placeholder(R.drawable.ic_wait_a_litle)
                .error(R.drawable.ic_no_connection)
//                .resize(w, h)
                .transform(CropSquareTransformation())
//                .centerCrop()
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .into(imageView)
            imageView.setOnClickListener {
                mapFr.animateCamera(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MPHolder {
        val rootElement = ItemLayoutForMapFrBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MPHolder(rootElement)
    }

    override fun onBindViewHolder(holder: MPHolder, position: Int) {
        holder.setData(listPhoto[position], mapFr)
    }

    override fun getItemCount(): Int {
        return listPhoto.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(newList: ArrayList<String>){

//        val diffResult = DiffUtil.calculateDiff(DiffUtilHelper(listPhoto, newList))
//        diffResult.dispatchUpdatesTo(this)
        listPhoto.clear()
        listPhoto.addAll(newList)
        notifyDataSetChanged()
    }

    fun updateItem(index: Int) {
        notifyItemChanged(index)
    }
}