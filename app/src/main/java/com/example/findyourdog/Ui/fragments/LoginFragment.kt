package com.example.findyourdog.Ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.findyourdog.R
import com.example.findyourdog.RemoteModel.firebase.FBAuth
import com.example.findyourdog.Ui.MainActivity
import com.example.findyourdog.ViewModel.BreedViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.fragment_login.*


class LoginFragment(): Fragment() {

    val mAuth = FirebaseAuth.getInstance()
    lateinit var viewModel: BreedViewModel
    val fbAuth = FBAuth(this)
    var signUpIn: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(activity as MainActivity).get(BreedViewModel::class.java)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel.signUpInValue.observe(viewLifecycleOwner, Observer {
                signUpIn = it

        })
        init()




        floatBtnSendEmailAndPassword.setOnClickListener(){
            if(edEmail.text.isNotEmpty() && edPassword.text.isNotEmpty()){
                if(signUpIn == 0){
                    fbAuth.signUpWithEmail(edEmail.text.toString(), edPassword.text.toString())
                }else{
                    fbAuth.signInWithEmail(edEmail.text.toString(), edPassword.text.toString())
                }

            }
        }



    }

    private fun init() {
        edEmail.apply {
            afterTextChanged {
                if(it.length >5) {
                    if (!it.contains("@")) {
                        edEmail.error = "@"
                    }
                }
            }
        }
        edPassword.apply {
            afterTextChanged {
                if(it.length < 6){
                    edPassword.error = "не менее 6 символов"
                }
            }
        }
    }

    fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
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
}