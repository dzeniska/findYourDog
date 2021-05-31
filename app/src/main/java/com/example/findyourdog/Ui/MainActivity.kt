package com.example.findyourdog.Ui

import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat

import androidx.core.view.GravityCompat
import androidx.core.view.MenuItemCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.findyourdog.R
import com.example.findyourdog.Ui.fragments.LoginFragment
import com.example.findyourdog.ViewModel.BreedViewModel
import com.example.findyourdog.ViewModel.BreedViewModelFactory
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_favorites.*
import kotlinx.android.synthetic.main.fragment_first.constraintLayout
import kotlinx.android.synthetic.main.header_main_menu.*
import kotlinx.android.synthetic.main.intro_main.*
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var tvHeaderAcc: TextView
    lateinit var navController: NavController
    lateinit var mSlideshowTextView: TextView
    lateinit var viewModel: BreedViewModel

    private var optionsList: Map<String, List<String>> = mapOf()
    val mAuth = FirebaseAuth.getInstance()

    @Inject
    lateinit var factory: BreedViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()

        nav_view.setNavigationItemSelectedListener(this)

        openCloseDrawer()

        viewModel.userUpdate.observe(this, Observer{
            uiUpdateMain(it)
            Log.d("!!!", "$it")
        })

    }
    fun uiUpdateMain(user: FirebaseUser?) {
        Log.d("!!!", user.toString())
        tvHeaderAcc.text = if (user == null) {
            resources.getString(R.string.not_reg)
//            resources.getString(R.string.hello)
        } else {
            """Рады видеть Вас
                |${user.email}
            """.trimMargin()
        }
    }
    override fun onResume() {
        super.onResume()
        uiUpdateMain(mAuth.currentUser)
    }

    fun openCloseDrawer() {
        //работа при откр-закр drawer
        val toggle: ActionBarDrawerToggle = object : ActionBarDrawerToggle(
            this, drawerLayout, toolbar, android.R.string.yes, android.R.string.no
        ) {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                super.onDrawerSlide(drawerView, slideOffset)
                val slideX = drawerView.width * slideOffset
                apptool.alpha = (1 - slideOffset)
                constraintLayout.visibility = View.VISIBLE
                constraintLayout.setTranslationX(slideX)
                constraintLayout.setScaleX(1 - slideOffset)
                // constraintLayout.setScaleY(1 - slideOffset)
            }
        }
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

    }

    fun init() {

//        val remoteModel = RemoteModel()
//         val localModel = LocalModel(this)
//         val repository = Repository(remoteModel, localModel)
//         val factory = BreedViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(BreedViewModel::class.java)

        viewModel.breedLive.setValue(mutableListOf())
        viewModel.breedFavLive.observe(this, Observer {
            //добавляем счётчик в item menu_drawer
            mSlideshowTextView = MenuItemCompat.getActionView(
                nav_view.menu.findItem(R.id.show_fav)
            ) as (TextView)
            initializeCountDrawer(it.size)
        })
        viewModel.countFavorites()

        viewModel.selectBreed()

        navController = findNavController(navHost)
        toolbar.setupWithNavController(navController, drawerLayout)
        //nav_view.setupWithNavController(navController)
        setSupportActionBar(toolbar)

        //убираем затемнение
        drawerLayout.setScrimColor(Color.TRANSPARENT)

        //открываем drawer
        drawerLayout.openDrawer(GravityCompat.START)
        tvHeaderAcc = nav_view.getHeaderView(0).findViewById(R.id.tvHeaderAcc)
    }

    //рисуем счётчик
    fun initializeCountDrawer(count: Int) {
        mSlideshowTextView.setGravity(Gravity.CENTER_VERTICAL)
        mSlideshowTextView.setTypeface(null, Typeface.BOLD)
        mSlideshowTextView.setTextColor(ContextCompat.getColor(this, R.color.counter))
        mSlideshowTextView.text = "${count}"
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed();
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.dogs_list -> {
                intent
                navController.navigate(R.id.dogsListFragment)
                closeDrawer()
                Toast.makeText(applicationContext, "Просмотр списка", Toast.LENGTH_SHORT).show()
            }
//            R.id.add_dog_item -> {
//                navController.navigate(R.id.addDogFragment)
//                closeDrawer()
//                Toast.makeText(applicationContext, "Добавьте пёса", Toast.LENGTH_LONG).show()
//            }
            R.id.show_fav -> {
                navController.navigate(R.id.favoritesFragment)
                closeDrawer()
                Toast.makeText(applicationContext, "Любимчики", Toast.LENGTH_SHORT).show()
            }
            R.id.auth -> {
                viewModel.signUpIn(0)

                navController.navigate(R.id.loginFragment)
                closeDrawer()
//                Toast.makeText(applicationContext, "loginFragment", Toast.LENGTH_LONG).show()
            }
            R.id.enter -> {
                viewModel.signUpIn(1)
                navController.navigate(R.id.loginFragment)
                closeDrawer()
//                Toast.makeText(applicationContext, "loginFragment", Toast.LENGTH_LONG).show()
            }
            R.id.exit -> {
                uiUpdateMain(null)
                mAuth.signOut()
            }
        }
        return true
    }

    private fun closeDrawer(){
        drawerLayout.closeDrawer(GravityCompat.START)
    }
}