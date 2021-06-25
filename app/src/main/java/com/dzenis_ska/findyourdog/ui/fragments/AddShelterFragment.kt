package com.dzenis_ska.findyourdog.ui.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.dzenis_ska.findyourdog.RemoteModel.*
import com.dzenis_ska.findyourdog.ui.MainActivity
import com.dzenis_ska.findyourdog.viewModel.BreedViewModel
import com.dzenis_ska.findyourdog.databinding.FragmentAddShelterBinding
import com.dzenis_ska.findyourdog.ui.utils.imageManager.ImagePicker
import com.fxn.utility.PermUtil
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddShelterFragment : Fragment() {
    var viewModel: BreedViewModel? = null
    lateinit var breed: DogBreeds
    lateinit var textName: String
    lateinit var vpAdapter: VpAdapter
    var imgBreedList = mutableListOf<String>()
    val listBreed = arrayListOf<String>()
    private var dog = "Dog"
    var bool = true
    lateinit var rootElement: FragmentAddShelterBinding

    var tlm: TabLayoutMediator? = null

    lateinit var navController: NavController
    val REQUEST_PHOTO = 30
    var bmp: Bitmap? = null
    var pref: SharedPreferences? = null
    private var counter: Long = 1000
    var imageUri = ""

    var launcherMultiSelectImage: ActivityResultLauncher<Intent>? = null
    var launcherSingleSelectImage: ActivityResultLauncher<Intent>? = null

//    private val requestPermissions =
//        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
//            updatePermissionsState()
//        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootElement = FragmentAddShelterBinding.inflate(inflater)
        viewModel = ViewModelProvider(activity as MainActivity).get(BreedViewModel::class.java)
        return rootElement.root
    }

    override fun onResume() {
        super.onResume()
        pref = this.activity?.getSharedPreferences("FUCK", 0)
        counter = pref?.getLong("counter", 5000)!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
        init()
        initRecyclerView()
        onClick(this)


    }

    private fun onClick(addShelterFragment: AddShelterFragment) {
        rootElement.apply{
            imgAddPhoto.setOnClickListener(){
                imgAddPhoto.alpha = 0.8f
                ImagePicker.launcher(context, addShelterFragment, launcherMultiSelectImage, 17)
            }
            fabAddImage.setOnClickListener(){

                ImagePicker.launcher(context, addShelterFragment, launcherMultiSelectImage, 1)
            }
            fabDeleteImage.setOnClickListener(){
                CoroutineScope(Dispatchers.Main).launch{
                    val num = viewModel!!.numPage
                    Log.d("!!!num", "${num}")
                    vpAdapter.removeItemAdapter(num)

                }

            }
            fabReplaceImage.setOnClickListener(){
                ImagePicker.launcher(context, addShelterFragment, launcherSingleSelectImage, 1)
            }

            floatingActionButtonAdd.setOnClickListener(){
                ImagePicker.launcher(context, addShelterFragment, launcherMultiSelectImage, 3)
                imgAddPhoto.visibility = View.GONE
            }
        }
    }

    private fun init() {
        launcherMultiSelectImage = ImagePicker.getLauncherForMultiSelectImages(this)
//        launcherSingleSelectImage = ImagePicker.getLauncherForSingleSelectImages(this)
    }

    private fun initRecyclerView() {
        vpAdapter = VpAdapter(this)
        rootElement.apply {
            vp2.adapter = vpAdapter
            tlm = TabLayoutMediator(tabLayout, vp2){ tab, position ->}
            vp2.registerOnPageChangeCallback(
                object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        viewModel?.numPage = position
                        Log.d("!!!", "${position}")
                    }
                }
            )
        }
    }

    fun tabLayoutMediator(b: Boolean) {
        if (b) {
            tlm?.attach()
        } else {
            tlm?.detach()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PermUtil.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(context as MainActivity, "Approvecker", Toast.LENGTH_LONG).show()
//                    ImagePicker.getOptions()
                } else {
                    Toast.makeText(context as MainActivity, "Approve permissions to open Pix ImagePicker", Toast.LENGTH_LONG).show()
                }
                return
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


//    counter++
//    saveData(counter)
    fun saveData(i:Long){
        val edit = pref?.edit()
        edit?.putLong("counter", i)
        edit?.apply()
    }
}


