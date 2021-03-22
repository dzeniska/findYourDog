package com.example.findyourdog

import android.R
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
import com.example.findyourdog.Ui.MainActivity
import com.example.findyourdog.ViewModel.BreedViewModel
import kotlinx.android.synthetic.main.fragment_one_photo.*
import java.io.ByteArrayInputStream
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

        return inflater.inflate(com.example.findyourdog.R.layout.fragment_one_photo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = getContext()

//        if (ContextCompat.checkSelfPermission(activity as MainActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            requestPermissions(Array(1) {
//                Manifest.permission.WRITE_EXTERNAL_STORAGE
//            }, 10)
//        }



        viewModel.onePhoto.observe(viewLifecycleOwner, Observer {
            Log.d("!!!px", it.toString())

            val stream = String(it)
            if(stream.length > 0){
                val dirName = context?.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath
                val pathName = "$dirName/${viewModel.fileName}"
                val f = File(pathName)
                Log.d("!!!p", pathName.toString())
                val inputStream: InputStream
                inputStream = it.inputStream()
                inputStream.saveToFile(f)
                val uri = Uri.fromFile(f)
                imgOnePhoto.visibility = View.VISIBLE
                imgOnePhoto as SubsamplingScaleImageView
                imgOnePhoto.setImage(ImageSource.uri(uri))

                progressBarLine.visibility = View.GONE
            }else{
                Toast.makeText(activity as MainActivity, "Плохое соединение с интернетом!", Toast.LENGTH_SHORT).show()
            }
        })
//            Picasso.with(context)
//                .load(it)
//                .placeholder(R.drawable.drawer_back_2)
//                .error(R.drawable.ic_add_dog)
//                .into(imgOnePhoto)
    }

    override fun onResume() {
        super.onResume()
        imgOnePhoto.visibility = View.GONE
        progressBarLine.visibility = View.VISIBLE
    }

}
    fun InputStream.saveToFile(file: File) =use{ input ->
        file.outputStream().use{output ->
            input.copyTo(output)
        }
    }


