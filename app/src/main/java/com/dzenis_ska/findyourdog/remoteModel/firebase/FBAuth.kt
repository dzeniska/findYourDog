package com.dzenis_ska.findyourdog.remoteModel.firebase

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log

import android.widget.Toast
import androidx.fragment.app.Fragment
import com.dzenis_ska.desk.constants.FirebaseAuthConstants
import com.dzenis_ska.findyourdog.ui.fragments.LoginFragment
import com.google.firebase.auth.*
import com.dzenis_ska.findyourdog.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class FBAuth(private val fragment: Fragment) {

    val mAuth = Firebase.auth

    private fun createUserWithEmailAndPassword(email: String, password: String, callback: (isCreate: Boolean?, messCreate: String, user: FirebaseUser?) -> Unit) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener { task ->
            val user = task.user!!
            sendEmailVerification(user) {
                if (it)
                    callback(true, FirebaseAuthConstants.LUCKY_CREATOR, user)
                else
                    callback(false, FirebaseAuthConstants.LUCKY_CREATOR, user)
            }
        }.addOnFailureListener { task ->
            if (task is FirebaseAuthUserCollisionException) {
                if (task.errorCode == FirebaseAuthConstants.ERROR_EMAIL_ALREADY_IN_USE) {
                    callback(false, FirebaseAuthConstants.ERROR_EMAIL_ALREADY_IN_USE, null)
                }
            } else if (task is FirebaseAuthInvalidCredentialsException) {
                if (task.errorCode == FirebaseAuthConstants.ERROR_INVALID_EMAIL) {
                    callback(false, FirebaseAuthConstants.ERROR_INVALID_EMAIL, null)
                } else if (task.errorCode == FirebaseAuthConstants.ERROR_WEAK_PASSWORD) {
                    callback(false, FirebaseAuthConstants.ERROR_WEAK_PASSWORD, null)
                }
            }
            if (task is FirebaseAuthWeakPasswordException) {
                if (task.errorCode == FirebaseAuthConstants.ERROR_WEAK_PASSWORD) {
                    callback(false, FirebaseAuthConstants.ERROR_WEAK_PASSWORD, null)
                }
            }
        }
    }

    fun signInWithEmail(email: String, password: String, callback: (isSign: Boolean?, messSign: String, fbUser: FirebaseUser?) -> Unit) {
        signOutAnon()
        mAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener { task ->
            callback(true, "${task.user?.email}", task.user)
        }.addOnFailureListener { task->
            callback(false, "$task", null)
            if (task is FirebaseAuthInvalidCredentialsException) {
                if (task.errorCode == FirebaseAuthConstants.ERROR_INVALID_EMAIL) {
                    callback(false, FirebaseAuthConstants.ERROR_INVALID_EMAIL, null)
                } else if (task.errorCode == FirebaseAuthConstants.ERROR_WRONG_PASSWORD) {
                    callback(false, FirebaseAuthConstants.ERROR_WRONG_PASSWORD, null)
                }
            } else if (task is FirebaseAuthInvalidUserException) {
                if (task.errorCode == FirebaseAuthConstants.ERROR_USER_NOT_FOUND) {
                    createUserWithEmailAndPassword(email, password){isCreate, messCreate, user ->
                        callback(isCreate, messCreate, user)
                    }
                    callback(false, FirebaseAuthConstants.ERROR_USER_NOT_FOUND, null)
                }
            }
        }
    }

    fun isEmailVerified(user: FirebaseUser?): Boolean? = user?.isEmailVerified

    private fun sendEmailVerification(user: FirebaseUser, callback: (isVerifyEmail: Boolean) -> Unit) {
        user.sendEmailVerification()
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    fun signInAnonymously(callback: (isSignAnon: Boolean?, messAnonSign: String) -> Unit) {
        if(mAuth.currentUser == null) {
            mAuth.signInAnonymously()
                .addOnSuccessListener {
                    val mess = "ну, гостем будешь, ok!"
                    callback(true, mess)
                }
                .addOnFailureListener { callback(false, "${it.message}") }
        }
    }

    fun signOutAnon() {
        val anon = mAuth.currentUser
        if (anon?.isAnonymous == true) anon.delete()
    }
    fun signOut(){
        if(mAuth.currentUser != null){
            mAuth.signOut()
        }
    }
}


