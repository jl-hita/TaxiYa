package com.jlhipe.taxiya.ui.screens.main

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.jlhipe.taxiya.R
import com.jlhipe.taxiya.model.Ruta
import com.jlhipe.taxiya.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Locale
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.Date
import kotlinx.coroutines.withContext

class RutaViewModel: ViewModel() {
    //Lista de rutas
    private val _rutas = MutableLiveData<List<Ruta>>()
    val rutas: LiveData<List<Ruta>> = _rutas

    //Ruta seleccionada
    private val _selectedRuta = MutableLiveData<Ruta?>()
    val selectedRuta: LiveData<Ruta?> = _selectedRuta

    //Lista de rutas buscando taxista
    private val _rutasBuscandoTaxi = MutableLiveData<List<Ruta>>()
    val rutasBuscandoTaxi: LiveData<List<Ruta>> = _rutasBuscandoTaxi

    //Indica que se están obteniendo los datos de Firebase
    private var _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    //BBDD Firebase
    private val db: FirebaseFirestore = Firebase.firestore

    val userId: String
        get() = Firebase.auth.currentUser?.uid.orEmpty()

    private var claveAPICalculoDistanciaDuracion: String = ""

    private var _primeraCargaTaxis = MutableLiveData<Boolean>(true)
    val primeraCargaTaxis: LiveData<Boolean> = _primeraCargaTaxis

    //Variable para manejar permisos
    val visiblePermissionDialogeQueue = mutableStateListOf<String>()

    //Job para ir recargando la ruta seleccionada de forma periódica
    private var refreshJob: Job? = null

    //Especifica cada cuantos ms se recarga la ruta de la BBDD
    private val tiempoRecarga :Long = 10000

    //Para controlar un aspecto de la navegación
    var puedeVolver by mutableStateOf(false)

    //Distancia entre cliente y conductor
    //private var _distanciaClienteConductor = MutableLiveData<Long>()
    //val distanciaClienteConductor: LiveData<Long> = _distanciaClienteConductor

    //Job para controlar distancia entre cliente y conductor
    private var distanciaJob: Job? = null

    //Para reintentar una vez si fallamos al obtener polylines
    private var reintentadoPolylines: Boolean = false

    /*
     * Si el usuario no está logeado esto provoca excepción al no tener permisos en Firebase
    init {
        loadRutas()
    }
     */

    //Inicia el job que va recargando la ruta seleccionada de forma periódica
    fun startAutoRefresh(rutaId: String) {
        // Si ya hay un job corriendo, lo cancelamos
        refreshJob?.cancel()

        refreshJob = viewModelScope.launch {
            //while (isActive) {
            while (true) {
                loadRutaFromFirebase(rutaId)
                delay(tiempoRecarga) // cada 5 segundos, ajusta a lo que necesites
            }
        }
    }

