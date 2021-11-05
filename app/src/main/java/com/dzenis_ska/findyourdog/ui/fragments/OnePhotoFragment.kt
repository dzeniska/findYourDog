package com.dzenis_ska.findyourdog.ui.fragments

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
import androidx.navigation.fragment.findNavController
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.dzenis_ska.findyourdog.databinding.FragmentOnePhotoBinding
import com.dzenis_ska.findyourdog.ui.MainActivity
import com.dzenis_ska.findyourdog.ui.utils.InitBackStack
import com.dzenis_ska.findyourdog.viewModel.BreedViewModel
import java.io.File
import java.io.InputStream


class OnePhotoFragment : Fragment() {
    val viewModel: BreedViewModel by activityViewModels()

    var rootElement: FragmentOnePhotoBinding? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("!!!onCreateView", "OnePhotoFragment")

        rootElement = FragmentOnePhotoBinding.inflate(inflater)
        return rootElement!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("!!!onViewCreated", "OnePhotoFragment")

        InitBackStack.initBackStack(findNavController())
        initViewModel()

    }


    private fun initViewModel(){
        viewModel.onePhoto.observe(viewLifecycleOwner, Observer {byteArray ->
            Log.d("!!!onviewModel.onePhoto.observe", "OnePhotoFragment ${byteArray}")
            if(byteArray != null) {
                val stream = String(byteArray)
                if (stream.isNotEmpty()) {
                    val dirName =
                        context?.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath
//                    val name = if(viewModel.fileName.isNotEmpty())
//                        viewModel.fileName
//                    else
//                        "jopa.jpg"
                    val pathName = "$dirName/jopa.jpg"
                    val f = File(pathName)
                    Log.d("!!!p", pathName)
                    val inputStream: InputStream
                    inputStream = byteArray.inputStream()
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
            }
        })
    }

    override fun onResume() {
        super.onResume()
        Log.d("!!!onResume", "OnePhotoFragment")
        rootElement!!.apply {
            imgOnePhoto.visibility = View.GONE
            progressBarLine.visibility = View.VISIBLE
        }
    }
    override fun onPause() {
        super.onPause()
        Log.d("!!!onPause", "OnePhotoFragment")
    }

    override fun onStop() {
//        if(viewModel.isAddSF) viewModel.openFragShelterWithoutAdViewed()
        super.onStop()
        Log.d("!!!onStop", "OnePhotoFragment")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("!!!onDestroy", "OnePhotoFragment")
    }

    override fun onDetach() {
        super.onDetach()
        Log.d("!!!onDetach", "OnePhotoFragment")
    }

    fun InputStream.saveToFile(f: File) = use { input ->
        f.outputStream().use { output ->
            input.copyTo(output)
        }
    }

    override fun onDestroyView() {

        Log.d("!!!onDestroyView", "OnePhotoFragment")
        super.onDestroyView()
        rootElement = null
    }
    companion object {
        const val PHOTO_URI = "photoUri"
    }
}




