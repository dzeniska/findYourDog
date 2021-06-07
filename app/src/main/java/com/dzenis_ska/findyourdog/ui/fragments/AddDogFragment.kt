package com.dzenis_ska.findyourdog.ui.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.dzenis_ska.findyourdog.R
import com.dzenis_ska.findyourdog.RemoteModel.*
import com.dzenis_ska.findyourdog.ui.MainActivity
import com.dzenis_ska.findyourdog.ViewModel.BreedViewModel
import kotlinx.android.synthetic.main.fragment_add_dog.*

class AddDogFragment : Fragment() {
    lateinit var viewModel: BreedViewModel
    lateinit var navController: NavController
    val REQUEST_PHOTO = 30
    var bmp: Bitmap? = null
    var pref: SharedPreferences? = null
    private var counter: Long = 1000
    var imageUri = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // in here you can do logic when backPress is clicked
                navController.popBackStack(R.id.dogsListFragment, true)
                navController.popBackStack(R.id.addDogFragment, true)
                navController.popBackStack(R.id.oneBreedFragment, true)
                navController.popBackStack(R.id.favoritesFragment, true)
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        viewModel = ViewModelProvider(activity as MainActivity).get(BreedViewModel::class.java)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_dog, container, false)
    }

    override fun onResume() {
        super.onResume()
        pref = this.activity?.getSharedPreferences("FUCK", 0)
        counter = pref?.getLong("counter", 5000)!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()

        imgAddPhoto.setOnClickListener() {
//            val intentPhoto = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val intentPhoto = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intentPhoto.type = "image/*"
            intentPhoto.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            startActivityForResult(intentPhoto, REQUEST_PHOTO)
        }
        floatingActionButtonAdd.setOnClickListener() {
            addDogToDB()
            navController.navigate(R.id.favoritesFragment)
        }
    }

    fun addDogToDB() {
//        val stream = ByteArrayOutputStream()
//        if (bmp != null) {
//            bmp!!.compress(Bitmap.CompressFormat.PNG, 100, stream)
//        } else {
//            val bitmap =
//                (resources.getDrawable(R.drawable.drawer_back_2) as BitmapDrawable).getBitmap()
//            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
//        }
//        val byteArray = stream.toByteArray()

        counter++
        saveData(counter)

        if (edMastDog.text.toString().length > 0 && edDescription.text.toString().length > 0) {
            val weight = Weight("", "")
            val height = Height("", "")
            val image = Image("", 100L, 100L, imageUri)

            val id = counter
            val breed = DogBreeds(
                weight,
                height,
                id,
                edMastDog.text.toString(),
                1,
                1,
                edDescription.text.toString(),
                "byteArray",
                "",
                "",
                "",
                "",
                "",
                "",
                image,
                "",
                "",
                "jopa"
            )
//            viewModel.insertOnePost(breed)

            edDescription.text.clear()
            edDescription.hint = "Ваш пёсель успешно добавлен в базу"

        } else {
            Toast.makeText(activity as MainActivity, "Заполните поля", Toast.LENGTH_SHORT)
                .show()
        }

    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == REQUEST_PHOTO && resultCode == Activity.RESULT_OK) {
//
//            imgAddPhoto.setImageURI(data?.data)
//            imageUri = data?.data.toString()
//        }
//    }
    fun saveData(i:Long){
        val edit = pref?.edit()
        edit?.putLong("counter", i)
        edit?.apply()
    }


}


