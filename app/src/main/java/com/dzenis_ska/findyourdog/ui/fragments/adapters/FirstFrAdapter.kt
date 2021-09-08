package com.dzenis_ska.findyourdog.ui.fragments.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.dzenis_ska.findyourdog.R
import com.dzenis_ska.findyourdog.databinding.ItemLayoutForFirstFrBinding
import com.dzenis_ska.findyourdog.ui.utils.CropSquareTransformation
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso

class FirstFrAdapter(): RecyclerView.Adapter<FirstFrAdapter.MPHolder>() {
    val listPhoto = arrayListOf<Int>()
    class MPHolder(val rootElement: ItemLayoutForFirstFrBinding): RecyclerView.ViewHolder(rootElement.root) {
        @SuppressLint("ResourceAsColor")
        fun setData(uri: Int) {
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

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MPHolder {
        val rootElement = ItemLayoutForFirstFrBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MPHolder(rootElement)
    }

    override fun onBindViewHolder(holder: MPHolder, position: Int) {
        holder.setData(listPhoto[position])
    }

    override fun getItemCount(): Int {
        return listPhoto.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(newList: ArrayList<Int>){

//        val diffResult = DiffUtil.calculateDiff(DiffUtilHelper(listPhoto, newList))
//        diffResult.dispatchUpdatesTo(this)
        listPhoto.clear()
        listPhoto.addAll(newList)
        notifyDataSetChanged()
    }
}