package com.dzenis_ska.findyourdog.remoteModel.firebase

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.View

import android.widget.Toast
import androidx.fragment.app.Fragment
import com.dzenis_ska.desk.constants.FirebaseAuthConstants
import com.dzenis_ska.findyourdog.ui.fragments.LoginFragment
import com.google.firebase.auth.*
import com.dzenis_ska.findyourdog.R
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class FBAuth(private val fragment: Fragment) {

    val mAuth = Firebase.auth

    fun signUpWithEmail(
        email: String,
        password: String,
        context: Context
    ) {
//        Log.d("!!!userAnDelete", "${mAuth.currentUser?.isAnonymous}")
        try {
            if (mAuth.currentUser?.isAnonymous == true) {
                mAuth.currentUser?.delete()?.addOnCompleteListener {
                    createUserWithEmailAndPassword(email, password, context, true)
                }?.addOnFailureListener {
                    Toast.makeText(
                        context,
                        "signUpWithEmail ${it}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                createUserWithEmailAndPassword(email, password, context, false)
            }
        }catch (e: ExceptionInInitializerError){
            Toast.makeText(
                context,
                "signUpWithEmail ${e}",
                Toast.LENGTH_LONG
            ).show()
        }

    }

    private fun createUserWithEmailAndPassword(email: String, password: String, context: Context, create: Boolean) {
        try {
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        context,
                        context.resources.getString(R.string.reg_up_is_successful),
                        Toast.LENGTH_SHORT
                    ).show()
                    sendEmailVerification(task.result?.user!!, context)
                    if (fragment is LoginFragment) {
                        fragment.uiUpdateMain(task.result?.user!!)
                    }
                } else {
//                    Toast.makeText(context, context.resources.getString(R.string.error_reg), Toast.LENGTH_SHORT).show()
//                Log.d("!!!er", task.exception.toString())
                    if (task.exception is FirebaseAuthUserCollisionException) {
                        val exception = task.exception as FirebaseAuthUserCollisionException
//                    Log.d("!!!erPas", exception.errorCode)
                        if (exception.errorCode == FirebaseAuthConstants.ERROR_EMAIL_ALREADY_IN_USE) {
                            signInWithEmail(email, password, context, create)
                            Toast.makeText(
                                context,
                                FirebaseAuthConstants.ERROR_EMAIL_ALREADY_IN_USE,
                                Toast.LENGTH_SHORT
                            ).show()
//                        Log.d("!!!error", "${FirebaseAuthConstants.ERROR_EMAIL_ALREADY_IN_USE}")
                        }
                    } else if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        if (create) signInAnonimously(null, context) {}
                        val exception = task.exception as FirebaseAuthInvalidCredentialsException
                        if (exception.errorCode == FirebaseAuthConstants.ERROR_INVALID_EMAIL) {
                            Toast.makeText(
                                context,
                                FirebaseAuthConstants.ERROR_INVALID_EMAIL,
                                Toast.LENGTH_SHORT
                            ).show()
                        } else if (exception.errorCode == FirebaseAuthConstants.ERROR_WEAK_PASSWORD) {
                            Toast.makeText(
                                context,
                                FirebaseAuthConstants.ERROR_WEAK_PASSWORD,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    if (task.exception is FirebaseAuthWeakPasswordException) {
                        if (create) signInAnonimously(null, context) {}
                        val exception = task.exception as FirebaseAuthWeakPasswordException
                        if (exception.errorCode == FirebaseAuthConstants.ERROR_WEAK_PASSWORD) {
                            Toast.makeText(
                                context,
                                FirebaseAuthConstants.ERROR_WEAK_PASSWORD,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(
                    context,
                    "createUserWithEmailAndPassword ${it.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }catch (e: ExceptionInInitializerError){
            Toast.makeText(
                context,
                "createUserWithEmailAndPassword ${e}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun signInWithEmail(email: String, password: String, context: Context, create: Boolean) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            try {
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            context,
                            "Вы вошли как ${task.result?.user?.email}",
                            Toast.LENGTH_SHORT
                        ).show()
                        if (fragment is LoginFragment) {
                            fragment.uiUpdateMain(task.result?.user!!)
                        }
                    } else {
                        if (create) signInAnonimously(null, context) {}
//                    Log.d("!!!er", task.exception.toString())
                        if (task.exception is FirebaseAuthInvalidCredentialsException) {
//                        Log.d("!!!er", task.exception.toString())
                            val exception =
                                task.exception as FirebaseAuthInvalidCredentialsException
                            if (exception.errorCode == FirebaseAuthConstants.ERROR_INVALID_EMAIL) {
                                Toast.makeText(
                                    context,
                                    FirebaseAuthConstants.ERROR_INVALID_EMAIL,
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else if (exception.errorCode == FirebaseAuthConstants.ERROR_WRONG_PASSWORD) {
                                if (fragment is LoginFragment) {
                                    fragment.uiReplacePassword()
                                }
                                Toast.makeText(
                                    context,
                                    FirebaseAuthConstants.ERROR_WRONG_PASSWORD,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else if (task.exception is FirebaseAuthInvalidUserException) {
                            val exception = task.exception as FirebaseAuthInvalidUserException
                            if (exception.errorCode == FirebaseAuthConstants.ERROR_USER_NOT_FOUND) {
                                Toast.makeText(
                                    context,
                                    FirebaseAuthConstants.ERROR_USER_NOT_FOUND,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
//                    Toast.makeText(context, context.resources.getString(R.string.error_enter), Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener {
                    Toast.makeText(
                        context,
                        "signInWithEmail ${it.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }catch (e: ExceptionInInitializerError){
                Toast.makeText(
                    context,
                    "signInWithEmail ${e}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun sendEmailVerification(user: FirebaseUser, context: Context) {
        try {
            user.sendEmailVerification().addOnCompleteListener() { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        context,
                        fragment.resources.getString(R.string.email_add),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        context,
                        context.resources.getString(R.string.email_no_add),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }.addOnFailureListener {
                Toast.makeText(
                    context,
                    "sendEmailVerification ${it.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }catch (e: ExceptionInInitializerError){
            Toast.makeText(
                context,
                "E sendEmailVerification ${e}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun signInAnonimously(view: View?,context: Context?, callback: (isS: Boolean?)-> Unit) {
        try {
            mAuth.signInAnonymously().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true)
                    Log.d("!!!userFBAuth", "${task.result.user?.uid}")
                    if (fragment is LoginFragment) {
                        fragment.showElements(true)
                    }
                } else {
                    callback(false)
                }
            }.addOnFailureListener {
                Toast.makeText(
                    context,
                    "signInAnonimously ${it.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }catch (e: ExceptionInInitializerError){
            Toast.makeText(
                context,
                "E signInAnonimously ${e}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}


