package com.example.findyourdog

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.findyourdog.RemoteModel.Dog

class AdapterDogs(val list:MutableList<Dog>/*, val activity: MainActivity*/ ): RecyclerView.Adapter<AdapterDogs.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val photo = itemView.findViewById<ImageView>(R.id.imgItemPhoto)
        val breed = itemView.findViewById<TextView>(R.id.tvItemBreedOfDog)
        val description = itemView.findViewById<TextView>(R.id.tvItemDescription)
    }
    //jnsdhcbhsbcdsc

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        val holder = ViewHolder(itemView)
        /*itemView.setOnClickListener(){
            val position = holder.adapterPosition
            activity.showP(position)
        }*/
        return holder
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(list[position].photo == null){
            holder.photo.setImageResource(R.drawable.add_photo)
        }else{
            val bmpArray = list[position].photo!!
            val bmp = BitmapFactory.decodeByteArray(bmpArray, 0, bmpArray.size)
            holder.photo.setImageBitmap(bmp)
        }
        holder.breed.text = list[position].breedOfDog
        holder.description.text = list[position].description
    }
}