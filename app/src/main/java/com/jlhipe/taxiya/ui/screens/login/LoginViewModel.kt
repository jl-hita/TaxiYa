package com.jlhipe.taxiya.ui.screens.login

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.jlhipe.taxiya.model.User
import com.jlhipe.taxiya.model.service.LoginService
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

//@HiltViewModel
/*
class LoginViewModel @Inject constructor(
    private val loginService: LoginService,
    //navController: NavController
) : TaxiYaViewModel() {
*/
class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    //var loginService = LoginService()

    /*
     * TODO INICIO SACADOS DE LoginService
     */
    val currentUser: Flow<User?>
        get() = callbackFlow {
            val listener =
                FirebaseAuth.AuthStateListener { auth ->
                    this.trySend(auth.currentUser.toTaxiYaUser())
                }
            Firebase.auth.addAuthStateListener(listener)
            awaitClose { Firebase.auth.removeAuthStateListener(listener) }
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
        //TODO
    }

    /*
     * TODO FIN SACADOS DE LoginService
     */

    fun updateEmail(newEmail: String) {
        _email.value = newEmail
    }
    fun updatePassword(newPassword: String) {
        _password.value = newPassword
    }

    //Le pasamos la llamada a navController.navigate por parámetro
    fun onSignInClick(navegar: () -> Unit = {}) {
        launchCatching {
            singIn(
                email = _email.value,
                password = _password.value
            )
            navegar()
        }
    }

    //Le pasamos la llamada a navController.navigate por parámetro
    fun onSignUpClick(navegar: () -> Unit = {}) {
        launchCatching {
            navegar()
        }
    }

    //Lanza funcionas asíncronas y gestiona las excepciones que puedan lanzar
    fun launchCatching(block: suspend CoroutineScope.() -> Unit) =
        viewModelScope.launch (
            CoroutineExceptionHandler { _, throwable ->
                Log.d("TAXIYA APP ERROR", throwable.message.orEmpty())
            },
            block = block
        )

    private fun FirebaseUser?.toTaxiYaUser(): User {
        return if (this == null) User() else User(
            id = this.uid,
            email = this.email ?: "",
            provider = this.providerId,
            nombre = this.displayName ?: "",
            isAnonymous = this.isAnonymous
        )
    }
}