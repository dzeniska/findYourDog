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
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import com.dzenis_ska.findyourdog.R
import com.dzenis_ska.findyourdog.databinding.FragmentIntroBinding
import com.dzenis_ska.findyourdog.remoteModel.firebase.FBAuth
import com.dzenis_ska.findyourdog.ui.MainActivity
import com.dzenis_ska.findyourdog.ui.fragments.adapters.FirstFrAdapter
import com.dzenis_ska.findyourdog.ui.utils.CheckNetwork
import kotlinx.coroutines.Job

class FirstFragment : Fragment() {
    var rootElement: FragmentIntroBinding? = null
    var navController: NavController? = null
    private val fbAuth = FBAuth()
    var adapter: FirstFrAdapter? = null

    private var job2: Job? = null

    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ){
                toastLong(R.string.need_permission_for_geolocation)
            }else{
                isAuth()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("!!!on", "onCreateView")
        val rootElement = FragmentIntroBinding.inflate(inflater)
        this.rootElement = rootElement
        return rootElement.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("!!!on", "onViewCreated")
        navController = findNavController()

        init()
        initClick()
    }

    private fun isAuth() = with(rootElement!!){
        val currUser = fbAuth.mAuth.currentUser
        if (currUser == null) {
            fbAuth.signInAnonymously() {isSignAnon, messAnonSign ->
                if(isSignAnon == true) {
                    navController?.navigate(R.id.mapsFragment)
                } else {
                        CheckNetwork.check(activity as MainActivity)

                }
                Toast.makeText(context, messAnonSign, Toast.LENGTH_LONG).show()
            }
        } else {
            navController?.navigate(R.id.mapsFragment)
        }
    }

    private fun init(){
        val listPhoto = arrayListOf(
            R.drawable.sobaka_ulibaka,
            R.drawable.sobaka_podozrevaka,
            R.drawable.sobaka_zabiyaka,
            R.drawable.sobaka_sugar_wool,
            R.drawable.sobaka_ispugaka,
            R.drawable.sobaka_plavaka,
            R.drawable.sobaka_ulibaka_escho
        )
        val listTitle = arrayListOf(
            res(R.string.smile),
            res(R.string.suspect),
            res(R.string.bully),
            res(R.string.sweet_cotton_wool),
            res(R.string.fright),
            res(R.string.capitan),
            res(R.string.smile_else)
        )
        adapter = FirstFrAdapter()
        rootElement!!.rcFF.adapter = adapter
        rootElement!!.rcFF.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        adapter!!.updateAdapter(listPhoto, listTitle)

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(rootElement!!.rcFF)
//        rootElement!!.rcFF.scrollToPosition(4)
//        rootElement!!.rcFF.smoothScrollBy(300,0)
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
                    val direction = FirstFragmentDirections.actionFirstFragmentToDogsListFragment()
                    navController?.navigate(direction,
                    navOptions {
                        anim {
                            enter = R.anim.enter
                            exit = R.anim.exit
                            popEnter = R.anim.pop_enter
                            popExit = R.anim.pop_exit
                        }
                    }
                        )


                }else if (event.action == MotionEvent.ACTION_CANCEL){
                    v.isPressed = false
                    imageButton1.elevation = 26f
                }
                false
            }

            //todo without map

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
//        adapter?.notifyDataSetChanged()
        Log.d("!!!on", "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d("!!!on", "onPause")
    }

    override fun onDestroyView() {
        job2 = null
        rootElement = null
        super.onDestroyView()
        Log.d("!!!on", "onDestroyView")
    }

    companion object {
        private val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }
}
