package com.dzenis_ska.findyourdog.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import com.dzenis_ska.findyourdog.R
import com.dzenis_ska.findyourdog.databinding.FragIntroBinding
import com.dzenis_ska.findyourdog.remoteModel.firebase.FBAuth
import com.dzenis_ska.findyourdog.ui.MainActivity
import com.dzenis_ska.findyourdog.ui.fragments.adapters.FirstFrAdapter
import com.dzenis_ska.findyourdog.ui.utils.CheckNetwork
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.*

class FirstFragment : Fragment() {
    var rootElement: FragIntroBinding? = null
    var navController: NavController? = null
    private val fbAuth = FBAuth(this)
    var adapter: FirstFrAdapter? = null
//    private val check: CheckNetwork = CheckNetwork()
//    private var isClick: Boolean = true

    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ){
                Toast.makeText(
                    context,
                    "Необходимо разрешение на геолокацию",
                    Toast.LENGTH_LONG
                ).show()
            }else{
                isAuth()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("!!!on", "onCreateView")
        val rootElement = FragIntroBinding.inflate(inflater)
        this.rootElement = rootElement
        return rootElement.root
    }

//    @SuppressLint("RestrictedApi")
//    private fun initBackStack() {
//        val fList = navController?.backStack
//        fList?.forEach {
//            Log.d("!!!frFF", "${it.destination.label}")
//        }
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("!!!on", "onViewCreated")
        navController = findNavController()

        init()
        initClick()
//        initBackStack()
    }

    private fun isAuth() = with(rootElement!!){

        if (fbAuth.mAuth.currentUser == null) {
            fbAuth.signInAnonimously(context) {
                if(it == true) {
                    navController?.navigate(R.id.mapsFragment)
                }else{
                    CheckNetwork.check(activity as MainActivity)
                }
            }
        } else {
            navController?.navigate(R.id.mapsFragment)
        }
    }
    private fun init(){
        val listPhoto = arrayListOf(
            R.drawable.sobaka_ulibaka_1,
            R.drawable.sobaka_podozrevaka_1,
            R.drawable.sobaka_ispugaka_1,
            R.drawable.sobaka_plavaka_1,
            R.drawable.sobaka_ulibaka_escho_1
        )
        val listTitle = arrayListOf(
            "Улыбыка",
            "Подозревака",
            "Испугака",
            "Капитака",
            "Улыбыка есчо"
        )
        adapter = FirstFrAdapter()
        rootElement!!.rcFF.adapter = adapter
        rootElement!!.rcFF.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        adapter!!.updateAdapter(listPhoto, listTitle)

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(rootElement!!.rcFF)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initClick() {
        rootElement!!.apply {
            imageButton1.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    v.isPressed = true
                    imageButton1.elevation = 5f
                }else if (event.action == MotionEvent.ACTION_UP){
                    v.isPressed = false
                    imageButton1.elevation = 26f
                    navController?.navigate(R.id.dogsListFragment)


                }else if (event.action == MotionEvent.ACTION_CANCEL){
                    v.isPressed = false
                    imageButton1.elevation = 26f
                }
                false
            }
            imBtnMap.setOnTouchListener { v, event ->
                Log.d("!!!click", "click")
                if (event.action == MotionEvent.ACTION_DOWN) {
                    v.isPressed = true
                    imBtnMap.elevation = 5f
                } else if (event.action == MotionEvent.ACTION_UP) {
                    v.isPressed = false
                    imBtnMap.elevation = 26f
                    if (ContextCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        requestPermissions.launch(permissions)
                    } else {
                        isAuth()
                    }
                } else if (event.action == MotionEvent.ACTION_CANCEL) {
                    v.isPressed = false
                    imBtnMap.elevation = 26f
                }
                false
            }
            imBtnAuth.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    v.isPressed = true
                    imBtnAuth.elevation = 5f
                }else if (event.action == MotionEvent.ACTION_UP){
                    v.isPressed = false
                    imBtnAuth.elevation = 26f
                    navController?.navigate(R.id.loginFragment)
                }else if (event.action == MotionEvent.ACTION_CANCEL){
                    v.isPressed = false
                    imBtnAuth.elevation = 26f
                }
                false
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d("!!!on", "onAttach")
    }

    override fun onDetach() {
        super.onDetach()
        Log.d("!!!on", "onDetach")
    }

    override fun onResume() {
        super.onResume()
        currentUser(fbAuth.mAuth.currentUser)
        Log.d("!!!on", "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d("!!!on", "onPause")
    }

    override fun onDestroyView() {
        rootElement = null
        super.onDestroyView()
        Log.d("!!!on", "onDestroyView")
    }
    private fun currentUser(currentUser: FirebaseUser?) {
        Log.d("!!!currentUser", "currentUser")
        if (currentUser == null) {
            fbAuth.signInAnonimously(context){
                if(it == false)
                    CheckNetwork.check(activity as MainActivity)
                else
                    Toast.makeText(context, context?.resources?.getString(R.string.hi), Toast.LENGTH_SHORT).show()
            }
        }
    }
    companion object {
        private val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }
}
