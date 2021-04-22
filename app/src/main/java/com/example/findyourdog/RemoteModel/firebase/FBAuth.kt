package com.example.findyourdog.RemoteModel.firebase

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.dzenis_ska.desk.constants.FirebaseAuthConstants
import com.example.findyourdog.Ui.MainActivity
import com.example.findyourdog.Ui.fragments.LoginFragment
import com.google.firebase.auth.*

class FBAuth(private val fragment: Fragment) {

    val mAuth = FirebaseAuth.getInstance()
    var error = "no error"


    fun signUpWithEmail(email: String, password: String) {
//        Log.d("!!!auth", "auth")
        if (email.isNotEmpty() && password.length > 5) {
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
//                    Log.d("!!!auth", "${task.result?.user}")
//                    Toast.makeText(activity, "context.resources.getString(R.id.)", Toast.LENGTH_SHORT).show()
                    sendEmailVerification(task.result?.user!!)
                    if(fragment is LoginFragment){
                        fragment.uiUpdateMain(task.result?.user)
                    }

//                    uiUpdate(task.result?.user)
                } else {
//                    Toast.makeText(activity, activity.resources.getString(R.string.sign_up_erroro), Toast.LENGTH_SHORT).show()
                    Log.d("!!!er", task.exception.toString())
                    if (task.exception is FirebaseAuthUserCollisionException) {
                        val exception = task.exception as FirebaseAuthUserCollisionException
                        Log.d("!!!erPas", exception.errorCode)
                        if (exception.errorCode == FirebaseAuthConstants.ERROR_EMAIL_ALREADY_IN_USE) {
//                            Toast.makeText(activity as MainActivity, FirebaseAuthConstants.ERROR_EMAIL_ALREADY_IN_USE, Toast.LENGTH_SHORT).show()
                            //Link Email
//                            linkEmailToG(email, password)
                        }
                    } else if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        val exception = task.exception as FirebaseAuthInvalidCredentialsException
                        if (exception.errorCode == FirebaseAuthConstants.ERROR_INVALID_EMAIL) {
//                            Toast.makeText(activity, FirebaseAuthConstants.ERROR_INVALID_EMAIL, Toast.LENGTH_SHORT).show()
                        } else if (exception.errorCode == FirebaseAuthConstants.ERROR_WEAK_PASSWORD) {
//                            Toast.makeText(activity, FirebaseAuthConstants.ERROR_WEAK_PASSWORD, Toast.LENGTH_SHORT).show()
                        }
                    }
                    if (task.exception is FirebaseAuthWeakPasswordException) {
                        val exception = task.exception as FirebaseAuthWeakPasswordException
                        if (exception.errorCode == FirebaseAuthConstants.ERROR_WEAK_PASSWORD) {
//                            Toast.makeText(activity, FirebaseAuthConstants.ERROR_WEAK_PASSWORD, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        } else {
//            Toast.makeText(activity, activity.resources.getString(R.string.password_lenght), Toast.LENGTH_SHORT).show()
        }
    }

    fun signInWithEmail(email: String, password: String) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                //Toast.makeText(activity, email.toString(), Toast.LENGTH_SHORT).show()
                if (task.isSuccessful) {
                    if(fragment is LoginFragment){
                        fragment.uiUpdateMain(task.result?.user)
                    }
                } else {
                    Log.d("!!!er", task.exception.toString())
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Log.d("!!!er", task.exception.toString())
                        val exception = task.exception as FirebaseAuthInvalidCredentialsException
                        if (exception.errorCode == FirebaseAuthConstants.ERROR_INVALID_EMAIL) {
//                            Toast.makeText(activity, FirebaseAuthConstants.ERROR_INVALID_EMAIL, Toast.LENGTH_SHORT).show()
                        } else if (exception.errorCode == FirebaseAuthConstants.ERROR_WRONG_PASSWORD) {
//                            Toast.makeText(activity, FirebaseAuthConstants.ERROR_WRONG_PASSWORD, Toast.LENGTH_SHORT).show()
                        }
                    } else if (task.exception is FirebaseAuthInvalidUserException) {
                        val exception = task.exception as FirebaseAuthInvalidUserException
//                        Log.d("!!!er", "er ${exception.errorCode}")
                        if (exception.errorCode == FirebaseAuthConstants.ERROR_USER_NOT_FOUND) {
//                            Toast.makeText(activity, FirebaseAuthConstants.ERROR_USER_NOT_FOUND, Toast.LENGTH_SHORT).show()
                        }
                    }
//                    Toast.makeText(activity, activity.resources.getString(R.string.sign_in_erroro), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun sendEmailVerification(user: FirebaseUser) {
        user.sendEmailVerification().addOnCompleteListener() { task ->
            if (task.isSuccessful) {
//                Toast.makeText( applicationCon, fragment.resources.getString(R.string.), Toast.LENGTH_SHORT).show()
            } else {
//                Toast.makeText(activity, activity.resources.getString(R.string.send_verification_email_error), Toast.LENGTH_SHORT).show()
            }
        }
    }
//    fun uiUpdate(user: FirebaseUser?) {
//        Log.d("!!!", user.toString())
////        tvAccount.text = if (user == null) {
////            resources.getString(R.string.not_reg)
////        } else {
////            user.email
////        }
//    }
}


