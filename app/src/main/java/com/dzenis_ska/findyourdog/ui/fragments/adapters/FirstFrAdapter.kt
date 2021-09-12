package com.dzenis_ska.findyourdog.ui.fragments.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.dzenis_ska.findyourdog.databinding.ItemLayoutForFirstFrBinding

class FirstFrAdapter(): RecyclerView.Adapter<FirstFrAdapter.MPHolder>() {
    val listPhoto = arrayListOf<Int>()
    val listText = arrayListOf<String>()
    class MPHolder(val rootElement: ItemLayoutForFirstFrBinding): RecyclerView.ViewHolder(rootElement.root) {
        @SuppressLint("ResourceAsColor")
        fun setData(id: Int, s: String) {
//            val imageView = rootElement.ivMapFrAdapter as ImageView
            rootElement.ivMapFrAdapter.setImageResource(id)
//            Picasso.get()
//                .load(uri)
//                .placeholder(R.drawable.ic_wait_a_litle)
//                .error(R.drawable.ic_no_connection)
////                .resize(w, h)
//                .transform(CropSquareTransformation())
////                .centerCrop()
//                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
//                .into(imageView)
            rootElement.tvDesc.text = s
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MPHolder {
        val rootElement = ItemLayoutForFirstFrBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MPHolder(rootElement)
    }

    override fun onBindViewHolder(holder: MPHolder, position: Int) {
        holder.setData(listPhoto[position], listText[position])
    }

    override fun getItemCount(): Int {
        return listPhoto.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(newList: ArrayList<Int>, listTitle: ArrayList<String>){

//        val diffResult = DiffUtil.calculateDiff(DiffUtilHelper(listPhoto, newList))
//        diffResult.dispatchUpdatesTo(this)
        listPhoto.clear()
        listPhoto.addAll(newList)
        listText.clear()
        listText.addAll(listTitle)
        notifyDataSetChanged()
    }
}