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
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.constraintlayout.helper.widget.Carousel
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.dzenis_ska.findyourdog.R
import com.dzenis_ska.findyourdog.databinding.FragIntroBinding
import com.dzenis_ska.findyourdog.viewModel.BreedViewModel
import kotlinx.coroutines.*

//import androidx.constraintlayout.helper.widget.Carousel

class FirstFragment : Fragment() {
    var rootElement: FragIntroBinding? = null
    lateinit var navController: NavController
    var job: Job? = null
    var carousel: Carousel? = null
    var carousel2: Carousel? = null
    val viewModel: BreedViewModel by activityViewModels()

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
        Log.d("!!!on", "${carousel}")
        carousel = rootElement!!.carousel

        Log.d("!!!on", "${carousel}")
        val images = listOf(
            R.drawable.bryce_canyon,
            R.drawable.cathedral_rock,
            R.drawable.death_valley,
            R.drawable.fitzgerald_marine_reserve,
            R.drawable.goldengate,
            R.drawable.golden_gate_bridge,
            R.drawable.shipwreck_1,
            R.drawable.shipwreck_2,
            R.drawable.grand_canyon,
            R.drawable.horseshoe_bend,
            R.drawable.muir_beach,
            R.drawable.rainbow_falls
        )
        val text = listOf(
            "Капитан",
            "Попка",
            "Рыжая с Нелей",
            "Просто рыжая",
            "Просто рыжая",
            "Просто рыжая",
            "Просто рыжая",
            "Просто рыжая",
            "Просто рыжая",
            "Просто рыжая",
            "Просто рыжая",
            "Каньён"
        )
        val numImages = images.size
        carousel?.setAdapter(object : Carousel.Adapter {
            override fun count(): Int {
                return numImages
            }

            override fun populate(view: View, index: Int) {
                Log.d("!!!first", "${index} ${view}")
                if (view is ImageView) {
                    view.setImageResource(images[index])
                }
            }

            override fun onNewItem(index: Int) {
                rootElement!!.apply {
                    CoroutineScope(Dispatchers.Main).launch {
                        tvDesk.animation =
                            AnimationUtils.loadAnimation(context, R.anim.alpha_1_2)
                        dela()
                        tvDesk.text = text[index]
                        tvDesk.animation =
                            AnimationUtils.loadAnimation(context, R.anim.alpha_1_1)
                    }
                }
            }
        })
        carousel2?.setAdapter(object : Carousel.Adapter {
            override fun count(): Int {
                return numImages
            }

            override fun populate(view: View, index: Int) {
                Log.d("!!!first", "${index} ${view}")
                if (view is ImageView) {
                    view.setImageResource(images[index])
                }
            }

            override fun onNewItem(index: Int) {
            }
        })

        initClick()
    }

    private suspend fun dela() = withContext(Dispatchers.IO) {
        delay(250)
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
                    navController.navigate(R.id.dogsListFragment)
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
}
