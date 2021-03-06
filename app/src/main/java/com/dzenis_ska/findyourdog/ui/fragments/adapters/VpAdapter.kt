package com.dzenis_ska.findyourdog.ui.fragments.adapters


import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.davemorrissey.labs.subscaleview.ImageSource
import com.dzenis_ska.findyourdog.databinding.VpAdapterItemBinding
import com.dzenis_ska.findyourdog.ui.fragments.AddShelterFragment
import com.dzenis_ska.findyourdog.ui.utils.imageManager.ImageManager


class VpAdapter(val addSF: AddShelterFragment) : RecyclerView.Adapter<VpAdapter.VpHolder>() {

    val arrayPhoto = mutableListOf<String>()

    class VpHolder(val binding: VpAdapterItemBinding) : RecyclerView.ViewHolder(binding.root) {
        //        binding.imgItemVp as SubsamplingScaleImageView
        fun setData(bitmap: String, addSF: AddShelterFragment) {
            binding.imgItemVp.setOnClickListener{
                addSF.fullScreen(250, 0.50f)
            }
            binding.imgItemVp.orientation = ImageManager.imageRotationPreview(bitmap)
//            binding.imgItemVp.setImageBitmap(bitmap)
            binding.imgItemVp.setImage(ImageSource.uri(bitmap))
//            binding.imgItemVp.setImage(ImageSource.bitmap(bitmap))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VpHolder {
        val binding =
            VpAdapterItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VpHolder(binding)
    }

    override fun onBindViewHolder(holder: VpHolder, position: Int) {
        holder.setData(arrayPhoto[position], addSF)
    }

    override fun getItemCount(): Int {
        return arrayPhoto.size
    }

    fun updateAdapter(arrayListPhoto: MutableList<String>) {
        arrayPhoto.clear()
        arrayPhoto.addAll(arrayListPhoto)
        addSF.rootElement!!.apply {
            imgAddPhoto.visibility = View.GONE
            clEditPhoto.visibility = View.VISIBLE
            if (arrayPhoto.size == 5) fabAddImage.visibility = View.GONE
        }
        Log.d("!!!addPhotoo",  "${arrayPhoto.size}")
        if(arrayPhoto.size > 1) {
            addSF.tabLayoutMediator(true)
        }
        notifyDataSetChanged()

    }
    fun updateAdapterForSinglePhoto(arrayListPhoto: MutableList<String>) {
        arrayPhoto.addAll(arrayListPhoto)
        if (arrayPhoto.size == 5) addSF.rootElement!!.fabAddImage.visibility = View.GONE
//        notifyItemInserted(0)
        Log.d("!!!addPhotoo",  "${arrayPhoto.size}")
        if(arrayPhoto.size > 1) {
            addSF.tabLayoutMediator(true)
            addSF.rootElement!!.tabLayout.visibility = View.VISIBLE
        }
        notifyDataSetChanged()
    }

    fun removeItemAdapter(numPage: Int){
        arrayPhoto.removeAt(numPage)
//        Log.d("!!!delPhotoo",  "${arrayPhoto}")
//        Log.d("!!!delPhotoo",  "${numPage}")
        if (arrayPhoto.size < 5) addSF.rootElement!!.fabAddImage.visibility = View.VISIBLE
        if(arrayPhoto.size < 2) {
            addSF.tabLayoutMediator(false)
            addSF.rootElement!!.tabLayout.visibility = View.GONE
        }
        if(arrayPhoto.size == 0) with(addSF.rootElement!!) {
            imgAddPhoto.visibility = View.VISIBLE
            clEditPhoto.visibility = View.GONE
        }
        notifyItemRemoved(numPage)
    }
    fun replaceItemAdapter( arrayListPhoto: MutableList<String>){
        arrayPhoto.removeAt(addSF.viewModel.numPage)
        arrayPhoto.add(addSF.viewModel.numPage, arrayListPhoto[0])
        notifyDataSetChanged()
    }
}