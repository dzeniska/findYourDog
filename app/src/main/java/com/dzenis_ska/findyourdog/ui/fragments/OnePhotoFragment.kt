package com.dzenis_ska.findyourdog.ui.fragments

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.dzenis_ska.findyourdog.databinding.FragmentOnePhotoBinding
import com.dzenis_ska.findyourdog.ui.MainActivity
import com.dzenis_ska.findyourdog.viewModel.BreedViewModel
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import java.io.InputStream


class OnePhotoFragment : Fragment() {
    val viewModel: BreedViewModel by activityViewModels()
    var rootElement: FragmentOnePhotoBinding? = null
    private var job: Job? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootElement = FragmentOnePhotoBinding.inflate(inflater)
        return rootElement!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.onePhoto.observe(viewLifecycleOwner, Observer {
//
//            job = CoroutineScope(Dispatchers.IO).launch {
////                getBitmapFromPicasso(it)
//                Log.d("!!!px1", it.toString())
//                val bt: Bitmap = Picasso.get().load(Uri.parse(it)).get()
//                Log.d("!!!px2", "$bt")
//                rootElement!!.imgOnePhoto.setImage(ImageSource.bitmap(bt))
//                Log.d("!!!px3", it.toString())
////                rootElement!!.imgOnePhoto.setImageBitmap(bt)
//                rootElement!!.progressBarLine.visibility = View.GONE
//            }
            val stream = String(it)
            if (stream.isNotEmpty()) {
                val dirName =
                    context?.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath
                val pathName = "$dirName/${viewModel.fileName}"
                val f = File(pathName)
                Log.d("!!!p", pathName)
                val inputStream: InputStream
                inputStream = it.inputStream()
                inputStream.saveToFile(f)
                val uri = Uri.fromFile(f)
//                Glide.with(this).load(uri)
                rootElement!!.apply {
                    imgOnePhoto.visibility = View.VISIBLE
                    imgOnePhoto as SubsamplingScaleImageView
                    imgOnePhoto.setImage(ImageSource.uri(uri))
//                    imgOnePhoto.setImage(ImageSource.bitmap(bitmap))
                    progressBarLine.visibility = View.GONE
                }
            } else {
                Toast.makeText(
                    activity as MainActivity,
                    "Плохое соединение с интернетом!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        rootElement!!.apply {
            imgOnePhoto.visibility = View.GONE
            progressBarLine.visibility = View.VISIBLE
        }
    }
    fun InputStream.saveToFile(f: File) = use { input ->
        f.outputStream().use { output ->
            input.copyTo(output)
        }
    }
}




