package com.dzenis_ska.findyourdog.ui.fragments


import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.davemorrissey.labs.subscaleview.ImageSource
import com.dzenis_ska.findyourdog.databinding.VpAdapterItemBinding
import com.dzenis_ska.findyourdog.ui.utils.imageManager.ImageManager


class VpAdapter(val addSF: AddShelterFragment) : RecyclerView.Adapter<VpAdapter.VpHolder>() {

    private val arrayPhoto = mutableListOf<String>()

    class VpHolder(val binding: VpAdapterItemBinding) : RecyclerView.ViewHolder(binding.root) {
        //        binding.imgItemVp as SubsamplingScaleImageView
        fun setData(bitmap: String) {
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
        holder.setData(arrayPhoto[position])
    }

    override fun getItemCount(): Int {
        return arrayPhoto.size
    }

    fun updateAdapter(arrayListPhoto: MutableList<String>) {
//        arrayPhoto.clear()
//        arrayPhoto.addAll(arrayListPhoto)
//        addSF.rootElement.imgAddPhoto.visibility = View.GONE
//        addSF.rootElement.clEditPhoto.visibility = View.VISIBLE
//        notifyDataSetChanged()
//
        val size = arrayListPhoto.size
        if((size == 1 && arrayPhoto.size == 0)){
            arrayPhoto.add(arrayListPhoto[0])
            hideElements()
            notifyDataSetChanged()
        }else if(size == 1 && arrayPhoto.size > 0){
            val array = arrayPhoto
            arrayPhoto.clear()
            arrayPhoto.add(arrayListPhoto[0])
            arrayPhoto.addAll(array)
            hideElements()
            notifyDataSetChanged()
        }else if(size > 1 && arrayPhoto.size == 0){
            arrayPhoto.addAll(arrayListPhoto)
            hideElements()
            notifyDataSetChanged()
        }else if(size > 1 && arrayPhoto.size > 0){
            val array = arrayPhoto
            arrayPhoto.clear()
            arrayPhoto.addAll(arrayListPhoto)
            arrayPhoto.addAll(array)
            hideElements()
            notifyDataSetChanged()
        }
//
//
    }
////
    fun hideElements(){
        val sizeListAdapter = arrayPhoto.size
        addSF.viewModel?.countSelectedPhoto = sizeListAdapter
        if(sizeListAdapter > 2) addSF.rootElement.fabAddImage.visibility = View.GONE
        if(sizeListAdapter > 1) {
            addSF.tabLayoutMediator(true)
            addSF.rootElement.imgAddPhoto.visibility = View.GONE
            addSF.rootElement.clEditPhoto.visibility = View.VISIBLE
        }else if(sizeListAdapter == 1){
            addSF.tabLayoutMediator(false)
            addSF.rootElement.imgAddPhoto.visibility = View.GONE
            addSF.rootElement.clEditPhoto.visibility = View.VISIBLE
        }
    }

    fun removeItemAdapter(numPage: Int){
//        if(arrayPhoto.size == 1){
//

//            addSF.tabLayoutMediator(false)
//        }

        arrayPhoto.removeAt(numPage)
        notifyItemRemoved(numPage)

//        addSF.tabLayoutMediator(true)
//        for(n in 0 until arrayPhoto.size) notifyItemChanged(n)
//        if(arrayPhoto.size < 2) {
//            addSF.tabLayoutMediator(false                                                                                                                                                                                 )
//            addSF.rootElement.imgAddPhoto.visibility = View.VISIBLE
//            addSF.rootElement.clEditPhoto.visibility = View.GONE
////        adapter.adapterCallBack.onItemDelete()
//        }
    }
    fun replaceItemAdapter( arrayListPhoto: MutableList<String>){
        arrayPhoto.removeAt(addSF.viewModel?.numPage!!)
        arrayPhoto.add(addSF.viewModel?.numPage!!, arrayListPhoto[0])
        notifyDataSetChanged()
    }
}