package com.dzenis_ska.findyourdog.ui.fragments

import android.annotation.SuppressLint
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
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.dzenis_ska.findyourdog.R
import com.dzenis_ska.findyourdog.databinding.FragmentLoginBinding
import com.dzenis_ska.findyourdog.remoteModel.firebase.AuthInterface
import com.dzenis_ska.findyourdog.remoteModel.firebase.FBAuth
import com.dzenis_ska.findyourdog.ui.MainActivity
import com.dzenis_ska.findyourdog.viewModel.BreedViewModel
import com.google.firebase.auth.FirebaseUser


class LoginFragment : Fragment(), AuthInterface {

    val viewModel: BreedViewModel by activityViewModels()
    var rootElement: FragmentLoginBinding? = null
    private val fbAuth = FBAuth(this)
    lateinit var navController: NavController


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootElement = FragmentLoginBinding.inflate(inflater)
        this.rootElement = rootElement
        // Inflate the layout for this fragment
        return rootElement.root
    }

    override fun onResume() {
        super.onResume()
        currentUser(fbAuth.mAuth.currentUser)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        onClick()
    }

    fun showElements(bool: Boolean) {
        rootElement?.apply {
            if (bool) {
                imgButtonEnter.setImageResource(R.drawable.ic_in_white)
                tvEnter.text = "Вход"
            } else {
                imgButtonEnter.setImageResource(R.drawable.world_map)
                tvEnter.text = "Карта"
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun currentUser(currentUser: FirebaseUser?) {

        rootElement?.apply {
            if (currentUser == null) {
                fbAuth.signInAnonimously(context as MainActivity, object : FBAuth.Listener {
                    override fun onComplete() {
                        tvRegIn.text = "Вы вошли как Гость"
                        Log.d("!!!userLFonComplete", "${currentUser?.uid}")
                    }
                })
            } else if (currentUser.isAnonymous) {
                Log.d("!!!userLFisAnonimous", "${currentUser?.uid}")
                tvRegIn.text = "Привет, Незнакомец!)"
            } else if (!currentUser.isAnonymous && currentUser.isEmailVerified){
                tvForgotPas.visibility = View.GONE
                imgButtonForgot.visibility = View.GONE
                Log.d("!!!userLFisNoAnonimous", "${currentUser?.uid}")
                tvRegIn.text = """Привет
                    |${currentUser.email}
                """.trimMargin()
                showElements(false)
            } else if (!currentUser.isAnonymous) {
                tvForgotPas.visibility = View.GONE
                imgButtonForgot.visibility = View.GONE
                Log.d("!!!userLFisNoAnonimous", "${currentUser?.uid}")
                tvRegIn.text = """Привет
                    |${currentUser.email}
                """.trimMargin()
//                showElements(false)
            }
        }
    }

    private fun onClick() {
        rootElement?.apply {
            imgButtonEnter.setOnClickListener(enterEmailAndPassword(fbAuth))
            imgButtonExit.setOnClickListener {
                if (fbAuth.mAuth.currentUser?.email != null) {
                    fbAuth.mAuth.signOut()
                    tvForgotPas.visibility = View.GONE
                    imgButtonForgot.visibility = View.GONE
                    currentUser(null)
                }
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

    override fun uiReplacePassword() {
        rootElement?.apply {
            tvForgotPas.visibility = View.VISIBLE
            imgButtonForgot.visibility = View.VISIBLE
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
                            tvForgotPas.visibility = View.GONE
                            imgButtonForgot.visibility = View.GONE
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

    private fun enterEmailAndPassword(fbAuth: FBAuth): View.OnClickListener {
        return View.OnClickListener {
            rootElement?.apply {
                if (tvEnter.text == "Карта") {
                    navController.navigate(R.id.mapsFragment)
                } else {
                    rootElement.apply {
                        imgInUp.animation = AnimationUtils.loadAnimation(context, R.anim.rotation)
                        tvRegIn.animation =
                            AnimationUtils.loadAnimation(context, R.anim.alpha_replace_user_down)
                        tvRegIn.visibility = View.GONE

                        if (edEmail.text.isNotEmpty() && edPassword.text.isNotEmpty()) {
                            if (!edEmail.text.contains('@') || !edEmail.text.contains('.')) {
                                Toast.makeText(
                                    context,
                                    "Поле Email должно включать символы '@' и '.'",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else if (edPassword.text.toString().length < 6) {
                                Toast.makeText(
                                    context,
                                    "Пароль должен содержать не менее 6 символов",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                fbAuth.signUpWithEmail(
                                    edEmail.text.toString(),
                                    edPassword.text.toString(),
                                    context as MainActivity
                                )
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "Вы забыли ввести Email или Password",
                                Toast.LENGTH_LONG
                            )
                                .show()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        rootElement = null
        super.onDestroyView()
    }
}