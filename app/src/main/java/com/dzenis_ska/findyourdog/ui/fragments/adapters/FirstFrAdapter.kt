package com.dzenis_ska.findyourdog.ui.fragments.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dzenis_ska.findyourdog.R
import com.dzenis_ska.findyourdog.databinding.ItemLayoutForFirstFrBinding

class FirstFrAdapter(): RecyclerView.Adapter<FirstFrAdapter.MPHolder>() {
    val listPhoto = arrayListOf<Int>()
    val listText = arrayListOf<String>()
    class MPHolder(view: View): RecyclerView.ViewHolder(view) {
        val rootElement = ItemLayoutForFirstFrBinding.bind(view)
        @SuppressLint("ResourceAsColor")
        fun setData(id: Int, s: String) {
            rootElement.ivMapFrAdapter.setImageResource(id)
            rootElement.tvDesc.text = s
        }
        companion object {
            fun create (parent: ViewGroup): MPHolder {
                return MPHolder(LayoutInflater
                    .from(parent.context)
                    .inflate(R.layout.item_layout_for_first_fr, parent, false))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MPHolder {
//        val rootElement = ItemLayoutForFirstFrBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MPHolder.create(parent)
    }

    override fun onBindViewHolder(holder: MPHolder, position: Int) {
        holder.setData(listPhoto[position], listText[position])
    }

    override fun getItemCount(): Int {
        return listPhoto.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(newList: ArrayList<Int>, listTitle: ArrayList<String>){
        listPhoto.clear()
        listPhoto.addAll(newList)
        listText.clear()
        listText.addAll(listTitle)
        notifyDataSetChanged()
    }
}