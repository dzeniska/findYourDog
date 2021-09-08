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

import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import androidx.core.view.GravityCompat
import androidx.core.view.MenuItemCompat
import androidx.lifecycle.Observer

import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.dzenis_ska.findyourdog.R
import com.dzenis_ska.findyourdog.databinding.ActivityMainBinding
import com.dzenis_ska.findyourdog.remoteModel.firebase.FBAuth
import com.dzenis_ska.findyourdog.ui.fragments.LoginFragment
import com.dzenis_ska.findyourdog.viewModel.BreedViewModel
import com.dzenis_ska.findyourdog.viewModel.BreedViewModelFactory
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint

import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val locationPermissionCode = 2
    private lateinit var tvHeaderAcc: TextView
    lateinit var navController: NavController
    lateinit var mSlideshowTextView: TextView

    //    private var optionsList: Map<String, List<String>> = mapOf()
    val fragment = LoginFragment()
    val fbAuth = FBAuth(fragment)

    var rootElement: ActivityMainBinding? = null

    @Inject
    lateinit var factory: BreedViewModelFactory
    private val viewModel: BreedViewModel by viewModels {
        factory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rootElement = ActivityMainBinding.inflate(layoutInflater)

//        return rootElement.root
        setContentView(rootElement!!.root )


        init()

        getPermission()
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
            mSlideshowTextView = MenuItemCompat.getActionView(
                rootElement!!.navView.menu.findItem(R.id.show_fav)
            ) as (TextView)
            initializeCountDrawer(it.size)
        })
        viewModel.countFavorites()

        viewModel.selectBreed()

//        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment
        navController = findNavController(R.id.navHost)
//        navController = findNavController(navHost)
        rootElement!!.apply {
            appBar.toolbar.setupWithNavController(navController, drawerLayout)
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
    private fun initializeCountDrawer(count: Int) {
        mSlideshowTextView.setGravity(Gravity.CENTER_VERTICAL)
        mSlideshowTextView.setTypeface(null, Typeface.BOLD)
        mSlideshowTextView.setTextColor(ContextCompat.getColor(this, R.color.counter))
        mSlideshowTextView.text = "${count}"
    }

    override fun onBackPressed() = with(rootElement!!) {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed();
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
//            navController.popBackStack()

        when (item.itemId) {
            R.id.show_fav -> {
                navController.popBackStack()
                navController.navigate(R.id.dogsListFragment)
                closeDrawer()
                Toast.makeText(applicationContext, "Любимчики", Toast.LENGTH_SHORT).show()
            }
            R.id.mapFr -> {
                navController.popBackStack()
                navController.navigate(R.id.mapsFragment)
                closeDrawer()
            }
            R.id.auth -> {
                navController.popBackStack()
                navController.navigate(R.id.loginFragment)
                closeDrawer()
//                Toast.makeText(applicationContext, "loginFragment", Toast.LENGTH_LONG).show()
            }
        }
        return true
    }

    private fun closeDrawer() {
        rootElement!!.drawerLayout.closeDrawer(GravityCompat.START)
    }

    fun checkNetwork() {
        //проверка на доступ к сети
        val cManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//getNetworkCapabilities -
        val info = cManager.getNetworkCapabilities(cManager.activeNetwork)
        if (info == null) {
            Toast.makeText(this, "NO Network!!!", Toast.LENGTH_LONG).show()
        } else if (info.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || info.hasTransport(
                NetworkCapabilities.TRANSPORT_WIFI
            )
        ) {
            Toast.makeText(this, "Network available", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "NO Network!!!", Toast.LENGTH_LONG).show()
        }
    }

    private fun getPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionCode
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}