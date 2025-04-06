package com.jlhipe.taxiya.model

import android.location.Geocoder
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import java.util.Date

data class Ruta (
    var identificador: String = "", //Identificador único: se genera y se comprueba que no exista
    var conductor: String = "", //ID única del conductor, no mostrar en UI
    var cliente: String = "",   //ID única del cliente, no mostrar en UI
    var origen: String = "",    //Dirección con el formato texto normal
    var destino: String = "",   //Dirección con el formato texto normal
    var origenGeo: GeoPoint,    //Dirección en formato GeoPoint(latitud: Double, longitud: Double)
    var destinoGeo: GeoPoint,    //Dirección en formato GeoPoint(latitud: Double, longitud: Double)
    //val origenCoord: String = "",    //Dirección con el formato coordenadas
    //val destinoCoord: String = "",   //Dirección con el formato coordenadas
    //val dia: Date,     //Día que se inició la ruta
    //val dia: String = "",     //Día que se inició la ruta con formato aaaa/mm/dd
    var momentoSalida: Long,   //Para indicar día y hora de la ruta, segundos desde 1970-01-01 hasta el momento
    var momentoLlegada: Long,
    var precio: Number = 0,    //Precio en céntimos de euro
    var distancia: Number = 0.0,    //Distancia en km con un decimal
    var asignado:  Boolean = false,    //¿Está la ruta asignada a un taxista?
    var haciaCliente: Boolean = false,    //¿Está el taxi yendo a recoger al usuario?
    var haciaDestino: Boolean = false,    //¿Está el usuario en el taxi en ruta?
    var finalizado: Boolean = false,    //¿Ha finalizado la ruta?
    var visible: Boolean = true,
){
    /*
     * Para tener datos de prueba para la UI
     */
    companion object {
        var listaRutas: MutableList<Ruta> = ArrayList()

        fun getRutas(): List<Ruta> {
            listaRutas.clear()
            listaRutas.add(
                Ruta(
                    identificador = "x3on15w85d",
                    conductor = "1qw6g1r8ge",
                    cliente = "kv93j43fwe",
                    origen = "Carrer del Mestre Ramírez, 2, 46220 Picassent, Valencia, España",
                    destino = "Avinguda de Paiporta, 80, 46910 Benetússer, Valencia",
                    origenGeo = GeoPoint(39.3669795, -0.4610799),
                    destinoGeo = GeoPoint(39.4256181, -0.4028219),
                    momentoSalida = 1742288400,
                    //momentoLlegada = 1742289600,
                    momentoLlegada = 1742292431,
                    precio = 159,
                    distancia = 11.8,
                    asignado = false,
                    haciaCliente = false,
                    haciaDestino = false,
                    finalizado = true
                )
            )
            listaRutas.add(
                Ruta(
                    identificador = "fgj346bytj",
                    conductor = "1qw6g1r8ge",
                    cliente = "kv93j43fwe",
                    origen = "Avinguda de Paiporta, 80, 46910 Benetússer, Valencia",
                    destino = "Carrer del Mestre Ramírez, 2, 46220 Picassent, Valencia, España",
                    origenGeo = GeoPoint(39.4256181, -0.4028219),
                    destinoGeo = GeoPoint(39.3669795, -0.4610799),
                    momentoSalida = 1742317200,
                    momentoLlegada = 1742318400,
                    precio = 159,
                    distancia = 11.8,
                    asignado = false,
                    haciaCliente = false,
                    haciaDestino = false,
                    finalizado = true
                )
            )
            return listaRutas
        }

        fun getData(): List<Ruta> {
            val listaRutas = getRutas()
            Companion.listaRutas.sortBy {
                it.momentoSalida
            }
            return listaRutas
        }
    }

    /*
    companion object {
        fun getData() : List<Ruta> {
            return listOf(
                Ruta(
                    identificador = "x3on15w85d",
                    conductor = "1qw6g1r8ge",
                    cliente = "kv93j43fwe",
                    origen = "Carrer del Mestre Ramírez, 2, 46220 Picassent, Valencia, España",
                    destino = "Avinguda de Paiporta, 80, 46910 Benetússer, Valencia",
                    origenGeo = GeoPoint(39.3669795, -0.4610799),
                    destinoGeo = GeoPoint(39.4256181, -0.4028219),
                    momentoSalida = 1742288400,
                    //momentoLlegada = 1742289600,
                    momentoLlegada = 1742292431,
                    precio = 159,
                    distancia = 11.8,
                    asignado = false,
                    haciaCliente = false,
                    haciaDestino = false,
                    finalizado = true
                ),
                Ruta(
                    identificador = "fgj346bytj",
                    conductor = "1qw6g1r8ge",
                    cliente = "kv93j43fwe",
                    origen = "Avinguda de Paiporta, 80, 46910 Benetússer, Valencia",
                    destino = "Carrer del Mestre Ramírez, 2, 46220 Picassent, Valencia, España",
                    origenGeo = GeoPoint(39.4256181, -0.4028219),
                    destinoGeo = GeoPoint(39.3669795, -0.4610799),
                    momentoSalida = 1742317200,
                    momentoLlegada = 1742318400,
                    precio = 159,
                    distancia = 11.8,
                    asignado = false,
                    haciaCliente = false,
                    haciaDestino = false,
                    finalizado = true
                ),
            )
        }
    }

    fun getDataSorted(): List<Ruta> {
        val lista = getData()
        lista.sortBy {
            it.spanishName
        }
        return characterList1
    }
    */
}

    fun generateRandomID(length: Int = 10) : String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}

/*
 * Para generar el ID, genera un string con alfanuméricos
 *
//fun generateRandomID(length: Int) : String {
fun generateRandomID(length: Int = 10) : String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}
 */