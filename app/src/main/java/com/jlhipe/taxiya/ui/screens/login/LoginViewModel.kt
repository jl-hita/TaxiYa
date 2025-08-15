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
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
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
import com.google.firebase.firestore.ktx.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _esConductor = MutableStateFlow(false)
    val esConductor: StateFlow<Boolean> = _esConductor.asStateFlow()

    private val _nombre = MutableStateFlow("")
    val nombre: StateFlow<String> = _nombre.asStateFlow()

    private val _apellidos = MutableStateFlow("")
    val apellidos: StateFlow<String> = _apellidos.asStateFlow()

    private val _logeado = MutableLiveData<Boolean>(false)
    val logeado: LiveData<Boolean> = _logeado

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    init {
        val currentUser = Firebase.auth.currentUser
        _logeado.value = currentUser != null
        currentUser?.uid?.let { uid ->
            viewModelScope.launch {
                cargarUsuarioDesdeFirestore(uid)
            }
        }

        _logeado.observeForever { logeado ->
            Log.d("DEBUG_LOGIN", "LiveData logeado = $logeado")
        }
    }


    // ----------------------------------------------------------
    // LOGIN / SIGNUP
    // ----------------------------------------------------------
    suspend fun signIn(email: String, password: String) {
        try {
            Firebase.auth.signInWithEmailAndPassword(email, password).await()
            Firebase.auth.currentUser?.uid?.let { uid ->
                val usuario = cargarUsuarioDesdeFirestore(uid)
                withContext(Dispatchers.Main) {
                    _user.value = usuario
                    _logeado.value = true
                }
            } ?: run {
                withContext(Dispatchers.Main) { _logeado.value = false }
            }
        } catch (e: Exception) {
            Log.d("LoginViewModel", "Excepción en signIn -> ${e.message}")
            withContext(Dispatchers.Main) {
                _error.value = e.message
                _logeado.value = false
            }
        }
    }

    suspend fun signUp(email: String, password: String) {
        try {
            Firebase.auth.createUserWithEmailAndPassword(email, password).await()
            Firebase.auth.currentUser?.uid?.let { uid ->
                val nuevoUser = User(
                    id = uid,
                    email = email,
                    esConductor = _esConductor.value,
                    nombre = _nombre.value,
                    apellidos = _apellidos.value
                )
                Firebase.firestore.collection("usuarios").document(uid).set(nuevoUser).await()
                withContext(Dispatchers.Main) {
                    _user.value = nuevoUser
                    _logeado.value = true
                    _error.value = ""
                }
            }
        } catch (e: Exception) {
            Log.d("LoginViewModel", "Excepción en signUp -> ${e.message}")
            withContext(Dispatchers.Main) {
                _error.value = e.message
                _password.value = ""
                _logeado.value = false
            }
        }
    }

    fun signOut() {
        try {
            Firebase.auth.signOut()
            _logeado.value = false
            _user.value = null
        } catch (e: Exception) {
            Log.e("LoginViewModel", "Error al hacer signOut", e)
        }
    }

    // ----------------------------------------------------------
    // CARGAR USUARIO FIRESTORE
    // ----------------------------------------------------------
    private suspend fun cargarUsuarioDesdeFirestore(uid: String): User? {
        return try {
            val doc = Firebase.firestore.collection("usuarios").document(uid).get().await()
            _user.value = doc.toObject(User::class.java)
            _user.value
        } catch (e: Exception) {
            Log.d("LoginViewModel", "Error cargando usuario desde Firestore -> ${e.message}")
            null
        }
    }
    /*
    private fun cargarUsuarioDesdeFirestore(uid: String) {
        if (uid.isEmpty()) return // <-- Evita llamar a Firestore con uid vacío

        try {
            Firebase.firestore.collection("usuarios")
                .document(uid)
                .get()
                .addOnSuccessListener { doc ->
                    val usuario = doc.toObject(User::class.java)
                    _user.postValue(usuario) // puede ser null si no existe
                }
                .addOnFailureListener { e ->
                    Log.e("LOGIN", "Error cargando usuario desde Firestore", e)
                }
        } catch (e: Exception) {
            Log.d("LoginViewModel", "Excepción en cargarUsuarioDesdeFirestore -> ${e.message}")
        }

    }
     */

    // ----------------------------------------------------------
    // ACTUALIZAR CAMPOS
    // ----------------------------------------------------------
    fun updateEmail(newEmail: String) { _email.value = newEmail }
    fun updatePassword(newPassword: String) { _password.value = newPassword }
    fun updateNombre(newNombre: String) { _nombre.value = newNombre }
    fun updateApellidos(newApellidos: String) { _apellidos.value = newApellidos }
    fun updateEsConductor(esConductor: Boolean) { _esConductor.value = esConductor }

    //TODO BORRAME, prueba
    fun probarLogin() {
        val currentUser = Firebase.auth.currentUser
        Log.d("DEBUG_LOGIN", "Current user: $currentUser")
    }

    // ----------------------------------------------------------
    // GOOGLE SIGN-IN
    // ----------------------------------------------------------
    suspend fun signInWithGoogle(idToken: String) {
        try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            Firebase.auth.signInWithCredential(credential).await()

            cargarUsuarioFirebase()

            /*
            val uid = Firebase.auth.currentUser?.uid
            if (uid != null) {
                // Verificamos si ya existe en Firestore
                val doc = Firebase.firestore.collection("usuarios").document(uid).get().await()
                val usuario = doc.toObject(User::class.java) ?: run {
                    // Si no existe, lo creamos
                    val nuevoUser = User(
                        id = uid,
                        email = Firebase.auth.currentUser?.email ?: "",
                        esConductor = _esConductor.value,
                        nombre = _nombre.value,
                        apellidos = _apellidos.value
                    )
                    Firebase.firestore.collection("usuarios").document(uid).set(nuevoUser).await()
                    nuevoUser
                }

                withContext(Dispatchers.Main) {
                    _user.value = usuario
                    _logeado.value = true
                }
            } else {
                withContext(Dispatchers.Main) { _logeado.value = false }
            }
            */
        } catch (e: Exception) {
            Log.d("LoginViewModel", "Excepción en signInWithGoogle -> ${e.message}")
            withContext(Dispatchers.Main) {
                _error.value = e.message
                _logeado.value = false
            }
        }
    }

    suspend fun cargarUsuarioFirebase() {
        val uid = Firebase.auth.currentUser?.uid
        if (uid != null) {
            // Verificamos si ya existe en Firestore
            val doc = Firebase.firestore.collection("usuarios").document(uid).get().await()
            val usuario = doc.toObject(User::class.java) ?: run {
                // Si no existe, lo creamos
                val nuevoUser = User(
                    id = uid,
                    email = Firebase.auth.currentUser?.email ?: "",
                    esConductor = _esConductor.value,
                    nombre = _nombre.value,
                    apellidos = _apellidos.value
                )
                Firebase.firestore.collection("usuarios").document(uid).set(nuevoUser).await()
                nuevoUser
            }

            withContext(Dispatchers.Main) {
                _user.value = usuario
                _logeado.value = true
            }
        } else {
            withContext(Dispatchers.Main) { _logeado.value = false }
        }
    }

    suspend fun onSignInWithGoogle(credential: Credential) {
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {

            val googleIdTokenCredential =
                GoogleIdTokenCredential.createFrom(credential.data)

            signInWithGoogle(googleIdTokenCredential.idToken)

            /*
            Firebase.auth.currentUser?.uid?.let { uid ->
                val usuario = cargarUsuarioDesdeFirestore(uid)
                withContext(Dispatchers.Main) {
                    _user.value = usuario
                    _logeado.value = true
                }
            } ?: run {
                withContext(Dispatchers.Main) { _logeado.value = false }
            }
             */
        }
    }

    fun navegar(navegar: () -> Unit = {}) {
        launchCatching {
            delay(500)
            navegar()
        }
    }

    fun launchCatching(block: suspend CoroutineScope.() -> Unit) =
        viewModelScope.launch(
            CoroutineExceptionHandler { _, throwable ->
                Log.d("TAXIYA APP ERROR", throwable.message.orEmpty())
            },
            block = block
        )

    fun onSignUpClick() {
        launchCatching {
            signUp(_email.value, _password.value)
        }
    }

    fun onLogOutClick() {
        launchCatching { signOut() }
    }

    fun onDeleteAccountClick() {
        launchCatching { deleteAccount() }
    }

    suspend fun deleteAccount() {
        Firebase.auth.currentUser!!.delete().await()
        _logeado.postValue(false)
    }
}

