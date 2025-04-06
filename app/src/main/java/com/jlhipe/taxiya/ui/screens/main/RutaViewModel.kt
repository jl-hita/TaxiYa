package com.jlhipe.taxiya.ui.screens.main

import android.content.ContentValues.TAG
import android.content.Context
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.jlhipe.taxiya.model.Ruta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class RutaViewModel: ViewModel() {
    //Lista de rutas
    private val _rutas = MutableLiveData<List<Ruta>>()
    val rutas: LiveData<List<Ruta>> = _rutas

    //Ruta seleccionada
    private val _selectedRuta = MutableLiveData<Ruta>()
    val selectedRuta: LiveData<Ruta> = _selectedRuta

    //Indica que se están obteniendo los datos de Firebase
    private var _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    //BBDD Firebase
    private val db: FirebaseFirestore = Firebase.firestore

    init {
        loadRutas()
    }

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
                listaRutas.add(
                    Ruta(
                        conductor = document.get("user").toString(),
                        cliente = document.get("cliente").toString(),
                        origenGeo = document.get("origenGeo") as GeoPoint,
                        destinoGeo = document.get("destinoGeo") as GeoPoint,
                        momentoSalida = 1742317200,
                        momentoLlegada = 1742318400,
                        precio = document.get("precio") as Number,
                        distancia = document.get("distancia") as Number,
                        asignado = document.get("asignado") as Boolean,
                        haciaCliente = document.get("haciaCliente") as Boolean,
                        haciaDestino = document.get("haciaDestino") as Boolean,
                        finalizado = document.get("finalizado") as Boolean,
                    )
                )
            }
            //_isLoading.postValue(false)
        }
        return listaRutas
    }

    //Inserta ruta en Cloud Firestore y devuelve un string con la ID del document
    fun insertaRutaFirebase(
        userID: String,
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
        momentoSalida: Long = 1742288400,
        //momentoLlegada = 1742289600,
        momentoLlegada: Long = 1742292431,
        precio: Number = 15.90,
        distancia: Number = 11.8,
        asignado: Boolean = false,
        haciaCliente: Boolean = false,
        haciaDestino: Boolean = false,
        finalizado: Boolean = true
    ): String {
        //Variable que guardará la ID del document
        var documentID: String = ""
        //Creamos hashMap de la Ruta
        /*
        val ruta = hashMapOf(
            "cliente" to cliente,
            "conductor" to conductor,
            "origen" to origen,
            "destino" to destino,
            "origenGeo" to origenGeo,
            //"origenGeoLat" to origenGeoLat,
            //"origenGeoLon" to origenGeoLon,
            "destinoGeo" to destinoGeo,
            //"destinoGeoLat" to destinoGeoLat,
            //"destinoGeoLon" to destinoGeoLon,
            "momentoSalida" to momentoSalida,
            "momentoLlegada" to momentoLlegada,
            "precio" to precio,
            "distancia" to distancia,
            "asignado" to asignado,
            "haciaCliente" to haciaCliente,
            "haciaDestino" to haciaDestino,
            "finalizado" to finalizado
        )
         */
        val ruta = Ruta(
            userId = userID,
            cliente = cliente,
            conductor = conductor,
            origen = origen,
            destino = destino,
            origenGeo = origenGeo,
            destinoGeo = destinoGeo,
            momentoSalida = momentoSalida,
            momentoLlegada = momentoLlegada,
            precio = precio,
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

        return documentID
        //Recargamos la lista de rutas
        //loadRutas()
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
    fun getNombreCiudad(geoPoint: GeoPoint, context: Context): String? {
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

        /*
        val horas = totalSegundos / 3600
        val restoHoras = (totalSegundos % 3600) * 3600
        val minutos = restoHoras / 60
        val segundos = (minutos % 60) * 60
        */

        if(horas>0)
            return "" + horas + "h " + minutos + "m "// + segundos + "s"
        else
            return "" + minutos + "m "// + segundos + "s"
    }
}