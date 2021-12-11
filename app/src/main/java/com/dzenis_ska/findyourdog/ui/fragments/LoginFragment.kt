package com.dzenis_ska.findyourdog.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.dzenis_ska.desk.constants.FirebaseAuthConstants
import com.dzenis_ska.findyourdog.R
import com.dzenis_ska.findyourdog.databinding.FragmentLoginBinding
import com.dzenis_ska.findyourdog.remoteModel.firebase.FBAuth
import com.dzenis_ska.findyourdog.ui.utils.InitBackStack
import com.dzenis_ska.findyourdog.viewModel.BreedViewModel
import com.google.firebase.auth.FirebaseUser

class LoginFragment : Fragment(){

    val viewModel: BreedViewModel by activityViewModels()
    var rootElement: FragmentLoginBinding? = null
    private val fbAuth = FBAuth()
    var navController: NavController? = null

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
        val rootElement = FragmentLoginBinding.inflate(inflater)
        this.rootElement = rootElement
        return rootElement.root
    }

    override fun onResume() {
        super.onResume()
        currentUser(fbAuth.mAuth.currentUser)
        Log.d("!!!onResumeLF", "onResumeLF")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("!!!onViewCreatedLf", "onViewCreatedLf")
        init()
        onClick()
        initBackStack()
    }

    fun showElements(bool: Boolean) {
        rootElement?.apply {
            if (bool) {
                imgButtonEnter.setImageResource(R.drawable.world_map_mini)
                tvEnter.text = "Карта"
            } else {
                imgButtonEnter.setImageResource(R.drawable.ic_in_white)
                tvEnter.text = "Вход"
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun currentUser(currentUser: FirebaseUser?) {
        rootElement?.apply {
            if (currentUser == null || currentUser.isAnonymous) {
                isEditEnable(false)
                tvRegIn.text = "Привет, Незнакомец!)"
            } else if (!currentUser.isAnonymous && currentUser.isEmailVerified) {
                isEditEnable(true)
                groupForgot.visibility = View.GONE
                tvRegIn.text = """Привет
                    |${currentUser.email}
                """.trimMargin()
                showElements(true)
            } else if (!currentUser.isAnonymous) {
                isEditEnable(true)
                groupForgot.visibility = View.GONE
                Log.d("!!!userLFisNoAnonimous", currentUser.uid)
                tvRegIn.text = """Привет
                    |${currentUser.email}
                """.trimMargin()
//                showElements(false)
            }
        }
    }

    private fun onClick() {
        Log.d("!!!onClickLF", "onResumeLF")
        rootElement?.apply {
            imgButtonEnter.setOnClickListener(enterEmailAndPassword(fbAuth))
            imgButtonExit.setOnClickListener {
                fbAuth.signOut()
                groupForgot.visibility = View.GONE
                uiUpdateMain(null)
                showElements(false)
                viewModel.uiUpdateMain(null)
            }
            imgButtonForgot.setOnClickListener() {
                setOnClickResetPassword()
            }
        }
    }

    private fun init() {
        navController = findNavController()
        rootElement?.apply {
            edEmail.apply {
                afterTextChanged {
                    if (it.length > 1) {
                        if (!it.contains("@") || !it.contains(".")) {
                            edEmail.error = "@, ."
                        }
                    }
                }
            }
            edPassword.apply {
                afterTextChanged {
                    if (it.length < 6) {
                        edPassword.error = "не менее 6 символов"
                    }
                }
            }
        }
    }

    private fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                afterTextChanged.invoke(editable.toString())
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })
    }

    fun uiUpdateMain(user: FirebaseUser?) {
        rootElement?.apply {
            viewModel.uiUpdateMain(user)
            tvRegIn.animation = AnimationUtils.loadAnimation(context, R.anim.alpha_replace_user_up)
            tvRegIn.visibility = View.VISIBLE
            currentUser(user)
        }
    }

    fun uiReplacePassword() {
        rootElement?.apply {
            groupForgot.visibility = View.VISIBLE
        }
    }

    private fun setOnClickResetPassword() {
        rootElement?.apply {
            if (edEmail.text.isNotEmpty()) {
                fbAuth.mAuth.sendPasswordResetEmail(edEmail.text.toString())
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                activity,
                                R.string.email_reset_password_was_send,
                                Toast.LENGTH_LONG
                            ).show()
                            groupForgot.visibility = View.GONE
                        } else {
                            Toast.makeText(
                                activity,
                                R.string.email_reset_password_was_not_send,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(activity, R.string.fill_the_email, Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun isEditEnable(edit:Boolean) = with(rootElement!!){
        tvReplaseUser.isVisible = edit
        edEmail.isEnabled = !edit
        edPassword.isEnabled = !edit
    }

    private fun enterEmailAndPassword(fbAuth: FBAuth): View.OnClickListener {
        return View.OnClickListener {
            rootElement?.apply {
                if (tvEnter.text == "Карта") {
                    if (ContextCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        requestPermissions.launch(permissions)
                    } else {
                        navController!!.navigate(R.id.mapsFragment, null, navOptions {
                            popUpTo(R.id.mapsFragment){
                                inclusive = true
                            }
                        })
                    }
                } else {
                    rootElement.apply {
                        imgInUp.animation = AnimationUtils.loadAnimation(context, R.anim.rotation)
                        tvRegIn.animation =
                            AnimationUtils.loadAnimation(context, R.anim.alpha_replace_user_down)
                        tvRegIn.visibility = View.GONE
                        val email = edEmail.text.toString()
                        val pass = edPassword.text.toString()
                        if (email.isNotEmpty() && pass.isNotEmpty()) {
                            if (!email.contains('@') || !email.contains('.')) {
                                Toast.makeText(
                                    context,
                                    "Поле Email должно включать символы '@' и '.'",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else if (pass.length < 6) {
                                Toast.makeText(
                                    context,
                                    "Пароль должен содержать не менее 6 символов",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                signInWithEmail(email, pass)
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "Вы забыли ввести Email или Password",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }
    }


    private fun signInWithEmail(email: String, pass: String) {
        fbAuth.signInWithEmail(email, pass){isSign, messSign, fbUser ->
            uiUpdateMain(fbUser)
            val isEmailVer = fbAuth.isEmailVerified(fbUser) ?: false
            if(isSign == true && !isEmailVer) Toast.makeText(context, resources.getString(R.string.check_email), Toast.LENGTH_LONG).show()
            showElements(isEmailVer)
            when (messSign){
                FirebaseAuthConstants.ERROR_WRONG_PASSWORD -> uiReplacePassword()
                FirebaseAuthConstants.LUCKY_CREATOR -> Toast.makeText(context, resources.getString(R.string.created_account), Toast.LENGTH_LONG).show()
            }

            if(isSign == false){
                Log.d("!!!signInWithEmail", "${messSign}")
//                Toast.makeText(context, messSign, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun isAuth() {
        navController!!.navigate(R.id.mapsFragment, null, navOptions {
            popUpTo(R.id.mapsFragment){
                inclusive = true
            }
        })
    }

    override fun onDestroyView() {
        Log.d("!!!onDestroyViewLf", "onDestroyViewLf")
        rootElement = null
        super.onDestroyView()
    }

    companion object {
        private val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }
    @SuppressLint("RestrictedApi")
    private fun initBackStack() {
        navController?.let { InitBackStack.initBackStack(it) }
//        val fList = navController?.backStack
//        fList?.forEach {
//            Log.d("!!!frLF", "${it.destination.label}")
//        }
    }
}