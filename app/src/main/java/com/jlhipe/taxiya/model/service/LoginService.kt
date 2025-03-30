package com.jlhipe.taxiya.model.service

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.jlhipe.taxiya.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class LoginService @Inject constructor() {
    /*
     * TODO ha sido pasado al LoginViewModel
     */
    val currentUser: Flow<User?>
        get() = callbackFlow {
            //TODO
        }

    val currentUserId: String
        get() = Firebase.auth.currentUser?.uid.orEmpty()

    //Devuelve true si está logeado
    fun hasUser(): Boolean {
        return Firebase.auth.currentUser != null
    }

    //login con email
    suspend fun singIn(email: String, password: String) {
        Firebase.auth.signInWithEmailAndPassword(email, password).await()
    }

    //registrar con email
    suspend fun signUp(email: String, password: String) {
        Firebase.auth.createUserWithEmailAndPassword(email, password).await()
    }

    suspend fun signOut() {
        //TODO
    }

    suspend fun deleteAccount() {

    }
}