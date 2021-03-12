package com.example.findyourdog.Ui

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.findyourdog.R
import com.example.findyourdog.RemoteModel.DogBreeds
import com.example.findyourdog.Ui.fragments.OneBreedFragment
import com.squareup.picasso.Callback
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso


class ImgAdapter(val list: MutableList<String>, val breed: DogBreeds, val context: Context?, val fragment: OneBreedFragment
): RecyclerView.Adapter<ImgAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val photo = itemView.findViewById<ImageView>(R.id.imgItemView)
        val progressBar = itemView.findViewById<View>(R.id.progressBar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.img_item_layout, parent, false)
        val holder = ViewHolder(itemView)
        itemView.setOnClickListener(){
            fragment.onePhoto(holder.adapterPosition)
        }
        return holder
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pict = holder.photo
        val url = list[position]
        val ind = breed.ind
        if(ind == 0){
            holder.progressBar.visibility = View.VISIBLE
            Picasso.with(context)
                .load(url)
                .placeholder(R.drawable.ic_wait_a_litle)
                .error(R.drawable.ic_no_connection)
//            .resize(100, 100)
                .centerCrop()
                .fit()
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
//                .into(pict)
                .into(pict, object : Callback {
                    override fun onSuccess() {
                        holder.progressBar.visibility = View.GONE
                    }
                    override fun onError() {
                        holder.progressBar.visibility = View.GONE
                    }
                })


        }else{
            holder.itemView.visibility = View.GONE
        }

    }

}