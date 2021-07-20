package com.dzenis_ska.findyourdog.remoteModel.firebase

import android.content.Context
import android.util.Log

import android.widget.Toast
import androidx.fragment.app.Fragment
import com.dzenis_ska.desk.constants.FirebaseAuthConstants
import com.dzenis_ska.findyourdog.ui.fragments.LoginFragment
import com.google.firebase.auth.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.dzenis_ska.findyourdog.R

class FBAuth(private val fragment: Fragment) {

    val mAuth = FirebaseAuth.getInstance()

    suspend fun signUpWithEmail(email: String, password: String, context: Context) =  withContext(Dispatchers.IO){
           mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, context.resources.getString(R.string.reg_up_is_successful), Toast.LENGTH_SHORT).show()
                    sendEmailVerification(task.result?.user!!,context)
                    if(fragment is LoginFragment){
                        fragment.uiUpdateMain(task.result?.user!!)
                    }
                } else {
//                    Toast.makeText(context, context.resources.getString(R.string.error_reg), Toast.LENGTH_SHORT).show()
                    Log.d("!!!er", task.exception.toString())
                    if (task.exception is FirebaseAuthUserCollisionException) {
                        val exception = task.exception as FirebaseAuthUserCollisionException
                        Log.d("!!!erPas", exception.errorCode)
                        if (exception.errorCode == FirebaseAuthConstants.ERROR_EMAIL_ALREADY_IN_USE) {
                            Toast.makeText(context, FirebaseAuthConstants.ERROR_EMAIL_ALREADY_IN_USE, Toast.LENGTH_SHORT).show()
                            Log.d("!!!error", "${FirebaseAuthConstants.ERROR_EMAIL_ALREADY_IN_USE}")

                        }
                    } else if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        val exception = task.exception as FirebaseAuthInvalidCredentialsException
                        if (exception.errorCode == FirebaseAuthConstants.ERROR_INVALID_EMAIL) {
                            Toast.makeText(context, FirebaseAuthConstants.ERROR_INVALID_EMAIL, Toast.LENGTH_SHORT).show()
                        } else if (exception.errorCode == FirebaseAuthConstants.ERROR_WEAK_PASSWORD) {
                            Toast.makeText(context, FirebaseAuthConstants.ERROR_WEAK_PASSWORD, Toast.LENGTH_SHORT).show()
                        }
                    }
                    if (task.exception is FirebaseAuthWeakPasswordException) {
                        val exception = task.exception as FirebaseAuthWeakPasswordException
                        if (exception.errorCode == FirebaseAuthConstants.ERROR_WEAK_PASSWORD) {
                            Toast.makeText(context, FirebaseAuthConstants.ERROR_WEAK_PASSWORD, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
        }
    }

    suspend fun signInWithEmail(email: String, password: String, context: Context) = withContext(Dispatchers.IO) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Вы вошли как ${task.result?.user?.email}", Toast.LENGTH_SHORT).show()
                    if(fragment is LoginFragment){
                        fragment.uiUpdateMain(task.result?.user!!)
                    }
                } else {
//                    Log.d("!!!er", task.exception.toString())
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
//                        Log.d("!!!er", task.exception.toString())
                        val exception = task.exception as FirebaseAuthInvalidCredentialsException
                        if (exception.errorCode == FirebaseAuthConstants.ERROR_INVALID_EMAIL) {
                            Toast.makeText(context, FirebaseAuthConstants.ERROR_INVALID_EMAIL, Toast.LENGTH_SHORT).show()
                        } else if (exception.errorCode == FirebaseAuthConstants.ERROR_WRONG_PASSWORD) {
                            if(fragment is LoginFragment){
                                fragment.uiReplacePassword()

                            }
                            Toast.makeText(context, FirebaseAuthConstants.ERROR_WRONG_PASSWORD, Toast.LENGTH_SHORT).show()
                        }
                    } else if (task.exception is FirebaseAuthInvalidUserException) {
                        val exception = task.exception as FirebaseAuthInvalidUserException
                        if (exception.errorCode == FirebaseAuthConstants.ERROR_USER_NOT_FOUND) {
                            Toast.makeText(context, FirebaseAuthConstants.ERROR_USER_NOT_FOUND, Toast.LENGTH_SHORT).show()
                        }
                    }
//                    Toast.makeText(context, context.resources.getString(R.string.error_enter), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun sendEmailVerification(user: FirebaseUser, context: Context) {
        user.sendEmailVerification().addOnCompleteListener() { task ->
            if (task.isSuccessful) {
                Toast.makeText( context, fragment.resources.getString(R.string.email_add), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, context.resources.getString(R.string.email_no_add), Toast.LENGTH_SHORT).show()
            }
        }
    }


}


