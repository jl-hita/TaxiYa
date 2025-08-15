package com.jlhipe.taxiya.model

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint

//import com.google.type.LatLng

data class Ruta(
    var id: String = "", //ID en Firebase de la ruta
    var fechaCreacion: Long? = 1, //Momento en el que fue creada la ruta, para ordenarla correctamente
    var conductor: String? = "", //ID única del conductor, no mostrar en UI
    var cliente: String = "",   //ID única del cliente, no mostrar en UI
    var origen: String = "",    //Dirección con el formato texto normal
    var destino: String = "",   //Dirección con el formato texto normal
    var origenGeo: GeoPoint = GeoPoint(0.0,0.0),    //Dirección en formato GeoPoint(latitud: Double, longitud: Double)
    //var origenGeo: com.google.android.gms.maps.model.LatLng = com.google.android.gms.maps.model.LatLng(0.0, 0.0),
    //var origenGeo: GeoPointData = GeoPointData(),
    var destinoGeo: GeoPoint = GeoPoint(0.0, 0.0),    //Dirección en formato GeoPoint(latitud: Double, longitud: Double)
    //var destinoGeo: com.google.android.gms.maps.model.LatLng = com.google.android.gms.maps.model.LatLng(0.0, 0.0),
    //var destinoGeo: GeoPointData = GeoPointData(),
    var momentoSalida: Long? = 1,   //Para indicar día y hora de la ruta, segundos desde 1970-01-01 hasta el momento
    var momentoLlegada: Long? = 1,
    //var precio: Number = 0,    //Precio en céntimos de euro
    var posicionConductor: GeoPoint = GeoPoint(0.0, 0.0), //Situacion espacial del conductor
    var distanciaConductor: Long = 0, //Distancia en metros del conductor respecto del cliente
    var duracionConductor: Int = 0, //Distancia en tiempo del conductor respecto del cliente
    var distancia: Long = 0,    //Distancia en metros con un decimal
    var duracion: Int = 0, //Duración en tiempo(segundos) calculada del viaje (se calcula antes de salir, se actualiza al terminar)
    var asignado:  Boolean = false,    //¿Está la ruta asignada a un taxista?
    var haciaCliente: Boolean = false,    //¿Está el taxi yendo a recoger al usuario?
    var haciaDestino: Boolean = false,    //¿Está el usuario en el taxi en ruta?
    var finalizado: Boolean = false,    //¿Ha finalizado la ruta?
    var cancelada: Boolean = false,
    var visible: Boolean = true,
) {
}

//Clase propia para sustituir LatLng y GeoPoint en mi modelo
data class GeoPointData(
    var latitude: Double = 0.0,
    var longitude: Double = 0.0
) {
    fun toLatLng(): LatLng = LatLng(latitude, longitude)
}