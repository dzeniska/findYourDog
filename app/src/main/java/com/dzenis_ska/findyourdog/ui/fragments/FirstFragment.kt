package com.dzenis_ska.findyourdog.ui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import com.dzenis_ska.findyourdog.R
import com.dzenis_ska.findyourdog.databinding.FragIntroBinding
import com.dzenis_ska.findyourdog.remoteModel.firebase.FBAuth
import com.dzenis_ska.findyourdog.ui.MainActivity
import com.dzenis_ska.findyourdog.ui.fragments.adapters.FirstFrAdapter
import com.dzenis_ska.findyourdog.viewModel.BreedViewModel
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.*


class FirstFragment : Fragment() {
    var rootElement: FragIntroBinding? = null
    var navController: NavController? = null
    private val fbAuth = FBAuth(this)
    var adapter: FirstFrAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("!!!on", "onCreateView")
        val rootElement = FragIntroBinding.inflate(inflater)
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
    private fun init(){
        val listPhoto = arrayListOf(
            R.drawable.sobaka_ulibaka_1,
            R.drawable.sobaka_podozrevaka_1,
            R.drawable.sobaka_ispugaka_1,
            R.drawable.sobaka_plavaka_1
        )
        val listTitle = arrayListOf(
            "Улыбыка",
            "Подозревака",
            "Испугака",
            "Капитака"
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
                }
                false
            }
            imBtnMap.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    v.isPressed = true
                    imageButton1.elevation = 5f
                }else if (event.action == MotionEvent.ACTION_UP){
                    v.isPressed = false
                    imageButton1.elevation = 26f
                    navController?.navigate(R.id.mapsFragment)
                }
                false
            }
            imBtnAuth.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    v.isPressed = true
                    imageButton1.elevation = 5f
                }else if (event.action == MotionEvent.ACTION_UP){
                    v.isPressed = false
                    imageButton1.elevation = 26f
                    navController?.navigate(R.id.loginFragment)
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
        (activity as MainActivity).checkNetwork(null)
        if (currentUser == null) {
            fbAuth.signInAnonimously(context as MainActivity, object : FBAuth.Listener {
                override fun onComplete() {
                    Toast.makeText(
                        activity,
                        "Вы вошли как Гость",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
        }
    }
}
