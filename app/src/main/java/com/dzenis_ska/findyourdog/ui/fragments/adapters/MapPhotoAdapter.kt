package com.dzenis_ska.findyourdog.ui.fragments.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.dzenis_ska.findyourdog.R
import com.dzenis_ska.findyourdog.databinding.ItemLayoutForMapFrBinding
import com.dzenis_ska.findyourdog.remoteModel.firebase.AdShelter
import com.dzenis_ska.findyourdog.ui.fragments.MapsFragment
import com.dzenis_ska.findyourdog.ui.utils.CropSquareTransformation
import com.squareup.picasso.Callback
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso

class MapPhotoAdapter(val mapFr: MapsFragment): RecyclerView.Adapter<MapPhotoAdapter.MPHolder>() {
    val listShelter = arrayListOf<AdShelter>()
    class MPHolder(val rootElement: ItemLayoutForMapFrBinding): RecyclerView.ViewHolder(rootElement.root) {
        @SuppressLint("ResourceAsColor")
        fun setData(adShelter: AdShelter, mapFr: MapsFragment) {
            val imageView = rootElement.ivMapFrAdapter as ImageView

            val anim = AnimationUtils.loadAnimation(mapFr.context, R.anim.alpha_fab)
            rootElement.progressBarMapItemAdapter.startAnimation(anim)
            rootElement.progressBar4.visibility = View.VISIBLE

            Picasso.get()
                .load(adShelter.photoes?.get(0))
                .placeholder(R.drawable.ic_wait_a_litle)
                .error(R.drawable.ic_broken_image)
//                .resize(w, h)
                .transform(CropSquareTransformation())
//                .centerCrop()
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .into(imageView, object : Callback{
                    override fun onSuccess() {
                        rootElement.progressBarMapItemAdapter.visibility = View.GONE
                        rootElement.progressBarMapItemAdapter.clearAnimation()
                        rootElement.progressBar4.visibility = View.GONE
                    }
                    override fun onError(e: Exception?) {
                        rootElement.progressBarMapItemAdapter.visibility = View.GONE
                        rootElement.progressBarMapItemAdapter.clearAnimation()
                        rootElement.progressBar4.visibility = View.GONE
                    }

                })
            imageView.setOnClickListener {
                mapFr.animateCamera(adShelter)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MPHolder {
        val rootElement = ItemLayoutForMapFrBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MPHolder(rootElement)
    }

    override fun onBindViewHolder(holder: MPHolder, position: Int) {
        holder.setData(listShelter[position], mapFr)
//        listShelter[position].photoes?.get(0)?.let { holder.setData(it, mapFr) }
    }

    override fun getItemCount(): Int {
        return listShelter.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(newList: ArrayList<AdShelter>){
//        val diffResult = DiffUtil.calculateDiff(DiffUtilHelper(listPhoto, newList))
//        diffResult.dispatchUpdatesTo(this)
        listShelter.clear()
        listShelter.addAll(newList)
        notifyDataSetChanged()
    }
}