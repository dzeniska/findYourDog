package com.dzenis_ska.findyourdog.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context

import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts

import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat

import androidx.core.view.GravityCompat
import androidx.core.view.MenuItemCompat
import androidx.lifecycle.Observer

import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.dzenis_ska.findyourdog.R
import com.dzenis_ska.findyourdog.databinding.ActivityMainBinding
import com.dzenis_ska.findyourdog.remoteModel.firebase.FBAuth
import com.dzenis_ska.findyourdog.ui.fragments.LoginFragment
import com.dzenis_ska.findyourdog.ui.utils.CheckNetwork
import com.dzenis_ska.findyourdog.viewModel.BreedViewModel
import com.dzenis_ska.findyourdog.viewModel.BreedViewModelFactory
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint
import io.ak1.pix.helpers.PixBus

import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var tvHeaderAcc: TextView
    var navController: NavController? = null
    var menuBreedItem: TextView? = null
    var menuMapItem: TextView? = null

    //    private var optionsList: Map<String, List<String>> = mapOf()
    private val fragmentLogin = LoginFragment()
    private val fbAuth = FBAuth(fragmentLogin)
    var rootElement: ActivityMainBinding? = null
//    var isClick: Boolean = true
//    private val check: CheckNetwork = CheckNetwork()

    @Inject
    lateinit var factory: BreedViewModelFactory
    private val viewModel: BreedViewModel by viewModels { factory }
    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            Log.d("!!!isClick", "is2")
            isAuth()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppThemeNoActionBar)
        rootElement = ActivityMainBinding.inflate(layoutInflater)
        setContentView(rootElement!!.root)

//        android:theme="@style/AppTheme"
//        android:theme="@style/AppTheme.NoActionBar">

        init()
//        getPermission()
        rootElement!!.navView.setNavigationItemSelectedListener(this)
        openCloseDrawer()
        viewModel.userUpdate.observe(this, Observer {
            uiUpdateMain(it)
//            Log.d("!!!", "$it")
        })
    }

    private fun uiUpdateMain(user: FirebaseUser?) {
        if(user?.email != null) {
            tvHeaderAcc.text = """Рады видеть Вас
                |${user?.email}
            """.trimMargin()
        }else{
            tvHeaderAcc.text = "Рады видеть Вас"
        }

    }

    override fun onResume() {
        super.onResume()
        uiUpdateMain(fbAuth.mAuth.currentUser)
    }

    private fun openCloseDrawer() {
        //работа при откр-закр drawer

        val toggle: ActionBarDrawerToggle = object : ActionBarDrawerToggle(
            this, rootElement!!.drawerLayout, rootElement!!.appBar.toolbar, android.R.string.yes, android.R.string.no
        ) {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                super.onDrawerSlide(drawerView, slideOffset)
                val slideX = drawerView.width * slideOffset
                rootElement!!.appBar.apptool.alpha = (1 - slideOffset)
//                constraintLayout.visibility = View.VISIBLE
                    rootElement!!.appBar.introMain.constraintLayout.setTranslationX(slideX)
                    rootElement!!.appBar.introMain.constraintLayout.setScaleX(1 - slideOffset)

                // constraintLayout.setScaleY(1 - slideOffset)
            }
        }
        rootElement!!.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

    }

    private fun init() {


//        val remoteModel = RemoteModel()
//         val localModel = LocalModel(this)
//         val repository = Repository(remoteModel, localModel)
//         val factory = BreedViewModelFactory(repository)
//        viewModel = ViewModelProvider(this, factory).get(BreedViewModel::class.java)

//        viewModel.breedLive.value = mutableListOf()

        viewModel.breedFavLive.observe(this, Observer {
            //добавляем счётчик в item menu_drawer
            menuBreedItem = MenuItemCompat.getActionView(
                rootElement!!.navView.menu.findItem(R.id.show_fav)
            ) as (TextView)
            breedCounter(it.size)
        })
        viewModel.liveAdsDataAllShelter.observe(this, {
            //добавляем счётчик в item menu_drawer
            menuMapItem = MenuItemCompat.getActionView(
                rootElement!!.navView.menu.findItem(R.id.mapFr)
            ) as (TextView)
            mapCounter(it.size)
        })
        viewModel.countFavorites()

        viewModel.selectBreed()

        viewModel.getAllAds()

        navController = findNavController(R.id.navHost)

        rootElement!!.apply {
            appBar.toolbar.setupWithNavController(navController!!, drawerLayout)
            //nav_view.setupWithNavController(navController)
            setSupportActionBar(appBar.toolbar)

            //убираем затемнение
            drawerLayout.setScrimColor(Color.TRANSPARENT)

            //открываем drawer
//        drawerLayout.openDrawer(GravityCompat.START)
            tvHeaderAcc = navView.getHeaderView(0).findViewById(R.id.tvHeaderAcc)
        }
    }

    //рисуем счётчик

    private fun breedCounter(count: Int) {
        menuBreedItem?.gravity = Gravity.CENTER_VERTICAL
        menuBreedItem?.setTypeface(null, Typeface.BOLD)
        menuBreedItem?.setTextColor(ContextCompat.getColor(this, R.color.white))
        menuBreedItem?.text = "+$count"
    }

    private fun mapCounter(count: Int) {
        menuMapItem?.gravity = Gravity.CENTER_VERTICAL
        menuMapItem?.setTypeface(null, Typeface.BOLD)
        menuMapItem?.setTextColor(ContextCompat.getColor(this, R.color.back_menu_one_breed_color))
        menuMapItem?.text = "+${count}"
    }


    override fun onBackPressed() {
        Log.d("!!!bakPressed", "${viewModel.backPressed}")
        when (viewModel.backPressed) {
            0 -> {
                super.onBackPressed()
            }
            2 -> {
                PixBus.onBackPressedEvent()
                viewModel.backPressed--
            }
            1 -> {
                val fList = supportFragmentManager.fragments
                fList.forEach { frag ->
                    if (frag.toString().startsWith("PixFragment")) {
                        supportFragmentManager.beginTransaction().remove(frag).commit()
                    }
                }
                viewModel.backPressed--
            }
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
//            navController.popBackStack()

        when (item.itemId) {
            R.id.show_fav -> {
                navController!!.popBackStack()
                navController!!.navigate(R.id.dogsListFragment)
                closeDrawer()
                Toast.makeText(applicationContext, "Любимчики", Toast.LENGTH_SHORT).show()
            }
            R.id.mapFr -> {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissions.launch(permissions)
                } else {
                    isAuth()
                }
            }
            R.id.auth -> {
                navController!!.popBackStack()
                navController!!.navigate(R.id.loginFragment)
                closeDrawer()
            }
        }
        return true
    }
    private fun isAuth(){
        Log.d("!!!isClick", "is")
        if (fbAuth.mAuth.currentUser == null) {
            fbAuth.signInAnonimously(null) {
                if (it == true) {
                    navController!!.popBackStack()
                    navController!!.navigate(R.id.mapsFragment)
                    closeDrawer()
                }else{
                    CheckNetwork.check(this)
                }
            }
        } else {
            navController!!.popBackStack()
            navController!!.navigate(R.id.mapsFragment)
            closeDrawer()
        }
    }

    private fun closeDrawer() {
        rootElement!!.drawerLayout.closeDrawer(GravityCompat.START)
    }

    companion object {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }
}