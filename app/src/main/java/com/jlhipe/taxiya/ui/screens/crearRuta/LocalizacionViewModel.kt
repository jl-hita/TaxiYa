package com.jlhipe.taxiya.ui.screens.crearRuta

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Locale

class LocalizacionViewModel(application: Application) : AndroidViewModel(application) {
    @SuppressLint("StaticFieldLeak")
    private val context = getApplication<Application>().applicationContext

    //private val _ubicacion = MutableLiveData<LatLng>()
    //val ubicacion: LiveData<LatLng> = _ubicacion

    //Utilizo un truquillo de hacer una lista de LatLng, ya que no me deja hacerlo directamente con LatLng
    //private val _ubicacion = MutableLiveData<List<LatLng>>()
    //val ubicacion: LiveData<List<LatLng>> = _ubicacion
    private val _ubicacion = MutableLiveData<LatLng>()
    val ubicacion: LiveData<LatLng> = _ubicacion

    private val _latitud = MutableLiveData<Double>()
    val latitud: LiveData<Double> = _latitud

    private val _longitud = MutableLiveData<Double>()
    val longitud: LiveData<Double> = _longitud

    private val _destino = MutableLiveData<String>()
    val destino: LiveData<String> = _destino

    private val _destinoLocation = MutableLiveData<List<LatLng>>()
    val destinoLocation: LiveData<List<LatLng>> = _destinoLocation

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)
    private var locationCallback: LocationCallback? = null

    init {
        setUbicacion(0.0, 0.0)
        setDestino(0.0, 0.0)
    }

    fun tienePermisosGPS():Boolean {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
        return true
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
            .setMinUpdateDistanceMeters(1f)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.locations.firstOrNull()?.let { location ->
                    val nuevaUbicacion = LatLng(location.latitude, location.longitude)
                    //_ubicacion.postValue(listOf(nuevaUbicacion))
                    //_ubicacion.postValue(nuevaUbicacion)
                    _ubicacion.value = nuevaUbicacion
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            request,
            locationCallback!!,
            Looper.getMainLooper()
        )
    }

    fun stopLocationUpdates() {
        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
    }

    fun setUbicacion(lat: Double, lon: Double) {
        //_ubicacion.value = listOf(LatLng(lat, lon))
        _ubicacion.value = LatLng(lat, lon)
    }

    /*
    fun setLatitud(lat: Double) {
        _latitud.value = lat

    }
     */

    /*
    fun setLongitud(lon: Double) {
        _longitud.value = lon
    }
     */

    /*
    fun setUbicacion(lat: Double, lon: Double) {
        val tempUbi = mutableListOf<LatLng>()
        tempUbi.add(LatLng(lat, lon))
        //_ubicacion.value = tempUbi //Provoca java.lang.IllegalStateException: Cannot invoke setValue on a background thread
        _ubicacion.postValue(tempUbi)
    }
     */

    fun setDestino(lat: Double, lon: Double) {
        val tempUbi = mutableListOf<LatLng>()
        tempUbi.add(LatLng(lat, lon))
        //_destinoLocation.value = tempUbi
        _destinoLocation.postValue(tempUbi)
    }

    fun setDestinoInput(destino: String) {
        _destino.value = destino
    }

    fun requestGeocodeLocation(location: LatLng):String {
        val geocoder = Geocoder(context, Locale.getDefault())
        val coordinate = LatLng(location.latitude, location.longitude)
        val addresses: MutableList<Address>? =
            geocoder.getFromLocation(coordinate.latitude,coordinate.longitude,1)
        return if(addresses?.isNotEmpty()== true) {
            addresses[0].getAddressLine(0)
        } else {
            "Dirección no encontrada"
        }
    }
/*
 * usa Geocoder.getFromLocationName, que bloquea el hilo actual (está deprecated en Android 14+)
 *
    fun requestCoordsFromAdress(direccion: String) {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocationName(direccion, 1)
        //val address = addresses!![0]
        if (addresses?.get(0) != null) {
            setDestino(addresses[0].latitude, addresses[0].longitude)
        }
    }
 */

    fun requestCoordsFromAdress(direccion: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocationName(direccion, 1)
                addresses?.firstOrNull()?.let {
                    setDestino(it.latitude, it.longitude)
                }
            } catch (e: IOException) {
                Log.e("Geo", "Error geocoding: ${e.message}")
            }
        }
    }
}