/*
class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _esConductor = MutableStateFlow(false)
    val esConductor: StateFlow<Boolean> = _esConductor.asStateFlow()

    private val _nombre = MutableStateFlow("")
    val nombre: StateFlow<String> = _nombre.asStateFlow()

    private val _apellidos = MutableStateFlow("")
    val apellidos: StateFlow<String> = _apellidos.asStateFlow()

    private var _logeado = MutableLiveData<Boolean>()
    val logeado: LiveData<Boolean> = _logeado

    private var _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    var _userId: String = ""

    private var _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    // -------------------------
    // CARGAR USUARIO DESDE FIRESTORE
    // -------------------------

    val currentUser: Flow<User?>
        get() = callbackFlow {
            val listener =
                FirebaseAuth.AuthStateListener { auth ->
                    this.trySend(auth.currentUser?.let { User(it.uid) })
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

    fun hasUser(): Boolean {
        return Firebase.auth.currentUser != null
    }

    // -------------------------
    // SIGN IN CON EMAIL
    // -------------------------
    suspend fun signIn(email: String, password: String) {
        try {
            Firebase.auth.signInWithEmailAndPassword(email, password).await()
            _logeado.value = true
            _error.value = ""
            Firebase.auth.currentUser?.uid?.let { cargarUsuarioDesdeFirestore(it) }
        } catch (e: Exception) {
            _error.value = e.message
            _password.value = ""
            _logeado.value = false
        }
    }

    // -------------------------
    // SIGN UP CON EMAIL
    // -------------------------
    suspend fun signUp(email: String, password: String) {
        Firebase.auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                val uid = Firebase.auth.currentUser!!.uid

                // 🔹 Guardar datos en Firestore
                val nuevoUsuario = User(
                    id = uid,
                    email = email,
                    nombre = _nombre.value,
                    apellidos = _apellidos.value,
                    esConductor = _esConductor.value
                )

                Firebase.firestore.collection("usuarios")
                    .document(uid)
                    .set(nuevoUsuario)
                    .addOnSuccessListener {
                        _user.postValue(nuevoUsuario)
                        Log.d("SIGNUP", "Usuario guardado en Firestore")
                    }
                    .addOnFailureListener { e ->
                        Log.e("SIGNUP", "Error guardando usuario en Firestore", e)
                    }

                _logeado.value = true
                _error.postValue("")
            } else {
                _error.postValue(it.exception?.message ?: "Error desconocido")
                _password.value = ""
            }
        }.await()
    }

    fun signOut() {
        Firebase.auth.signOut()
        _logeado.value = false
    }

    suspend fun deleteAccount() {
        Firebase.auth.currentUser!!.delete().await()
        _logeado.postValue(false)
    }

    fun updateEmail(newEmail: String) { _email.value = newEmail }
    fun updatePassword(newPassword: String) { _password.value = newPassword }
    fun updateNombre(newNombre: String) { _nombre.value = newNombre }
    fun updateApellidos(newApellidos: String) { _apellidos.value = newApellidos }
    fun updateEsConductor(esConductor: Boolean) { _esConductor.value = esConductor }
    fun setLogeado(logeado: Boolean) { _logeado.value = logeado }

    fun onSignInClick() {
        launchCatching {
            //singIn(_email.value, _password.value)
            signIn(_email.value, _password.value)
        }
    }

    fun onSignUpClick() {
        launchCatching {
            signUp(_email.value, _password.value)
        }
    }

    fun onLogOutClick() {
        launchCatching { signOut() }
    }

    fun onDeleteAccountClick() {
        launchCatching { deleteAccount() }
    }

    fun navegar(navegar: () -> Unit = {}) {
        launchCatching {
            delay(500)
            navegar()
        }
    }

    fun launchCatching(block: suspend CoroutineScope.() -> Unit) =
        viewModelScope.launch(
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

    suspend fun onSignInWithGoogle(credential: Credential) {
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {

            val googleIdTokenCredential =
                GoogleIdTokenCredential.createFrom(credential.data)

            signInWithGoogle(googleIdTokenCredential.idToken)

            _logeado.value = true

            Firebase.auth.currentUser?.uid?.let { uid ->
                cargarUsuarioDesdeFirestore(uid)
            }
        }
    }

    suspend fun signInWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        Firebase.auth.signInWithCredential(credential).await()
        _logeado.value = true
        Firebase.auth.currentUser?.uid?.let { cargarUsuarioDesdeFirestore(it) }
    }

    fun cargarUsuarioDesdeFirestore(uid: String) {
        Firebase.firestore.collection("usuarios")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val usuario = document.toObject(User::class.java)
                    if (usuario != null) {
                        _user.postValue(usuario)
                    } else {
                        Log.w("SIGNIN", "Documento encontrado pero no se pudo convertir a User")
                        _user.postValue(null)
                    }
                } else {
                    Log.w("SIGNIN", "El documento no existe en Firestore")
                    _user.postValue(null)
                }
            }
            .addOnFailureListener { e ->
                Log.e("SIGNIN", "Error obteniendo usuario en Firestore", e)
                _user.postValue(null)
            }
    }

}
 */

