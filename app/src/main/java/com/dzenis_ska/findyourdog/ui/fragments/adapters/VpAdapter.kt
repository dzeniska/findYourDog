package com.dzenis_ska.findyourdog.ui.fragments.adapters


import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.davemorrissey.labs.subscaleview.ImageSource
import com.dzenis_ska.findyourdog.R
import com.dzenis_ska.findyourdog.databinding.VpAdapterItemBinding
import com.dzenis_ska.findyourdog.ui.MainActivity
import com.dzenis_ska.findyourdog.ui.fragments.AddShelterFragment
import com.dzenis_ska.findyourdog.ui.utils.imageManager.ImageManager
import com.squareup.picasso.Callback
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception


class VpAdapter(val addSF: AddShelterFragment) : RecyclerView.Adapter<VpAdapter.VpHolder>() {

    val scope = CoroutineScope(Dispatchers.Main)
    val arrayPhoto = mutableListOf<Uri>()
    val arrayPhotoBool = mutableMapOf<Int, Boolean>()

    class VpHolder(val binding: VpAdapterItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun setData(
            addSF: AddShelterFragment,
            scope: CoroutineScope,
            uri: Uri,
            b: Boolean
        ) {
            binding.imgItemVp.setOnClickListener{
                addSF.fullScreen(250, 0.50f)
                addSF.toFragOnePhoto(uri)
            }
           /* binding.imgItemVp.orientation = ImageManager.imageRotationPreview(bitmap)
//            binding.imgItemVp.setImageBitmap(bitmap)
            binding.imgItemVp.setImage(ImageSource.uri(bitmap))
//            binding.imgItemVp.setImage(ImageSource.bitmap(bitmap))*/

//            scope.launch {
//                Picasso.get()
//                    .load(uri)
//                    .into(binding.imgItemVp)
//            }

            if(!b) {
                scope.launch {
                    binding.imgItemVpSub.visibility = View.VISIBLE
                    binding.imgItemVp.visibility = View.GONE
                    binding.imgItemVpSub.orientation = ImageManager.imageRotationNew(uri, addSF.activity as MainActivity)
                    binding.imgItemVpSub.setImage(ImageSource.uri(uri))
                }
            }else{
                binding.imgItemVpSub.visibility = View.GONE
                binding.imgItemVp.visibility = View.VISIBLE
//                Picasso.get().load(uri).into(binding.imgItemVp)
                binding.progressBarS.visibility = View.VISIBLE
                Picasso.get()
                    .load(uri)
                    .placeholder(R.drawable.ic_wait_a_litle)
                    .error(R.drawable.ic_no_connection)
//            .resize(100, 100)
                    .centerCrop()
                    .fit()
                    .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
//                .into(pict)
                    .into(binding.imgItemVp, object : Callback {
                        override fun onSuccess() {
                            binding.progressBarS.visibility = View.GONE
                        }
                        override fun onError(e: Exception?) {
                            binding.progressBarS.visibility = View.GONE
                        }
                    })
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VpHolder {
        val binding =
            VpAdapterItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VpHolder(binding)
    }

    override fun onBindViewHolder(holder: VpHolder, position: Int) {
            holder.setData( addSF, scope, arrayPhoto[position], arrayPhotoBool[position]!!)
    }

    override fun getItemCount(): Int {
        return arrayPhoto.size
    }

    @SuppressLint("RestrictedApi")
    fun updateAdapter(arrayListPhoto: List<Uri>, b: Boolean) {
        arrayPhoto.clear()
        arrayPhoto.addAll(arrayListPhoto)
        arrayPhotoBool.clear()
        Log.d("!!!parseUri", "${arrayPhotoBool}")
        for (n in 0..4){
            arrayPhotoBool.put(n, b)
        }
        Log.d("!!!parseUri", "${arrayPhotoBool}")
        addSF.rootElement!!.apply {
            addSF.hideAddPhoto(false)
            if (arrayPhoto.size == 5) fabAddImage.visibility = View.GONE
        }
        Log.d("!!!addPhotoo",  "${arrayPhoto.size}")
        if(arrayPhoto.size > 1) {
            addSF.tabLayoutMediator(true)
        }
        notifyDataSetChanged()
    }

    fun updateAdapterForSinglePhoto(arrayListPhoto: List<Uri>) {
        var size = arrayPhoto.size
        arrayPhoto.addAll(arrayListPhoto)
        Log.d("!!!parseUri", "${arrayPhotoBool}")
        arrayListPhoto.forEach { _ ->
            arrayPhotoBool.put(size, false)
            size++
        }
        Log.d("!!!parseUri", "${arrayPhotoBool}")
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
        Log.d("!!!parseUri", "${arrayPhotoBool}")
        for(n in 0..4) {
            if(n in numPage..3) {
                if(n<4) arrayPhotoBool[n+1]?.let { arrayPhotoBool.put(n, it) }
            }
        }

        Log.d("!!!parseUri", "${arrayPhotoBool}")

        if (arrayPhoto.size < 5) addSF.rootElement!!.fabAddImage.visibility = View.VISIBLE
        if(arrayPhoto.size < 2) {
            addSF.tabLayoutMediator(false)
            addSF.rootElement!!.tabLayout.visibility = View.GONE
        }
        if(arrayPhoto.size == 0) with(addSF.rootElement!!) {
            addSF.hideAddPhoto(true)
        }
        notifyItemRemoved(numPage)
    }
    fun replaceItemAdapter( arrayListPhoto: List<Uri>){
        if(arrayListPhoto.size == 1) {
            Log.d("!!!parseUri", "${arrayPhotoBool}")
            Log.d("!!!numPage", "${addSF.viewModel.numPage}")
            arrayPhoto.removeAt(addSF.viewModel.numPage)
            arrayPhotoBool[addSF.viewModel.numPage] = false
            Log.d("!!!parseUri", "${arrayPhotoBool}")
            arrayPhoto.add(addSF.viewModel.numPage, arrayListPhoto[0])
            notifyDataSetChanged()
        }
    }

}