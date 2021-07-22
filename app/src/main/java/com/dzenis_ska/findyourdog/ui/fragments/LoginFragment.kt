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
import com.dzenis_ska.findyourdog.R
import com.dzenis_ska.findyourdog.databinding.FragmentLoginBinding
import com.dzenis_ska.findyourdog.remoteModel.firebase.AuthInterface
import com.dzenis_ska.findyourdog.remoteModel.firebase.FBAuth
import com.dzenis_ska.findyourdog.ui.MainActivity
import com.dzenis_ska.findyourdog.viewModel.BreedViewModel
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.fragment_login.*


class LoginFragment() : Fragment(), AuthInterface {

    val viewModel: BreedViewModel by activityViewModels()
    var rootElement: FragmentLoginBinding? = null
    private val fbAuth = FBAuth(this)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootElement = FragmentLoginBinding.inflate(inflater)
        // Inflate the layout for this fragment
        return rootElement!!.root
    }

    override fun onResume() {
        super.onResume()
        currentUser(fbAuth.mAuth.currentUser)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.userUpdate.observe(viewLifecycleOwner, {
            currentUser(fbAuth.mAuth.currentUser)
//            uiUpdateMain(it)
//            Log.d("!!!", "$it")
        })

        init()
        currentUser(fbAuth.mAuth.currentUser)
        onClick()

    }

    @SuppressLint("SetTextI18n")
    private fun currentUser(currentUser: FirebaseUser?) {

        rootElement.apply {
            if (currentUser == null) {
                tvRegIn.text = resources.getString(R.string.auth_ic)
                uiUpdateMain(currentUser)
            } else {
                uiUpdateMain(currentUser)
                tvRegIn.text = """Привет
                    |${currentUser.email}
                """.trimMargin()
            }
        }
    }


    private fun onClick() = with(rootElement) {
//        val scope = CoroutineScope(Dispatchers.Main)

        imgButtonEnter.setOnClickListener(enterEmailAndPassword(fbAuth))

        imgButtonExit.setOnClickListener {
            fbAuth.mAuth.signOut()
            currentUser(null)
        }

        imgButtonForgot.setOnClickListener() {
            setOnClickResetPassword()
        }
    }


    private fun init() = with(rootElement) {

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
        viewModel.uiUpdateMain(user)
    }

    override fun uiReplacePassword() {
        rootElement!!.tvForgotPas.visibility = View.VISIBLE
        rootElement!!.imgButtonForgot.visibility = View.VISIBLE
    }

    private fun setOnClickResetPassword() {
        if (edEmail.text.isNotEmpty()) {
            fbAuth.mAuth.sendPasswordResetEmail(edEmail.text.toString())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            activity,
                            R.string.email_reset_password_was_send,
                            Toast.LENGTH_LONG
                        ).show()
                        rootElement!!.tvForgotPas.visibility = View.GONE
                        rootElement!!.imgButtonForgot.visibility = View.GONE
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

    private fun enterEmailAndPassword(fbAuth: FBAuth): View.OnClickListener {
        return View.OnClickListener {

            rootElement.apply {
                imgInUp.animation = AnimationUtils.loadAnimation(context, R.anim.rotation)
                tvRegIn.animation = AnimationUtils.loadAnimation(context, R.anim.alpha_add_photo)

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

    override fun onDestroy() {
        rootElement = null
        super.onDestroy()
    }
}