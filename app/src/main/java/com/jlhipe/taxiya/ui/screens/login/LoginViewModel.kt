package com.jlhipe.taxiya.ui.screens.login

import android.app.Application
import android.util.Log
import androidx.credentials.Credential
import androidx.credentials.CustomCredential
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.jlhipe.taxiya.R
import com.jlhipe.taxiya.model.User
import com.jlhipe.taxiya.model.service.LoginService
import com.jlhipe.taxiya.ui.screens.main.RutaViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
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

    private val _esConductor = MutableStateFlow(false)
    val esConductor: StateFlow<Boolean> = _esConductor.asStateFlow()

    //private val _validado = MutableStateFlow(true)
    //val validado: StateFlow<Boolean> = _validado.asStateFlow()

    private val _nombre = MutableStateFlow("")
    val nombre: StateFlow<String> = _nombre.asStateFlow()

    private val _apellidos = MutableStateFlow("")
    val apellidos: StateFlow<String> = _apellidos.asStateFlow()

    private var _logeado = MutableLiveData<Boolean>()
    val logeado: LiveData<Boolean> = _logeado

    private var _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    var _userId: String = ""

    private var _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    /*
     * TODO INICIO SACADOS DE LoginService
     */
    val currentUser: Flow<User?>
        get() = callbackFlow {
            val listener =
                FirebaseAuth.AuthStateListener { auth ->
                    this.trySend(auth.currentUser?.let {User(it.uid) })
                    //this.trySend(auth.currentUser.toTaxiYaUser())
                }
            Firebase.auth.addAuthStateListener(listener)
            awaitClose { Firebase.auth.removeAuthStateListener(listener) }
        }

    val currentUserId: String
        get() = Firebase.auth.currentUser?.uid.orEmpty()

    val currentUserName: String
        get() = Firebase.auth.currentUser?.displayName.orEmpty()

    val currentUserEmail: String
        get() = Firebase.auth.currentUser?.email.orEmpty()

    //Devuelve true si está logeado
    fun hasUser(): Boolean {
        return Firebase.auth.currentUser != null
    }

    //login con email
    suspend fun singIn(email: String, password: String) {
        Firebase.auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if(it.isSuccessful) {
                //_logeado.postValue(true)
                _logeado.value = true
                _error.postValue("")
            } else {
                _error.postValue(it.exception!!.message)
                _password.value = ""
            }
        }.await()
    }

    //registrar con email
    suspend fun signUp(email: String, password: String) {
        Firebase.auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener{
            if(it.isSuccessful) {
                //_logeado.postValue(true)
                _logeado.value = true
                _error.postValue("")
                //TODO enviar email de confirmación
            } else {
                _error.postValue(it.exception!!.message)
                _password.value = ""
                //TODO Mostrar mensaje de error (quizás cambiar un boolean que haga el mensaje visible basta)
            }
        }.await()
        //TODO guardar resto de variables en la BBDD
    }

    /*suspend */fun signOut() {
        Firebase.auth.signOut()
        //_logeado.postValue(false)
        _logeado.value = false
    }

    suspend fun deleteAccount() {
        Firebase.auth.currentUser!!.delete().await()
        //_logeado.value = false
        _logeado.postValue(false)
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
    fun updateNombre(newNombre: String) {
        _nombre.value = newNombre
    }
    fun updateApellidos(newApellidos: String) {
        _apellidos.value = newApellidos
    }
    fun updateEsConductor(esConductor: Boolean) {
        _esConductor.value = esConductor
    }

    fun setLogeado(logeado: Boolean) {
        _logeado.value = logeado
    }

    fun onSignInClick() {
        //TODO recuperar ciertos campos en la BBDD
        launchCatching {
            singIn(
                email = _email.value,
                password = _password.value,
            )
        }
    }

    fun onSignUpClick() {
        //TODO guardar ciertos campos en la BBDD
        launchCatching {
            signUp(
                email = _email.value,
                password = _password.value,
                /*
                esConductor = false,
                validado = true, //TODO para pruebas se validan los conductores por defecto, en producción deberá ser false
                nombre = _nombre.value,
                apellidos = _apellidos.value
                 */
            )
        }
    }

    fun onLogOutClick() {
        launchCatching {
            signOut()
        }
    }

    fun onDeleteAccountClick() {
        launchCatching {
            deleteAccount()
        }
    }

    fun navegar(navegar: () -> Unit = {}) {
        launchCatching {
            delay(500)
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

    fun onSignInWithGoogle(credential: Credential) {
        launchCatching {
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                signInWithGoogle(googleIdTokenCredential.idToken)
                _logeado.value = true
            } else {
                //Log.e(ERROR_TAG, UNEXPECTED_CREDENTIAL)
            }
        }
    }

    suspend fun signInWithGoogle(idToken: String) {
        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
        Firebase.auth.signInWithCredential(firebaseCredential).await()
    }
}