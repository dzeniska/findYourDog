package com.dzenis_ska.findyourdog.ui.fragments.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.dzenis_ska.findyourdog.R
import com.dzenis_ska.findyourdog.remoteModel.DogBreeds
import com.dzenis_ska.findyourdog.ui.fragments.DogsListFragment
import com.dzenis_ska.findyourdog.ui.fragments.FavoritesFragment
import com.dzenis_ska.findyourdog.ui.utils.CropSquareTransformation
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso


class AdapterBreeds(val list: MutableList<DogBreeds>, val context: Context?, val fragment: Fragment
): RecyclerView.Adapter<AdapterBreeds.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val photo = itemView.findViewById<ImageView>(R.id.imgItemPhoto)
        val like = itemView.findViewById<ImageView>(R.id.image_like)
        val breed = itemView.findViewById<TextView>(R.id.tvItemBreedOfDog)
        val description = itemView.findViewById<TextView>(R.id.tvItemBreedOfDog)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        val holder = ViewHolder(itemView)

        itemView.setOnClickListener(){
            if(fragment is DogsListFragment) {
                fragment.onBreedSelect(holder.adapterPosition)
            }else if(fragment is FavoritesFragment){
                fragment.onBreedFavSelect(holder.adapterPosition)
            }
        }
        holder.description.setOnClickListener(){
            if(fragment is DogsListFragment) {
                fragment.onBreedSelect(holder.adapterPosition)
            }else if(fragment is FavoritesFragment){
                fragment.onBreedFavSelect(holder.adapterPosition)
            }
        }
        return holder
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.animation = AnimationUtils.loadAnimation(context, R.anim.translate_item)
        val width = list[position].image.width?.toInt()
        val height = list[position].image.height?.toInt()
        val w:Int
        val h:Int
        if(width!! > height!!){
            w = 100
            h = w*height/width
        }else{
            h = 100
            w = h*width/height
        }
        val pict = holder.photo
        val url: String? = list[position].image.url
//        val ind = list[position].ind
//        if(ind == 0){
            Picasso.get()
                .load(url)
                .placeholder(R.drawable.ic_wait_a_litle)
                .error(R.drawable.ic_no_connection)
                .resize(w, h)
                .transform(CropSquareTransformation())
                .centerCrop()
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .into(pict)

        holder.breed.text = list[position].name
        val pos = list[position].isFavorite
        if (pos == 1){
            holder.like.setImageResource(R.drawable.paw_red_2)
        }else{
            holder.like.setImageResource(R.drawable.paw_blue)
        }
    }
}