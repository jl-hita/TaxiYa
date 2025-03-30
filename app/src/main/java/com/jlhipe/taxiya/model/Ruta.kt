package com.jlhipe.taxiya.model

import java.util.Date

data class Ruta (
    val identificador: String = "", //Identificador único: se genera y se comprueba que no exista
    val conductor: String = "", //ID única del conductor, no mostrar en UI
    val cliente: String = "",   //ID única del cliente, no mostrar en UI
    val origen: String = "",    //Dirección con el formato texto normal
    val destino: String = "",   //Dirección con el formato texto normal
    //val origenCoord: String = "",    //Dirección con el formato coordenadas
    //val destinoCoord: String = "",   //Dirección con el formato coordenadas
    //val dia: Date,     //Día que se inició la ruta
    val dia: String = "",     //Día que se inició la ruta con formato aaaa/mm/dd
    val precio: Int = 0,    //Precio en céntimos de euro
    val distancia: Float = 0.0F,    //Distancia en km con un decimal
    val asignado:  Boolean = false,    //¿Está la ruta asignada a un taxista?
    val haciaCliente: Boolean = false,    //¿Está el taxi yendo a recoger al usuario?
    val haciaDestino: Boolean = false,    //¿Está el usuario en el taxi en ruta?
    val finalizado: Boolean = false,    //¿Ha finalizado la ruta?
){
    /*
     * Para tener datos de prueba para la UI
     */
    companion object {
        fun getData() : List<Ruta> {
            return listOf(
                Ruta(generateRandomID(), "IDdeCoonductorInventada", "IDdeClienteInventada", "Carrer del Mestre Ramírez, 2, 46220 Picassent, Valencia, España", "Avinguda de Paiporta, 80, 46910 Benetússer, Valencia", "2025/03/18", 159, 11.8F, false, false, false, true),
                Ruta(generateRandomID(), "IDdeCoonductorInventada", "IDdeClienteInventada", "Avinguda de Paiporta, 80, 46910 Benetússer, Valencia", "Carrer del Mestre Ramírez, 2, 46220 Picassent, Valencia, España", "2025/03/18", 159, 11.8F, false, false, false, true),
            )
        }
    }
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