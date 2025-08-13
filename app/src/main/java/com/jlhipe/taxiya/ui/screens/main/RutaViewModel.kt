package com.jlhipe.taxiya.ui.screens.main

import android.content.Context
import android.location.Geocoder
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.jlhipe.taxiya.model.Ruta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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

class RutaViewModel: ViewModel() {
    //Lista de rutas
    private val _rutas = MutableLiveData<List<Ruta>>()
    val rutas: LiveData<List<Ruta>> = _rutas

    //Ruta seleccionada
    private val _selectedRuta = MutableLiveData<Ruta?>()
    val selectedRuta: LiveData<Ruta?> = _selectedRuta

    //Indica que se están obteniendo los datos de Firebase
    private var _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    //BBDD Firebase
    private val db: FirebaseFirestore = Firebase.firestore

    val userId: String
        get() = Firebase.auth.currentUser?.uid.orEmpty()

    //Variable para manejar permisos
    val visiblePermissionDialogeQueue = mutableStateListOf<String>()

    init {
        loadRutas()
    }

    /*
    fun setUserId(userId: String) {
        this.userId = userId
    }
    */

    //Carga la lista de rutas
    fun loadRutas() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)
            delay(2000)
            //_rutas.postValue(Ruta.getData()) //Carga las rutas del modelo, obsoleto
            _rutas.postValue(loadRutasFirebase()) //Cargamos las rutas de firebase
            _isLoading.postValue(false)
        }
    }

    //Carga la lista de rutas de Cloud Firestore
    fun loadRutasFirebase(): List<Ruta> {
        var listaRutas: MutableList<Ruta> = ArrayList()
        db.collection("rutas").get().addOnSuccessListener {
            //_isLoading.postValue(true)
            //rutasFirebase.removeAll(rutasFirebase)
            for(document in  it.documents) {
                //if(document.get("userId") == this.userId) { //Alternativa: comprobar si coincide con el campo cliente o conductor
                //Cargamos solo las rutas en las que el usuario sea el conductor o el cliente
                if(document.get("cliente") == this.userId || document.get("conductor") == this.userId) {
                    val origenMap = document.get("origenGeo") as Map<String, Double>
                    val destinoMap = document.get("destinoGeo") as Map<String, Double>

                    listaRutas.add(
                        //document.toObject<Ruta>()!!
                        Ruta(
                            conductor = document.get("conductor").toString(),
                            cliente = document.get("cliente").toString(),
                            //origenGeo = document.get("origenGeo") as GeoPoint,
                            //origenGeo = document.get("origenGeo") as LatLng,
                            //destinoGeo = document.get("destinoGeo") as GeoPoint,
                            //destinoGeo = document.get("destinoGeo") as LatLng,
                            origenGeo = LatLng(origenMap["latitude"]!!, origenMap["longitude"]!!),
                            destinoGeo = LatLng(destinoMap["latitude"]!!, destinoMap["longitude"]!!),
                            momentoSalida = 1742317200,
                            momentoLlegada = 1742318400,
                            //precio = document.get("precio") as Number,
                            distancia = document.get("distancia") as Number,
                            asignado = document.get("asignado") as Boolean,
                            haciaCliente = document.get("haciaCliente") as Boolean,
                            haciaDestino = document.get("haciaDestino") as Boolean,
                            finalizado = document.get("finalizado") as Boolean,
                        )
                    )
                }
            }
            //_isLoading.postValue(false)
        }
        return listaRutas
    }


    /*
    fun insertaRutaFirebase(
        apiKey: String,
        ruta: Ruta
    ) {
        viewModelScope.launch {
            try {
                //çobtenemos distancia y duración del trayecto calculados con la api
                val (distancia, duracion) = getDistanceAndDuration(ruta.origenGeo, ruta.destinoGeo, apiKey)
                ruta.distancia = distancia
                ruta.duracion = duracion

                //Traducimos de coordenadas a direcciones en formato legible
                //val origenDireccion = getAddressFromLatLng(ruta.origenGeo, apiKey)
                //val destinoDireccion = getAddressFromLatLng(ruta.destinoGeo, apiKey)
                //ruta.origen = origenDireccion
                //ruta.destino = destinoDireccion

                //Marcamos la ruta como seleccionada en el viewmodel
                _selectedRuta.postValue(ruta)

                //Insertamos en Firebase
                //FirebaseFirestore.getInstance()
                    //.collection("rutas")
                db.collection("rutas")
                    .add(ruta)
                    .addOnSuccessListener { docRef ->
                        Log.d("Firebase", "Ruta insertada con ID: ${docRef.id}")
                        //signalIsNotLoading()
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firebase", "Error insertando ruta", e)
                        //signalIsNotLoading()
                    }
            } catch (e: Exception) {
                Log.e("Firebase", "Error obteniendo distancia/duración", e)
            }
        }
    }
    */

    /*
     * Calcula datos de una ruta nueva
     * y la onserta en firebase
     */
    suspend fun insertaRutaFirebase(
        apiKey: String,
        ruta: Ruta,
        routesApiKey: String
    ): String? = try {
        // Obtener distancia y duración con la API
        val (distancia, duracion) = getDistanceAndDuration(ruta.origenGeo, ruta.destinoGeo, routesApiKey)
        ruta.distancia = distancia
        ruta.duracion = duracion

        Log.d("Firebase", "Distancia: ${ruta.distancia}, duración: ${ruta.duracion}")
        // Guardar ruta en Firestore y esperar resultado
        val docRef = db.collection("rutas").add(ruta).await()

        // Guardar el id generado en el objeto ruta
        ruta.id = docRef.id

        //Marcamos la ruta como seleccionada en el viewmodel
        _selectedRuta.postValue(ruta)

        Log.d("Firebase", "Ruta insertada con ID: ${docRef.id}")

        docRef.id // devolver el id generado
    } catch (e: Exception) {
        Log.e("Firebase", "Error insertando ruta", e)
        null
    }

    fun getRuta(documentId: String): Ruta {
        var ruta = Ruta()

        db.collection("rutas").document(documentId).get().addOnSuccessListener { document ->
            ruta = Ruta(
                conductor = document.get("conductor").toString(),
                cliente = document.get("cliente").toString(),
                //origenGeo = document.get("origenGeo") as GeoPoint,
                origenGeo = document.get("origenGeo") as LatLng,
                //destinoGeo = document.get("destinoGeo") as GeoPoint,
                destinoGeo = document.get("destinoGeo") as LatLng,
                momentoSalida = document.get("momentoSalida") as Long,
                momentoLlegada = document.get("momentoLlegada") as Long,
                //precio = document.get("precio") as Number,
                distancia = document.get("distancia") as Number,
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

    //Devuelve las rutas activas
    fun hayRutaActiva(listaRutas: List<Ruta>): Ruta? {
        return listaRutas.find { !it.finalizado }
    }

    //Otra forma de comprobar si el usuario tiene rutas activas
    fun comprobarRutaActivaDelUsuario(userId: String) {
        viewModelScope.launch {
            FirebaseFirestore.getInstance()
                .collection("rutas")
                .whereEqualTo("usuarioId", userId)
                .whereEqualTo("finalizada", false)
                .limit(1) // Solo una activa a la vez
                .get()
                .addOnSuccessListener { documentos ->
                    if (!documentos.isEmpty) {
                        val doc = documentos.documents[0]
                        val ruta = doc.toObject(Ruta::class.java)?.copy(id = doc.id)
                        _selectedRuta.postValue(ruta)
                    } else {
                        _selectedRuta.postValue(null)
                    }
                }
                .addOnFailureListener {
                    // TODO: Manejo de errores
                    _selectedRuta.postValue(null)
                }
        }
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
            ruta.visible = ( ruta.origen.contains(searchString, true) || ruta.destino.contains(searchString, true) )
            searchList.add(ruta)
        }
        _rutas.value = searchList
    }

    //Eliminamos filtro (mostrar todas las rutas)
    fun resetSearchList() {
        val searchList = mutableListOf<Ruta>()
        _rutas.value?.forEach {
            val ruta = it.copy(visible = true)
            searchList.add(ruta)
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

    //Para obtener actualizaciones de la ubicación mediante GPS



    //Calcula distancia y duración en tiempo de la ruta
    /*
    suspend fun getDistanceAndDuration(
        origin: LatLng,
        destination: LatLng,
        apiKey: String
    ): Pair<Int, Int> = withContext(Dispatchers.IO) {
        //var apiKey: String = stringResource(R.string.apimakey);

        val urlString = "https://maps.googleapis.com/maps/api/directions/json" +
                "?origin=${origin.latitude},${origin.longitude}" +
                "&destination=${destination.latitude},${destination.longitude}" +
                "&mode=driving" +
                "&key=$apiKey"

        Log.d("API", "Request URL: $urlString")

        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection

        try {
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(response)
            val status = json.getString("status")
            Log.d("API", "Status: $status")

            if (status != "OK") {
                Log.e("API", "Error API: ${json.optString("error_message")}")
                return@withContext Pair(0, 0)
            }

            val routes = json.getJSONArray("routes")
            val legs = routes.getJSONObject(0).getJSONArray("legs")
            val leg = legs.getJSONObject(0)
            val distance = leg.getJSONObject("distance").getInt("value")
            val duration = leg.getJSONObject("duration").getInt("value")
            return@withContext Pair(distance, duration)

        } catch (e: Exception) {
            Log.e("API", "Error al obtener ruta: ${e.message}")
        } finally {
            connection.disconnect()
        }
        return@withContext Pair(0, 0)
    }
     */

    suspend fun getDistanceAndDuration(
        origin: LatLng,
        destination: LatLng,
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

    //Alternativa, traduce de coordenadas a dirección
    fun obtenerDireccion(latLng: LatLng, context: Context): String {
        val geocoder = Geocoder(context, Locale.getDefault())
        val direcciones = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
        return direcciones?.firstOrNull()?.getAddressLine(0) ?: "Dirección desconocida"
    }

    fun obtenerCoordenadas(direccion: String, context: Context): LatLng? {
        val geocoder = Geocoder(context, Locale.getDefault())
        return try {
            val resultados = geocoder.getFromLocationName(direccion, 1)
            if (!resultados.isNullOrEmpty()) {
                val location = resultados.first()
                LatLng(location.latitude, location.longitude)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
/*
     * Ejemplo de uso desde viewmodel o composable
     */
/*
val origin = LatLng(40.416775, -3.703790)
val destination = LatLng(40.437869, -3.819620)
val apiKey = "TU_API_KEY"

viewModelScope.launch {
    val resultado = getDistanceAndDuration(origin, destination, apiKey)
    if (resultado != null) {
        val (distancia, duracion) = resultado
        Log.d("Ruta", "Distancia: ${distancia/1000.0} km, Duración: ${duracion/60} min")
    }
}
*/

/*
    //Inserta ruta en Cloud Firestore y devuelve un string con la ID del document
    fun insertaRutaFirebase(
        //userID: String,
        //identificador: String,
        cliente: String, // = "5vUgPOL0a4SijzsA9Zjxx92aAbU2",
        conductor: String, // = "1qw6g1r8ge",
        origen: String = "Carrer del Mestre Ramírez, 2, 46220 Picassent, Valencia, España",
        destino: String = "Avinguda de Paiporta, 80, 46910 Benetússer, Valencia",
        origenGeo: GeoPoint = GeoPoint(39.3669795, -0.4610799),
        //origenGeoLat: Number = 39.3669795,
        //origenGeoLon: Number = -0.4610799,
        destinoGeo: GeoPoint = GeoPoint(39.4256181, -0.4028219),
        //destinoGeoLat: Number = 39.4256181,
        //destinoGeoLon: Number = -0.4028219,
        momentoSalida: Long? = 1742288400,
        //momentoLlegada = 1742289600,
        momentoLlegada: Long? = 1742292431,
        //precio: Number = 15.90,
        distancia: Number = 11.8,
        asignado: Boolean = false,
        haciaCliente: Boolean = false,
        haciaDestino: Boolean = false,
        finalizado: Boolean = true
    ): String {
        //Variable que guardará la ID del document
        var documentID: String = ""

        val ruta = Ruta(
            cliente = cliente,
            conductor = conductor,
            origen = origen,
            destino = destino,
            origenGeo = origenGeo,
            destinoGeo = destinoGeo,
            momentoSalida = momentoSalida,
            momentoLlegada = momentoLlegada,
            //precio = precio,
            distancia = distancia,
            asignado = asignado,
            haciaCliente = haciaCliente,
            haciaDestino = haciaDestino,
            finalizado = finalizado,
            visible = true,
        )

        //La insertamos en la BBDD con una ID generada
        db.collection("rutas")
            .add(ruta)
            .addOnSuccessListener { documentReference ->
                documentID = documentReference.id
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }

        //Devolvemos la ID del documento para casos en los que debamos editar la ruta después de crearla
        return documentID
    }
    */