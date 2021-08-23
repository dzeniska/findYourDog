package com.dzenis_ska.findyourdog.ui

import android.Manifest
import android.content.Context

import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

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
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.dzenis_ska.findyourdog.R
import com.dzenis_ska.findyourdog.remoteModel.firebase.FBAuth
import com.dzenis_ska.findyourdog.ui.fragments.LoginFragment
import com.dzenis_ska.findyourdog.viewModel.BreedViewModel
import com.dzenis_ska.findyourdog.viewModel.BreedViewModelFactory
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_first.constraintLayout
import kotlinx.android.synthetic.main.intro_main.*
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

    @Inject
    lateinit var factory: BreedViewModelFactory
    private val viewModel: BreedViewModel by viewModels {
        factory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        init()

        getPermission()

        nav_view.setNavigationItemSelectedListener(this)

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
    private fun initializeCountDrawer(count: Int) {
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
                navController.navigate(R.id.dogsListFragment)
                closeDrawer()
            }
            R.id.show_fav -> {
                navController.navigate(R.id.favoritesFragment)
                closeDrawer()
                Toast.makeText(applicationContext, "Любимчики", Toast.LENGTH_SHORT).show()
            }
            R.id.mapFr -> {

                navController.navigate(R.id.mapsFragment)
                closeDrawer()
            }
            R.id.auth -> {
                navController.navigate(R.id.loginFragment)
                closeDrawer()
//                Toast.makeText(applicationContext, "loginFragment", Toast.LENGTH_LONG).show()
            }
        }
        return true
    }

    private fun closeDrawer() {
        drawerLayout.closeDrawer(GravityCompat.START)
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