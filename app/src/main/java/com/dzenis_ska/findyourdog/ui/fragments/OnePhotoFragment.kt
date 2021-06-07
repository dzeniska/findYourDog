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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.dzenis_ska.findyourdog.ui.MainActivity
import com.dzenis_ska.findyourdog.ViewModel.BreedViewModel
import kotlinx.android.synthetic.main.fragment_one_photo.*
import java.io.File
import java.io.InputStream


class OnePhotoFragment : Fragment() {
    lateinit var viewModel: BreedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(activity as MainActivity).get(BreedViewModel::class.java)
        // Inflate the layout for this fragment

        return inflater.inflate(
            com.dzenis_ska.findyourdog.R.layout.fragment_one_photo,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = context


        viewModel.onePhoto.observe(viewLifecycleOwner, Observer {
            Log.d("!!!px", it.toString())

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
                imgOnePhoto.visibility = View.VISIBLE
                imgOnePhoto as SubsamplingScaleImageView
                imgOnePhoto.setImage(ImageSource.uri(uri))

                progressBarLine.visibility = View.GONE
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
        imgOnePhoto.visibility = View.GONE
        progressBarLine.visibility = View.VISIBLE
    }
}

fun InputStream.saveToFile(f: File) = use { input ->
    f.outputStream().use { output ->
        input.copyTo(output)
    }
}


