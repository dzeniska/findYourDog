package com.dzenis_ska.findyourdog.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.MenuItemCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.navOptions
import androidx.navigation.ui.setupWithNavController
import com.dzenis_ska.findyourdog.R
import com.dzenis_ska.findyourdog.databinding.ActivityMainBinding
import com.dzenis_ska.findyourdog.remoteModel.firebase.FBAuth
import com.dzenis_ska.findyourdog.ui.utils.CheckNetwork
import com.dzenis_ska.findyourdog.viewModel.BreedViewModel
import com.dzenis_ska.findyourdog.viewModel.BreedViewModelFactory
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@SuppressLint("SetTextI18n")
@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var tvHeaderAcc: TextView
    var navController: NavController? = null
    var menuBreedItem: TextView? = null
    var menuMapItem: TextView? = null
    private var job: Job? = null

    private val fbAuth = FBAuth()
    var rootElement: ActivityMainBinding? = null

    @Inject
    lateinit var factory: BreedViewModelFactory
    private val viewModel: BreedViewModel by viewModels { factory }
    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            Log.d("!!!isClick", "is2")
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(
                    this,
                    resources.getString(R.string.need_permission_for_geolocation),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                isAuth()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setTheme(R.style.AppThemeNoActionBar)
        rootElement = ActivityMainBinding.inflate(layoutInflater)
        setContentView(rootElement!!.root)

        init()
        rootElement!!.navView.setNavigationItemSelectedListener(this)
//        openCloseDrawer()
        viewModel.userUpdate.observe(this, {
            uiUpdateMain(it)
        })
    }


    private fun uiUpdateMain(user: FirebaseUser?) {
        if (user?.email != null) {
            tvHeaderAcc.text =
                """${resources.getString(R.string.nice_to_meet_you)}
                |${user.email}
                """.trimMargin()
        } else {
            tvHeaderAcc.text = resources.getString(R.string.nice_to_meet_you)
        }

    }

    override fun onResume() {
        super.onResume()
        uiUpdateMain(fbAuth.mAuth.currentUser)
    }

    override fun onSupportNavigateUp(): Boolean {
        val back = navController!!.backQueue
        Log.d("!!!fragments", "${back.size}")

        navController!!.backQueue.forEach {
            Log.d("!!!fragments", "${it.destination.label}")
        }

        if(navController!!.backQueue.size > 2){
            onBackPressed()
        } else {
            openDrawer()
        }
        return true
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
        viewModel.liveAdsDataAllMarkers.observe(this, {
            //добавляем счётчик в item menu_drawer
            menuMapItem = MenuItemCompat.getActionView(
                rootElement!!.navView.menu.findItem(R.id.goToMapFr)
            ) as (TextView)
            mapCounter(it.size)
        })
        viewModel.countFavorites()

//        viewModel.selectBreed()

//        viewModel.getAllMarkersForMap(){}

        navController = findNavController(R.id.navHost)

        rootElement!!.apply {
            appBar.toolbar.setupWithNavController(navController!!, drawerLayout)
            setSupportActionBar(appBar.toolbar)

            //убираем затемнение
//            drawerLayout.setScrimColor(Color.TRANSPARENT)

            tvHeaderAcc = navView.getHeaderView(0).findViewById(R.id.tvHeaderAcc)
        }
    }

    //рисуем счётчик
    private fun breedCounter(count: Int) {
        menuBreedItem?.gravity = Gravity.CENTER_VERTICAL
        menuBreedItem?.setTypeface(null, Typeface.BOLD)
        menuBreedItem?.setTextColor(ContextCompat.getColor(this, R.color.white))
        menuBreedItem?.text = "${resources.getString(R.string.plus)}${count}"
    }

    private fun mapCounter(count: Int) {
        menuMapItem?.gravity = Gravity.CENTER_VERTICAL
        menuMapItem?.setTypeface(null, Typeface.BOLD)
        menuMapItem?.setTextColor(ContextCompat.getColor(this, R.color.back_menu_one_breed_color))
        menuMapItem?.text = "${resources.getString(R.string.plus)}${count}"
    }


    @SuppressLint("RestrictedApi")
    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.show_fav -> {
                navigateTo(R.id.dogsListFragment)
                closeDrawer()
                Toast.makeText(applicationContext, resources.getString(R.string.lovers), Toast.LENGTH_SHORT).show()
            }
            R.id.goToMapFr -> {
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
                navigateTo(R.id.loginFragment)
                closeDrawer()
            }

            R.id.developerPage -> {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(resources.getString(R.string.link_page))
                startActivity(intent)
            }

        }
        return true
    }

    override fun onBackPressed() {
        if(rootElement!!.drawerLayout.isOpen)
        closeDrawer()
        else
        super.onBackPressed()
    }

    private fun isAuth() {
        val currUser = fbAuth.mAuth.currentUser
        if (currUser == null) {
            fbAuth.signInAnonymously() {isSignAnon, messAnonSign ->
                if(isSignAnon == true) {
                    navigateTo(R.id.mapsFragment)
                    closeDrawer()
                } else {
                    CheckNetwork.check(this@MainActivity)
                }
                Toast.makeText(this, messAnonSign, Toast.LENGTH_LONG).show()
            }
        } else {
            navigateTo(R.id.mapsFragment)
            closeDrawer()
        }
    }

    private fun closeDrawer() {
        rootElement!!.drawerLayout.closeDrawer(GravityCompat.START)
    }
    private fun openDrawer() {
        rootElement!!.drawerLayout.openDrawer(GravityCompat.START)
    }

    private fun navigateTo(id: Int) {
        var idToBack: Int? = null
        navController?.backQueue?.forEach {
            Log.d("!!!navController", "${it.destination.label}")
            when(it.destination.label) {
                resources.getString(R.string.dogs_list) -> {
                    idToBack = R.id.dogsListFragment
                    return@forEach
                }
                resources.getString(R.string.authFirst) -> {
                    idToBack = R.id.loginFragment
                    return@forEach
                }
                resources.getString(R.string.map) -> {
                    idToBack = R.id.mapsFragment
                    return@forEach
                }
            }
        }

        navController!!.navigate(id, null, navOptions {
            if(idToBack != null){
                popUpTo(idToBack!!) { inclusive = true }
            }
        })
    }

    override fun onDestroy() {
        job = null
        super.onDestroy()
    }

    companion object {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }
}