    //Detiene el job de autorecarga
    fun stopAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = null
    }

    //TODO podría quitar estos dos métodos y manejarlo todo en cambiaUbicacionConductor()
    //Inicia el job que controla la distancia entre conductor y cliente
    fun startTrackingDistancia() {
        distanciaJob?.cancel()

        distanciaJob = viewModelScope.launch {
            while (true) {
                val ruta = _selectedRuta.value
                if (ruta != null) {
                    //val clientePos = LatLng(ruta.origenGeo.latitude, ruta.origenGeo.longitude)
                    //val conductorPos = LatLng(ruta.posicionConductor.latitude, ruta.posicionConductor.longitude)

                    //val distancia: Long = calcularDistancia(clientePos, conductorPos)
                    // Obtener distancia y duración con la API
                    val (distancia, duracion) = getDistanceAndDuration(ruta.posicionConductor, ruta.origenGeo, claveAPICalculoDistanciaDuracion)
                    _selectedRuta.value!!.distanciaConductor = distancia.toLong()
                    _selectedRuta.value!!.duracionConductor = duracion
                    Log.d("RutaViewModel", "Clave API -> $claveAPICalculoDistanciaDuracion")
                    Log.d("RutaViewModel", "Distancia conductor->destino: $distancia, duración conductor->destino: $duracion")

                    // Crear copia actualizada de la ruta
                    var updatedRuta = ruta.copy(distanciaConductor = distancia.toLong())
                    updatedRuta = updatedRuta.copy(duracionConductor = duracion)

                    //Actualizamos en firebase
                    //actualizaDistanciaClienteConductor(distancia.toLong())
                    actualizaDistanciaDuracionConductor(distancia.toLong(), duracion)

                    //TODO si afecta mucho al rendimiento se puede quitar de aquí y que se calcule solo al asignar ruta
                    //Actualizamos dibujo ruta conductor -> cliente
                    getDirectionsRouteConductorCliente(ruta.id)

                    //Comprobamos si el conductor ha llegado a la posición del cliente
                    comprobarSiIniciaDestino()

                    // Publicar en el LiveData
                    _selectedRuta.postValue(updatedRuta)
                }

                delay(tiempoRecarga) // ej: 5000L para 5 segundos
            }
        }
    }

    //Detiene el job de
    fun stopTrackingDistancia() {
        distanciaJob?.cancel()
        distanciaJob = null
    }

    //Actualiza la distancia entre conductor y cliente en firebase
    fun actualizaDistanciaClienteConductor(distancia: Long) {
        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                selectedRuta.value?.let {
                    db.collection("rutas")
                        .document(it.id)
                        .update("distanciaConductor", distancia)
                        .await()
                }
            } catch (e: Exception) {
                Log.e("RutaViewModel", "Error al actualizar distancia conductor -> cliente", e)
            }
        }
    }

    fun actualizaDistanciaDuracionConductor(distancia: Long, duracion: Int) {
        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                selectedRuta.value?.let {
                    db.collection("rutas")
                        .document(it.id)
                        //.update("distanciaConductor", distancia)
                        .update(
                            mapOf(
                                "distanciaConductor" to distancia,
                                "duracionConductor" to duracion,
                            )
                        )
                        .await()
                }
            } catch (e: Exception) {
                Log.e("RutaViewModel", "Error al actualizar distancia conductor -> cliente", e)
            }
        }
    }

    //Actualiza la distancia entre conductor/cliente y destino en firebase
    fun actualizaDistanciaConductorDestino(distancia: Long) {
        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                selectedRuta.value?.let {
                    db.collection("rutas")
                        .document(it.id)
                        .update("distanciaDestino", distancia)
                        .await()
                }
            } catch (e: Exception) {
                Log.e("RutaViewModel", "Error al actualizar distancia conductor -> cliente", e)
            }
        }
    }

    //Actualiza la distancia y duración entre conductor/cliente y destino en firebase
    fun actualizaDistanciaDuracionConductorDestino(distancia: Long, duracion: Int) {
        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                selectedRuta.value?.let {
                    db.collection("rutas")
                        .document(it.id)
                        //.update("distanciaDestino", distancia)
                        .update(
                            mapOf(
                                "distancia" to distancia,
                                "duracion" to duracion
                            )
                        )
                        .await()
                }
            } catch (e: Exception) {
                Log.e("RutaViewModel", "Error al actualizar distancia conductor -> cliente", e)
            }
        }
    }

    //Cuando la distancia Conductor/Cliente sea menor a 25 metros se inicia ruta hacia destino
    fun comprobarSiIniciaDestino() {
        selectedRuta.value?.let {
            if (!it.finalizado && it.asignado && it.haciaCliente && !it.haciaDestino && it.distanciaConductor < 25) {
                iniciarRutaHaciaDestino()
            }
        }
    }

    //Inicia ruta hacia Destino
    fun iniciarRutaHaciaDestino() {
        selectedRuta.value?.let {
            // Marcar haciaDestino a true solo una vez
            it.haciaCliente = false
            it.haciaDestino = true
            //it.momentoLlegada = System.currentTimeMillis() / 1000
            //it.duracion = ((it.momentoLlegada!! - it.momentoSalida!!).toInt())

            // Guardar cambios en Firebase
            viewModelScope.launch {
                try {
                    selectedRuta.value?.let {
                        FirebaseFirestore.getInstance().collection("rutas")
                            .document(it.id)
                            .update(
                                mapOf(
                                    "haciaCliente" to false,
                                    "haciaDestino" to true,
                                    //"momentoLlegada" to it.momentoLlegada,
                                    //"duracion" to it.duracion
                                )
                            )
                            .await()
                        //Cuando se hace el cambio se para el job
                        stopTrackingDistancia()

                        //Borramos dibujo ruta conductor -> cliente
                        borrarDirectionsRouteConductorCliente(it.id)

                        //Dibujo ruta conductor -> cliente
                        getDirectionsRouteClienteDestino(it.id)

                        ///Se inicia el tracking a destino
                        startTrackingDestino()
                    }
                } catch (e: Exception) {
                    Log.e("RutaViewModel", "Error al actualizar distancia conductor -> cliente", e)
                }
            }
        }
    }

    /*
            //it.momentoLlegada = System.currentTimeMillis() / 1000
            //it.duracion = ((it.momentoLlegada!! - it.momentoSalida!!).toInt())

            // Guardar cambios en Firebase
            viewModelScope.launch {
                try {
                    selectedRuta.value?.let {
                        FirebaseFirestore.getInstance().collection("rutas")
                            .document(it.id)
                            .update(
                                mapOf(
                                    "haciaCliente" to false,
                                    "haciaDestino" to true,
                                    //"momentoLlegada" to it.momentoLlegada,
                                    //"duracion" to it.duracion
                                )
                            )
                            .await()
                        //Cuando se hace el cambio se para el job
                        stopTrackingDistancia()

                        ///Se inicia el tracking a destino
                        startTrackingDestino()
                    }
                } catch (e: Exception) {
                    Log.e("RutaViewModel", "Error al actualizar distancia conductor -> cliente", e)
                }
            }
     */
    //Cuando la distancia Conductor/Cliente y destino sea menor a 25 metros se inicia ruta hacia destino
    fun comprobarSiLlegaADestino(forzar: Boolean = false) {
        selectedRuta.value?.let {
            if (forzar || (!it.finalizado && it.asignado && it.haciaDestino && it.distancia < 25)) {
                it.haciaDestino = false
                it.enDestino = true
                it.finalizado = true
                it.momentoLlegada = System.currentTimeMillis() / 1000
                it.duracion = ((it.momentoLlegada!! - it.momentoSalida!!).toInt())

                // Guardar cambios en Firebase
                viewModelScope.launch {
                    try {
                        selectedRuta.value?.let {
                            FirebaseFirestore.getInstance().collection("rutas")
                                .document(it.id)
                                .update(
                                    mapOf(
                                        "haciaDestino" to false,
                                        "enDestino" to true,
                                        "finalizado" to true,
                                        "momentoLlegada" to it.momentoLlegada,
                                        "duracion" to it.duracion
                                    )
                                )
                                .await()
                            //Cuando se llega a destino se para el tracking
                            stopTrackingDistancia()

                        }
                    } catch (e: Exception) {
                        Log.e("RutaViewModel", "Error al actualizar distancia conductor -> cliente", e)
                    }
                }
            }
        }

    }

    //Inicia el job que controla la distancia entre conductor/cliente y destino
    fun startTrackingDestino() {
        distanciaJob?.cancel()

        distanciaJob = viewModelScope.launch {
            while (true) {
                val ruta = _selectedRuta.value
                if (ruta != null) {
                    //val conductorPos = LatLng(ruta.posicionConductor.latitude, ruta.posicionConductor.longitude)
                    //val destinoPos = LatLng(ruta.destinoGeo.latitude, ruta.destinoGeo.longitude)

                    //val distancia: Long = calcularDistancia(conductorPos, destinoPos)
                    val (distancia, duracion) = getDistanceAndDuration(ruta.posicionConductor, ruta.destinoGeo, claveAPICalculoDistanciaDuracion)
                    _selectedRuta.value!!.distancia = distancia.toLong()
                    _selectedRuta.value!!.duracion = duracion
                    Log.d("RutaViewModel", "Clave API -> $claveAPICalculoDistanciaDuracion")
                    Log.d("RutaViewModel", "Distancia conductor->destino: $distancia, duración conductor->destino: $duracion")

                    // Crear copia actualizada de la ruta
                    //val updatedRuta = ruta.copy(distanciaConductor = distancia)
                    var updatedRuta = _selectedRuta.value!!.copy(distancia = distancia.toLong())
                    updatedRuta = updatedRuta.copy(duracion = duracion)

                    //Actualizamos en firebase
                    actualizaDistanciaDuracionConductorDestino(distancia.toLong(), duracion)

                    //Comprobamos si el conductor ha llegado al destino
                    comprobarSiLlegaADestino()

                    // Publicar en el LiveData
                    _selectedRuta.postValue(updatedRuta)
                }

                delay(tiempoRecarga) // ej: 5000L para 5 segundos
            }
        }
    }

    //Carga la ruta especificada de firebase en la ruta seleccionada
    private fun loadRutaFromFirebase(rutaId: String) {
        //val currentUser = Firebase.auth.currentUser ?: return

        try {
            FirebaseFirestore.getInstance().collection("rutas").document(rutaId)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val ruta = doc.toObject(Ruta::class.java)
                        _selectedRuta.value = ruta
                    }
                }
        } catch(e: Exception) {
            Log.d("RutaViewModel", "Excepción en RutaViewModel.loadRutaFromFirebase() -> ${e.message} -> ${e.stackTrace}")
        }

    }

    //Cambia ubicación Conductor
    fun cambiaUbicacionConductor(nuevaUbicacion: LatLng) {
        val geoPoint: GeoPoint = GeoPoint(nuevaUbicacion.latitude, nuevaUbicacion.longitude)

        viewModelScope.launch {
            try {
                selectedRuta.value?.let {
                    FirebaseFirestore.getInstance().collection("rutas")
                        .document(it.id)
                        .update("posicionConductor",geoPoint)
                        .await()

                    it.posicionConductor = geoPoint
                }

                //TODO Gestionar AQUI la proximidad a cliente y a destino para sacarlo de la vista
            } catch (e: Exception) {
                Log.e("RutaViewModel", "Error al actualizar ubicacion conductor", e)
            }
        }
    }

    //Cambia ubicación Cliente
    fun cambiaUbicacionCliente(nuevaUbicacion: LatLng) {
        val geoPoint: GeoPoint = GeoPoint(nuevaUbicacion.latitude, nuevaUbicacion.longitude)

        viewModelScope.launch {
            try {
                selectedRuta.value?.let {
                    FirebaseFirestore.getInstance().collection("rutas")
                        .document(it.id)
                        .update("origenGeo",geoPoint,)
                        .await()

                    it.origenGeo = geoPoint
                }
            } catch (e: Exception) {
                Log.e("RutaViewModel", "Error al actualizar ubicacion conductor", e)
            }
        }
    }


    /*
    fun setUserId(userId: String) {
        this.userId = userId
    }
    */

    //Carga la lista de rutas
    fun loadRutas() {
        val currentUser = Firebase.auth.currentUser ?: return
        val usuario = this.userId

        Log.d("rutaViewModel", "Cargando rutas de usuario $usuario")
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)
            //delay(2000)
            val rutasCargadas = loadRutasFirebase(usuario) //Cargamos las rutas de firebase
            _rutas.postValue(rutasCargadas)
            _isLoading.postValue(false)
        }
    }

    //Carga la lista de rutas de Firebase
    suspend fun loadRutasFirebase(userId: String): List<Ruta> {
        //val currentUser = Firebase.auth.currentUser ?: return emptyList()

        val db = Firebase.firestore
        val snapshot = db.collection("rutas").orderBy("fechaCreacion", Query.Direction.DESCENDING).get().await() //Ordenamos por fechaCreacion, descendiente

        val listaRutas = mutableListOf<Ruta>()

        for (document in snapshot.documents) {
            val cliente = document.getString("cliente")
            val conductor = document.getString("conductor")

            Log.d("LoadRutas", "Documento ID=${document.id}, cliente=$cliente, conductor=$conductor, userId=$userId")

            if (cliente == userId || conductor == userId) {
                //val origenGeo = document.getGeoPoint("origenGeo")
                //val destinoGeo = document.getGeoPoint("destinoGeo")

                Log.d("LoadRutas", "Agregando ruta ${document.id} -> $cliente / $conductor")

                listaRutas.add(
                    Ruta(
                        id = document.id,
                        fechaCreacion = document.getLong("fechaCreacion") ?: 1L,
                        conductor = document.getString("conductor") ?: "",
                        cliente = document.getString("cliente") ?: "",
                        origen = document.getString("origen") ?: "",
                        destino = document.getString("destino") ?: "",
                        origenGeo = document.get("origenGeo") as? GeoPoint ?: GeoPoint(0.0, 0.0),
                        destinoGeo = document.get("destinoGeo") as? GeoPoint ?: GeoPoint(0.0, 0.0),
                        momentoSalida = document.getLong("momentoSalida") ?: 1L,
                        momentoLlegada = document.getLong("momentoLlegada") ?: 1L,
                        posicionConductor = document.get("posicionConductor") as? GeoPoint ?: GeoPoint(0.0, 0.0),
                        distanciaConductor = document.getLong("distanciaConductor") ?: 0,
                        duracionConductor = (document.getLong("duracionConductor") ?: 0L).toInt(),
                        distancia = document.getLong("distancia") ?: 0,
                        duracion = (document.getLong("duracion") ?: 0L).toInt(),
                        asignado = document.getBoolean("asignado") ?: false,
                        haciaCliente = document.getBoolean("haciaCliente") ?: false,
                        haciaDestino = document.getBoolean("haciaDestino") ?: false,
                        finalizado = document.getBoolean("finalizado") ?: false,
                        cancelada = document.getBoolean("cancelada") ?: false,
                        visibleCliente = document.getBoolean("visibleCliente") ?: true,
                        visibleConductor = document.getBoolean("visibleConductor") ?: true
                    )
                )
            } else {
                Log.d("LoadRutas", "Ruta ${document.id} no coincide con userId")
            }
        }

        Log.d("LoadRutas", "Total rutas cargadas: ${listaRutas.size}")
        return listaRutas
    }

    fun actualizarPuedeVolver(volver: Boolean) {
        //_puedeVolver.postValue(volver)
        //_puedeVolver.value = volver
        puedeVolver = volver
    }

    /**
     * Cargar lista de rutas
     * Filtrar por -> cliente != usuario && ruta.finalizada == false && ruta.asignada == false
     * Ordenar por distancia de usuario a cliente ascendiente
     * Limitar a 5 rutas por lista
     * @param userId Id del usuario actual
     * @param userLatLng Coordenadas del usuario
     */
    fun cargarRutasBuscandoTaxi(userId: String, userLatLng: LatLng) {
        FirebaseFirestore.getInstance().collection("rutas")
            .whereEqualTo("finalizado", false)
            .whereEqualTo("asignado", false)
            .get()
            .addOnSuccessListener { snapshot ->
                val rutas = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Ruta::class.java)?.copy(id = doc.id)
                }
                    //Filtrar
                    .filter { it.cliente != userId }
                    //Ordenar por distancia al cliente
                    .sortedBy { ruta ->
                        val clienteLatLng = LatLng(
                            ruta.origenGeo.latitude,
                            ruta.origenGeo.longitude
                        )
                        calcularDistancia(userLatLng, clienteLatLng)
                    }
                    //Limitar a 5
                    .take(5)

                _rutasBuscandoTaxi.value = rutas
                _primeraCargaTaxis.value = false
            }
            .addOnFailureListener { e ->
                Log.e("RutaViewModel", "Error cargando rutas", e)
                _rutasBuscandoTaxi.value = emptyList()
            }
    }

    fun setPrimeraCarga(carga: Boolean) {
        _primeraCargaTaxis.value = carga
    }

    //Calcula la distancia entre dos LatLng en metros
    fun calcularDistancia(p1: LatLng, p2: LatLng): Long {
        val result = FloatArray(1)
        Location.distanceBetween(
            p1.latitude, p1.longitude,
            p2.latitude, p2.longitude,
            result
        )
        return result[0].toLong()
    }

    //Calcula datos de una ruta nueva
    suspend fun insertaRutaFirebase(
        //apiKey: String,
        ruta: Ruta,
        routesApiKey: String
    ): String? = try {
        // Obtener distancia y duración con la API
        val (distancia, duracion) = getDistanceAndDuration(ruta.origenGeo, ruta.destinoGeo, routesApiKey)
        ruta.distancia = distancia.toLong()
        ruta.distanciaOriginal = ruta.distancia
        ruta.duracion = duracion

        ruta.fechaCreacion = System.currentTimeMillis()

        Log.d("Firebase", "Distancia: ${ruta.distancia}, duración: ${ruta.duracion}")
        // Guardar ruta en Firestore y esperar resultado
        val docRef = db.collection("rutas").add(ruta).await()

        // Guardar el id generado en el objeto ruta
        ruta.id = docRef.id
        setIdRuta(ruta.id)

        //Marcamos la ruta como seleccionada en el viewmodel
        _selectedRuta.postValue(ruta)

        Log.d("Firebase", "Ruta insertada con ID: ${docRef.id}")

        docRef.id // devolver el id generado
    } catch (e: Exception) {
        Log.e("Firebase", "Error insertando ruta", e)
        null
    }

    fun setRuta(ruta: Ruta) {
        _selectedRuta.postValue(ruta)
        //_selectedRuta.value = ruta
    }

    fun deseleccionarRuta() {
        _selectedRuta.postValue(null)
        //_selectedRuta.value = null
    }

    fun getRuta(documentId: String): Ruta {
        //val currentUser = Firebase.auth.currentUser ?: return Ruta()

        var ruta = Ruta()

        db.collection("rutas").document(documentId).get().addOnSuccessListener { document ->
            ruta = Ruta(
                fechaCreacion = document.get("fechaCreacion") as Long,
                conductor = document.get("conductor").toString(),
                cliente = document.get("cliente").toString(),
                origenGeo = document.get("origenGeo") as GeoPoint,
                //origenGeo = document.get("origenGeo") as LatLng,
                destinoGeo = document.get("destinoGeo") as GeoPoint,
                //destinoGeo = document.get("destinoGeo") as LatLng,
                momentoSalida = document.get("momentoSalida") as Long,
                momentoLlegada = document.get("momentoLlegada") as Long,
                //precio = document.get("precio") as Number,
                distancia = document.get("distancia") as Long,
                asignado = document.get("asignado") as Boolean,
                haciaCliente = document.get("haciaCliente") as Boolean,
                haciaDestino = document.get("haciaDestino") as Boolean,
                finalizado = document.get("finalizado") as Boolean,
            )
        }

        return ruta
    }

    fun updateRuta(documentId: String, ruta: Ruta) {
        db.collection("rutas").document(documentId).set(ruta)
    }

    //Otra forma de comprobar si el usuario tiene rutas activas
    fun comprobarRutaActivaDelUsuario(userId: String, esConductor: Boolean) {
        //val currentUser = Firebase.auth.currentUser ?: return

        if (!esConductor) {
            viewModelScope.launch {
                _isLoading.postValue(true)
                FirebaseFirestore.getInstance()
                    .collection("rutas")
                    .whereEqualTo("cliente", userId)
                    .whereEqualTo("finalizado", false)
                    .limit(1) // Solo una activa a la vez
                    .get()
                    .addOnSuccessListener { documentos ->
                        if (!documentos.isEmpty) {
                            val doc = documentos.documents[0]
                            val ruta = doc.toObject(Ruta::class.java)?.copy(id = doc.id)
                            Log.d("Main", "Ruta activa -> $ruta")
                            _selectedRuta.postValue(ruta)
                        } else {
                            Log.d("Main", "Ruta nula")
                            _selectedRuta.postValue(null)
                        }
                        _isLoading.postValue(false)
                    }
                    .addOnFailureListener {
                        // TODO: Manejo de errores
                        _isLoading.postValue(false)
                        _selectedRuta.postValue(null)
                    }
            }
        } else {
            viewModelScope.launch {
                _isLoading.postValue(true)
                FirebaseFirestore.getInstance()
                    .collection("rutas")
                    .whereEqualTo("conductor", userId)
                    .whereEqualTo("finalizado", false)
                    .limit(1) // Solo una activa a la vez
                    .get()
                    .addOnSuccessListener { documentos ->
                        if (!documentos.isEmpty) {
                            val doc = documentos.documents[0]
                            val ruta = doc.toObject(Ruta::class.java)?.copy(id = doc.id)
                            Log.d("Main", "Ruta activa -> $ruta")
                            _selectedRuta.postValue(ruta)
                        } else {
                            Log.d("Main", "Ruta nula")
                            _selectedRuta.postValue(null)
                        }
                        _isLoading.postValue(false)
                    }
                    .addOnFailureListener {
                        // TODO: Manejo de errores
                        _isLoading.postValue(false)
                        _selectedRuta.postValue(null)
                    }
            }
        }
    }

    //Marca la ruta como cancelada
    fun marcarRutaCancelada(rutaId: String) {
        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("rutas")
                    .document(rutaId)
                    .update(
                        mapOf(
                            "finalizado" to true,
                            "cancelada" to true
                        )
                    )
                    .await()

                //Cancela la ruta seleccionada si no es null
                _selectedRuta.value?.let { ruta ->
                    ruta.cancelada = true
                    ruta.finalizado = true
                }

                Log.d("RutaViewModel", "Ruta $rutaId cancelada")
            } catch (e: Exception) {
                Log.e("RutaViewModel", "Error al cancelar la ruta $rutaId", e)
            }
        }
    }

    //
    /**
     * Elimina la ruta seleccionada -> Lo que hace es marcarla como no visible para quien la "elimina" pero seguirá en la BBDD
     * Primero averiguamos si el otro usuario implicado la tiene marcada como no visible
     * Si los dos la marcan como no visible se elimina por completo
     */
    fun eliminarRuta(rutaId: String, esConductor: Boolean) {
        val campo = if(esConductor) "visibleConductor" else "visibleCliente"
        val otroCampo = if(esConductor) "visibleCliente" else "visibleConductor"

        viewModelScope.launch {
            try {
                //Comprobamos si el otro usuario ha eliminado la ruta por su lado
                val doc = FirebaseFirestore.getInstance().collection("rutas").document(rutaId)
                    .get()
                    .await()

                val borrarRuta = doc.exists() && (doc.getBoolean(otroCampo) == false)

                if(borrarRuta) {
                    //Borramos por completo la ruta de firebase
                    FirebaseFirestore.getInstance()
                        .collection("rutas")
                        .document(rutaId)
                        .delete()
                        .await()

                    //Deselccionamos ruta seleccionada
                    _selectedRuta.value = null
                } else {
                    //Ocultamos la ruta para el usuario que llama la función
                    FirebaseFirestore.getInstance()
                        .collection("rutas")
                        .document(rutaId)
                        .update(
                            campo, false
                        )
                        .await()

                    //"Elimina" la ruta seleccionada si no es null
                    _selectedRuta.value?.let { ruta ->
                        if(esConductor) {
                            ruta.visibleConductor = false
                        } else {
                            ruta.visibleCliente = false
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("RutaViewModel", "Error al eliminar la ruta $rutaId", e)
            }
        }
    }

    //Asignar ruta
    fun asignarRuta(rutaId: String, user: User, routesApiKey: String) {
        claveAPICalculoDistanciaDuracion = routesApiKey

        viewModelScope.launch {
            try {
                //Hacemos que no puedan salir de DetallesRuta
                actualizarPuedeVolver(false)

                //Calculamos cuanto va a tardar el conductor en llegar al cliente
                val (distancia, duracion) = getDistanceAndDuration(selectedRuta.value!!.origenGeo, selectedRuta.value!!.destinoGeo, routesApiKey)
                selectedRuta.value!!.duracionConductor = duracion

                Log.d("RutaViewModel", "Duración del trayecto conductor -> cliente = $duracion")

                //Modificamos ruta.duracion <- sumamos el tiempo que tardará el conductor en llegar
                val duracionFinal = selectedRuta.value!!.duracion + duracion

                val db = FirebaseFirestore.getInstance()
                db.collection("rutas")
                    .document(rutaId)
                    .update(
                        mapOf(
                            "conductor" to user.id,
                            "asignado" to true,
                            "duracionConductor" to duracion,
                            "duracion" to duracionFinal
                        )
                    )
                    .await()

                //Asignamos la ruta seleccionada si no es null
                _selectedRuta.value?.let { ruta ->
                    ruta.asignado = true
                    ruta.conductor = user.id
                    ruta.duracionConductor = duracion
                    ruta.duracion = duracionFinal
                }

                //Obtenemos dibujo ruta conductor -> cliente
                getDirectionsRouteConductorCliente(rutaId)

                Log.d("RutaViewModel", "Ruta $rutaId asignada")
            } catch (e: Exception) {
                Log.e("RutaViewModel", "Error al eliminar la ruta $rutaId", e)
            }
        }
    }

    //Desasignar ruta
    fun desasignarRuta(rutaId: String) {
        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("rutas")
                    .document(rutaId)
                    .update(
                        mapOf(
                            "conductor" to "",
                            "asignado" to false
                        )
                    )
                    .await()

                //Desasignamos la ruta seleccionada si no es null
                _selectedRuta.value?.let { ruta ->
                    ruta.asignado = false
                    ruta.conductor = ""
                }

                //Borramos dibujo ruta conductor -> cliente
                borrarDirectionsRouteConductorCliente(rutaId)

            } catch (e: Exception) {
                Log.e("RutaViewModel", "Error al eliminar la ruta $rutaId", e)
            }
        }
    }

    //Iniciar camino hacia cliente
    fun iniciarRutaHaciaCliente(rutaId: String) {
        viewModelScope.launch {
            try {
                val momentoSalida = System.currentTimeMillis() / 1000

                val db = FirebaseFirestore.getInstance()
                db.collection("rutas")
                    .document(rutaId)
                    //.update("haciaCliente", true)
                    .update(
                        mapOf(
                            "haciaCliente" to true,
                            "momentoSalida" to momentoSalida
                            )
                    )
                    .await()

                //Modificamos la ruta seleccionada si no es null
                _selectedRuta.value?.let { ruta ->
                    ruta.haciaCliente = true
                    ruta.momentoSalida = momentoSalida
                }

            } catch (e: Exception) {
                Log.e("RutaViewModel", "Error al eliminar la ruta $rutaId", e)
            }
        }
    }

    //Modificamos ID de ruta
    fun setIdRuta(rutaId: String) {
        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("rutas")
                    .document(rutaId)
                    .update("id", rutaId)
                    .await()

                //Modificamos la ruta seleccionada si no es null
                _selectedRuta.value?.let { ruta ->
                    ruta.id = rutaId
                }

                Log.d("RutaViewModel", "Ruta $rutaId id -> $rutaId")
            } catch (e: Exception) {
                Log.e("RutaViewModel", "Error al modificar id de la ruta", e)
            }
        }
    }

    //Para calcular distancia y duración en tiempo de la ruta más rápida entre origen y destino según google maps
    suspend fun getDistanceAndDuration(
        origin: GeoPoint,
        destination: GeoPoint,
        apiKey: String
    ): Pair<Int, Int> = withContext(Dispatchers.IO) {
        val client = OkHttpClient()

        val url = "https://routes.googleapis.com/directions/v2:computeRoutes"

        val jsonBody = """
        {
          "origin": {
            "location": {
              "latLng": { "latitude": ${origin.latitude}, "longitude": ${origin.longitude} }
            }
          },
          "destination": {
            "location": {
              "latLng": { "latitude": ${destination.latitude}, "longitude": ${destination.longitude} }
            }
          },
          "travelMode": "DRIVE",
          "routingPreference": "TRAFFIC_AWARE",
          "computeAlternativeRoutes": false,
          "units": "METRIC"
        }
    """.trimIndent()

        val body = jsonBody.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("X-Goog-Api-Key", apiKey)
            .addHeader("X-Goog-FieldMask", "routes.duration,routes.distanceMeters")
            .post(body)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw Exception("HTTP error: ${response.code}")
                }

                val jsonResponse = JSONObject(response.body?.string() ?: "")
                val routesArray = jsonResponse.optJSONArray("routes")
                if (routesArray != null && routesArray.length() > 0) {
                    val route = routesArray.getJSONObject(0)
                    val distanceMeters = route.optInt("distanceMeters", 0)
                    val durationSeconds = route.optString("duration", "0s")
                        .replace("s", "")
                        .toIntOrNull() ?: 0

                    return@withContext Pair(distanceMeters, durationSeconds)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext Pair(0, 0)
    }

    fun formatDuration(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        return if (hours > 0) {
            String.format(Locale.getDefault(), "%02d:%02d", hours, minutes)
        } else {
            String.format(Locale.getDefault(), "%02d min", minutes)
        }
    }

    //Traduce de coordenadas a dirección con Geocoder
    suspend fun obtenerDireccion(latLng: GeoPoint, context: Context, googleApiKey: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())

                //API 33+ tiene versión suspend
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

                addresses?.firstOrNull()?.getAddressLine(0)
                    ?: obtenerDireccionGoogleMaps(latLng, googleApiKey) // fallback
            } catch (e: IOException) {
                //Fallback a Google Maps API si Geocoder falla
                obtenerDireccionGoogleMaps(latLng, googleApiKey)
            }
        }
    }

    //Traduce de coordenadas a dirección con Google Maps API, por si Geocoder falla
    private fun obtenerDireccionGoogleMaps(latLng: GeoPoint, apiKey: String): String {
        return try {
            val urlStr = "https://maps.googleapis.com/maps/api/geocode/json?latlng=${latLng.latitude},${latLng.longitude}&key=$apiKey"
            val response = URL(urlStr).readText()
            val json = JSONObject(response)
            val results = json.getJSONArray("results")
            if (results.length() > 0) results.getJSONObject(0).getString("formatted_address")
            else "Dirección desconocida"
        } catch (e: Exception) {
            "Dirección no disponible"
        }
    }

    //Traduce de dirección a coordenadas con Geocoder y fallback a Google Maps API
    suspend fun obtenerCoordenadas(
        direccion: String,
        context: Context,
        googleApiKey: String
    ): LatLng? {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())

                //Intentamos con Geocoder local
                @Suppress("DEPRECATION")
                val direcciones = geocoder.getFromLocationName(direccion, 1)
                if (!direcciones.isNullOrEmpty()) {
                    val location = direcciones.first()
                    LatLng(location.latitude, location.longitude)
                } else {
                    //Si falla usamos la API de Google Maps
                    obtenerCoordenadasGoogleMaps(direccion, googleApiKey)
                }
            } catch (e: IOException) {
                //Fallback a Google Maps API si Geocoder falla
                obtenerCoordenadasGoogleMaps(direccion, googleApiKey)
            }
        }
    }

    //Traduce de dirección a coordenadas usando Google Maps API
    private fun obtenerCoordenadasGoogleMaps(direccion: String, apiKey: String): LatLng? {
        return try {
            val urlStr =
                "https://maps.googleapis.com/maps/api/geocode/json?address=${URLEncoder.encode(direccion, "UTF-8")}&key=$apiKey"
            val response = URL(urlStr).readText()
            val json = JSONObject(response)
            val results = json.getJSONArray("results")
            if (results.length() > 0) {
                val location = results.getJSONObject(0)
                    .getJSONObject("geometry")
                    .getJSONObject("location")
                LatLng(location.getDouble("lat"), location.getDouble("lng"))
            } else null
        } catch (e: Exception) {
            null
        }
    }

    //Decodifica polylines
    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val latLng = LatLng(
                lat.toDouble() / 1E5,
                lng.toDouble() / 1E5
            )
            poly.add(latLng)
        }

        return poly
    }

    //Obtiene el dibujo de ruta entre dos puntos
    suspend fun getDirectionsRoute(p1: GeoPoint, p2: GeoPoint): List<LatLng> {
        //_selectedRuta.value ?: return emptyList()
        //Doy por hecho que p1 y p2 son correctos

        val apiKey = claveAPICalculoDistanciaDuracion
        val client = OkHttpClient()
        var decodedPath: List<LatLng> = emptyList()

        return withContext(Dispatchers.IO) {
            try {
                val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                        "origin=${p1.latitude},${p1.longitude}" +
                        "&destination=${p2.latitude},${p2.longitude}" +
                        "&mode=driving&key=$apiKey"

                val request = Request.Builder().url(url).build()
                client.newCall(request).execute().use { response ->
                    val body = response.body?.string() ?: return@withContext emptyList()

                    val json = JSONObject(body)
                    val status = json.getString("status")
                    if (status != "OK") {
                        Log.e("RutaViewModel", "*** Google Directions API error: $status")
                        return@withContext emptyList()
                    }

                    val routes = json.getJSONArray("routes")
                    if (routes.length() > 0) {
                        val encoded = routes.getJSONObject(0)
                            .getJSONObject("overview_polyline")
                            .getString("points")
                        reintentadoPolylines = false //Reseteamos para volver a reintentar si en el futuro falla
                        return@withContext decodePolyline(encoded)
                    } else {
                        if(reintentadoPolylines) {
                            reintentadoPolylines = false //Reseteamos para volver a reintentar si en el futuro falla
                            return@withContext emptyList()
                        } else {
                            reintentadoPolylines = true //Marcamos como que hemos reintentado, para no repetir esta petición
                            delay(1000)
                            return@withContext getDirectionsRoute(p1, p2) //Reintentamos la primera vez si falla
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("RutaViewModel", "Error al obtener ruta: ${e.message}", e)
                return@withContext emptyList()
            }
        }
    }

    //Obtiene la linea de ruta entre conductor y cliente
    fun getDirectionsRouteConductorCliente(rutaId: String) {
        var ruta = _selectedRuta.value ?: return

        viewModelScope.launch {
            val polylineLatLng: List<LatLng> = getDirectionsRoute(ruta.posicionConductor, ruta.origenGeo)

            //Convertimos a GeoPoint para Firebase
            ruta.polylineCliente = polylineLatLng.map { GeoPoint(it.latitude, it.longitude) }

            if(ruta.polylineCliente.isNotEmpty()) {
                //Guardamos en firebase
                FirebaseFirestore.getInstance()
                    .collection("rutas")
                    .document(rutaId)
                    .update("polylineCliente", ruta.polylineCliente)

                //Copiamos a selectedRuta
                _selectedRuta.postValue(ruta)
            }
        }
    }

    //Borramos linea de ruta entre conductor y cliente
    fun borrarDirectionsRouteConductorCliente(rutaId: String) {
        var ruta = _selectedRuta.value ?: return

        viewModelScope.launch {
            ruta.polylineCliente = emptyList()

            //Guardamos en firebase
            FirebaseFirestore.getInstance()
                .collection("rutas")
                .document(rutaId)
                .update("polylineCliente", ruta.polylineCliente)

            //Copiamos a selectedRuta
            _selectedRuta.postValue(ruta)
        }
    }

    //Obtiene la linea de ruta entre Conductor y destino
    fun getDirectionsRouteClienteDestino(rutaId: String) {
        var ruta = _selectedRuta.value ?: return

        viewModelScope.launch {
            val polylineLatLng: List<LatLng> = getDirectionsRoute(ruta.posicionConductor, ruta.destinoGeo)

            // Convertimos a GeoPoint para Firebase
            ruta.polylineDestino = polylineLatLng.map { GeoPoint(it.latitude, it.longitude) }

            if(ruta.polylineDestino.isNotEmpty()) {
                //Guardamos en firebase
                FirebaseFirestore.getInstance()
                    .collection("rutas")
                    .document(rutaId)
                    .update("polylineDestino", ruta.polylineDestino)

                //Copiamos a selectedRuta
                _selectedRuta.postValue(ruta)
            }
        }
    }

    //Borramos linea de ruta entre cliente y destino
    fun borrarDirectionsRouteClienteDestino(rutaId: String) {
        var ruta = _selectedRuta.value ?: return

        ruta.polylineDestino = emptyList()

        //Guardamos en firebase
        FirebaseFirestore.getInstance()
            .collection("rutas")
            .document(rutaId)
            .update("polylineDestino", ruta.polylineDestino)

        //Copiamos a selectedRuta
        _selectedRuta.postValue(ruta)
    }

    /**
     * Métodos obsoletos
     */

    //Traduce de dirección a coordenadas
    suspend fun getLatLngFromAddress(address: String, apiKey: String): LatLng? = withContext(Dispatchers.IO) {
        val encodedAddress = URLEncoder.encode(address, "UTF-8")
        val urlString = "https://maps.googleapis.com/maps/api/geocode/json" +
                "?address=$encodedAddress" +
                "&key=$apiKey"

        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection

        try {
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(response)
            val results = json.getJSONArray("results")
            if (results.length() > 0) {
                val location = results.getJSONObject(0)
                    .getJSONObject("geometry")
                    .getJSONObject("location")
                val lat = location.getDouble("lat")
                val lng = location.getDouble("lng")
                return@withContext LatLng(lat, lng)
            }
        } catch (e: Exception) {
            Log.e("Geocoding", "Error obteniendo coordenadas: ${e.message}")
        } finally {
            connection.disconnect()
        }
        return@withContext null
    }

    //Traduce de coordenadas a dirección
    suspend fun getAddressFromLatLng(latLng: LatLng, apiKey: String): String = withContext(Dispatchers.IO) {
        val urlString = "https://maps.googleapis.com/maps/api/geocode/json" +
                "?latlng=${latLng.latitude},${latLng.longitude}" +
                "&key=$apiKey"

        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection

        try {
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(response)
            val results = json.getJSONArray("results")
            if (results.length() > 0) {
                return@withContext results.getJSONObject(0).getString("formatted_address")
            }
        } catch (e: Exception) {
            Log.e("Geocoding", "Error obteniendo dirección: ${e.message}")
        } finally {
            connection.disconnect()
        }
        return@withContext ""
    }

    //Calcula la duración de la ruta y la devuelve en un formato legible
    fun getDuracionTiempo(tOrigen: Long, tFinal: Long): String {
        val totalSegundos = tFinal - tOrigen

        val horas = totalSegundos / 3600
        val minutos = (totalSegundos - (horas*3600)) / 60
        //val segundos = totalSegundos - (minutos * 60) - (horas * 3600)

        if(horas>0)
            return "" + horas + "h " + minutos + "m "// + segundos + "s"
        else
            return "" + minutos + "m "// + segundos + "s"
    }

    //Cancelamos ruta
    fun cancelarRuta(rutaId: String) {
        FirebaseFirestore.getInstance()
            .collection("rutas")
            .document(rutaId)
            .update("finalizado", true)
    }

    fun signalIsLoading() {
        _isLoading.postValue(true)
    }

    fun signalIsNotLoading() {
        _isLoading.postValue(false)
    }

    //Filtramos rutas por origen o destino
    fun searchRuta(searchString: String) {
        val searchList = mutableListOf<Ruta>()
        _rutas.value?.forEach {
            val ruta = it.copy()
            //ruta.visible = ( ruta.origen.contains(searchString, true) || ruta.destino.contains(searchString, true) )
            searchList.add(ruta)
        }
        _rutas.value = searchList
    }

    //Eliminamos filtro (mostrar todas las rutas)
    fun resetSearchList() {
        val searchList = mutableListOf<Ruta>()
        _rutas.value?.forEach {
            //val ruta = it.copy(visible = true)
            //searchList.add(ruta)
        }
        _rutas.value = searchList
    }

    fun getDia(segundos: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yy")
        //val sdf = getDateInstance()
        return sdf.format(segundos * 1000L)
    }

    fun getHora(segundos: Long): String {
        val sdf = SimpleDateFormat("HH:mm")
        //val sdf = getTimeInstance()
        return sdf.format(segundos * 1000L)
    }

    fun getFechaCompleta(segundos: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yy HH:mm")
        return sdf.format(segundos * 1000L)
    }

    //Obtiene la unidad más pequeña de "ciudad" que pueda obtener del GeoPoint
    fun getNombreCiudad(geoPoint: LatLng, context: Context): String? {
        var cityName: String?
        val geoCoder = Geocoder(context, Locale.getDefault())
        val address = geoCoder.getFromLocation(geoPoint.latitude,geoPoint.longitude,1)

        if(address?.get(0)?.subLocality != null)
            return address.get(0)?.subLocality
        if(address?.get(0)?.locality != null)
            return address.get(0)?.locality //Ej: Picassent
        if(address?.get(0)?.subAdminArea != null)
            return address.get(0)?.subAdminArea //Ej: Valencia
        if(address?.get(0)?.adminArea != null)
            return address.get(0)?.adminArea //Comunidad Valenciana
        if(address?.get(0)?.countryName != null)
            return address.get(0)?.countryName
        return ""
    }

    //Asigna un conductor a una ruta
    fun setAsignado(documentId: String, conductor: String) {
        var rutaTemp = getRuta(documentId)
        rutaTemp.conductor = conductor
        rutaTemp.asignado = true
        rutaTemp.haciaCliente = false
        rutaTemp.haciaDestino = false
        rutaTemp.finalizado = false
        updateRuta(documentId, rutaTemp)
    }

    //El conductor incia ruta hacia el cliente
    fun setHaciaCliente(documentId: String) {
        var rutaTemp = getRuta(documentId)
        rutaTemp.haciaCliente = true
        updateRuta(documentId, rutaTemp)
    }

    //El conductor inicia la ruta hacia el destino
    fun setHaciaDestino(documentId: String) {
        var rutaTemp = getRuta(documentId)
        rutaTemp.haciaCliente = false
        rutaTemp.haciaDestino = true
        updateRuta(documentId, rutaTemp)
    }

    //Marca la ruta como finalizada
    fun setFinalizado(documentId: String) {
        var rutaTemp = getRuta(documentId)
        rutaTemp.haciaDestino = false
        rutaTemp.finalizado = true
        updateRuta(documentId, rutaTemp)
    }

    //Marca la ruta como finalizada
    fun marcarRutaFinalizada(rutaId: String) {
        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("rutas")
                    .document(rutaId)
                    .update("finalizado", true)
                    .await()
                Log.d("RutaViewModel", "Ruta $rutaId marcada como finalizada")
            } catch (e: Exception) {
                Log.e("RutaViewModel", "Error al marcar ruta como finalizada", e)
            }
        }
    }

    //Devuelve las rutas activas
    fun hayRutaActiva(listaRutas: List<Ruta>): Ruta? {
        return listaRutas.find { !it.finalizado }
    }

    //Borra todas las rutas de firebase
    fun borrarTodasLasRutas() {
        val db = FirebaseFirestore.getInstance()
        val collectionRef = db.collection("rutas")

        collectionRef.get()
            .addOnSuccessListener { snapshot ->
                val batch = db.batch()
                snapshot.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }
                batch.commit()
                    .addOnSuccessListener {
                        Log.d("RutaViewModel", "Todas las rutas eliminadas correctamente")
                    }
                    .addOnFailureListener { e ->
                        Log.e("RutaViewModel", "Error al eliminar rutas", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("RutaViewModel", "Error al obtener rutas", e)
            }
    }
}