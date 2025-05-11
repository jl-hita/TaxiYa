package com.jlhipe.taxiya.ui.screens.nuevaruta

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import java.util.Locale

class LocalizacionViewModel(application: Application) : AndroidViewModel(application) {
    @SuppressLint("StaticFieldLeak")
    private val context = getApplication<Application>().applicationContext

    //private val _ubicacion = MutableLiveData<LatLng>()
    //val ubicacion: LiveData<LatLng> = _ubicacion

    //Utilizo un truquillo de hacer una lista de LatLng, ya que no me deja hacerlo directamente con LatLng
    private val _ubicacion = MutableLiveData<List<LatLng>>()
    val ubicacion: LiveData<List<LatLng>> = _ubicacion

    private val _latitud = MutableLiveData<Double>()
    val latitud: LiveData<Double> = _latitud

    private val _longitud = MutableLiveData<Double>()
    val longitud: LiveData<Double> = _longitud

    private val _destino = MutableLiveData<String>()
    val destino: LiveData<String> = _destino

    private val _destinoLocation = MutableLiveData<List<LatLng>>()
    val destinoLocation: LiveData<List<LatLng>> = _destinoLocation


    init {
        setUbicacion(0.0, 0.0)
    }

    fun tienePermisosGPS():Boolean {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
        return true
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

    fun setUbicacion(lat: Double, lon: Double) {
        val tempUbi = mutableListOf<LatLng>()
        tempUbi.add(LatLng(lat, lon))
        _ubicacion.value = tempUbi
    }

    fun setDestino(lat: Double, lon: Double) {
        val tempUbi = mutableListOf<LatLng>()
        tempUbi.add(LatLng(lat, lon))
        _ubicacion.value = tempUbi
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

    fun requestCoordsFromAdress(direccion: String) {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocationName(direccion, 1)
        //val address = addresses!![0]
        if (addresses?.get(0) != null) {
            setDestino(addresses[0].latitude, addresses[0].longitude)
        }
    }
}