/*
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

    //Cargamos el objeto user de Firebase


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
*/

/*
 * Versión rara que me ha hecho Paco
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _esConductor = MutableStateFlow(false)
    val esConductor: StateFlow<Boolean> = _esConductor.asStateFlow()

    private val _nombre = MutableStateFlow("")
    val nombre: StateFlow<String> = _nombre.asStateFlow()

    private val _apellidos = MutableStateFlow("")
    val apellidos: StateFlow<String> = _apellidos.asStateFlow()

    private var _logeado = MutableLiveData<Boolean>()
    val logeado: LiveData<Boolean> = _logeado

    private var _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private var _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    val currentUser: Flow<User?>
        get() = callbackFlow {
            val listener =
                FirebaseAuth.AuthStateListener { auth ->
                    this.trySend(auth.currentUser?.let { User(it.uid) })
                }
            Firebase.auth.addAuthStateListener(listener)
            awaitClose { Firebase.auth.removeAuthStateListener(listener) }
        }

    val currentUserId: String
        get() = Firebase.auth.currentUser?.uid.orEmpty()

    fun hasUser(): Boolean {
        return Firebase.auth.currentUser != null
    }

    // LOGIN con email
    suspend fun singIn(email: String, password: String) {
        Firebase.auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Firebase.auth.currentUser?.uid?.let { cargarUsuarioDesdeFirestore(it) }
                _logeado.value = true
                _error.postValue("")
            } else {
                _error.postValue(task.exception?.message ?: "Error desconocido")
                _password.value = ""
            }
        }.await()
    }

    // REGISTRO con email
    suspend fun signUp(email: String, password: String) {
        Firebase.auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val uid = Firebase.auth.currentUser!!.uid

                val nuevoUsuario = User(
                    id = uid,
                    email = email,
                    esConductor = _esConductor.value,
                    nombre = _nombre.value,
                    apellidos = _apellidos.value,
                    activo = false,
                    enRuta = false,
                    rutaActual = "",
                    provider = Firebase.auth.currentUser?.providerId ?: "",
                    isAnonymous = false
                )

                Firebase.firestore.collection("usuarios")
                    .document(uid)
                    .set(nuevoUsuario)
                    .addOnSuccessListener {
                        _user.postValue(nuevoUsuario)
                    }
                    .addOnFailureListener { e ->
                        Log.e("SIGNUP", "Error guardando usuario en Firestore", e)
                    }

                _logeado.value = true
                _error.postValue("")
            } else {
                _error.postValue(task.exception?.message ?: "Error desconocido")
                _password.value = ""
            }
        }.await()
    }

    fun signOut() {
        Firebase.auth.signOut()
        _logeado.value = false
    }

    suspend fun deleteAccount() {
        Firebase.auth.currentUser!!.delete().await()
        _logeado.postValue(false)
    }

    fun updateEmail(newEmail: String) { _email.value = newEmail }
    fun updatePassword(newPassword: String) { _password.value = newPassword }
    fun updateNombre(newNombre: String) { _nombre.value = newNombre }
    fun updateApellidos(newApellidos: String) { _apellidos.value = newApellidos }
    fun updateEsConductor(esConductor: Boolean) { _esConductor.value = esConductor }
    fun setLogeado(logeado: Boolean) { _logeado.value = logeado }

    fun onSignInClick() {
        launchCatching {
            singIn(
                email = _email.value,
                password = _password.value
            )
        }
    }

    fun onSignUpClick() {
        launchCatching {
            signUp(
                email = _email.value,
                password = _password.value
            )
        }
    }

    fun onLogOutClick() { launchCatching { signOut() } }
    fun onDeleteAccountClick() { launchCatching { deleteAccount() } }

    fun navegar(navegar: () -> Unit = {}) {
        launchCatching {
            delay(500)
            navegar()
        }
    }

    fun launchCatching(block: suspend CoroutineScope.() -> Unit) =
        viewModelScope.launch(
            CoroutineExceptionHandler { _, throwable ->
                Log.d("TAXIYA APP ERROR", throwable.message.orEmpty())
            },
            block = block
        )

    private fun cargarUsuarioDesdeFirestore(uid: String) {
        Firebase.firestore.collection("usuarios")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val usuario = document.toObject(User::class.java)
                    _user.postValue(usuario)
                }
            }
            .addOnFailureListener { e ->
                Log.e("SIGNIN", "Error obteniendo usuario en Firestore", e)
            }
    }

    // LOGIN con Google usando GoogleSignInAccount
    fun onSignInWithGoogle(account: GoogleSignInAccount) {
        launchCatching {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            Firebase.auth.signInWithCredential(credential).await()

            // Una vez autenticado, recuperamos el usuario desde Firestore
            Firebase.auth.currentUser?.uid?.let { cargarUsuarioDesdeFirestore(it) }
            _logeado.value = true
        }
    }
}
 */