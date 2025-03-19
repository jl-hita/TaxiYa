package com.jlhipe.taxiya.model

data class Ruta (
    val identificador: String = "", //Identificador único: se genera y se comprueba que no exista
    val origen: String = "",    //Dirección con el formato de google maps API
    val destino: String = "",   //Dirección con el formato de google maps API
    val conductor: String = "", //ID única del conductor, no mostrar en UI
    val cliente: String = "",   //ID única del cliente, no mostrar en UI
    val precio: Int = 0,   //Precio en céntimos de euro
    val distancia: Float = 0.0F,    //Distancia en km con un decimal
){
    /*
     * Para tener datos de prueba para la UI
     */
    companion object {
        fun getData() : List<Ruta> {
            return listOf(
                Ruta(generateRandomID(), "Carrer del Mestre Ramírez, 2, 46220 Picassent, Valencia, España", "Avinguda de Paiporta, 80, 46910 Benetússer, Valencia", "IDdeCoonductorInventada", "IDdeClienteInventada", 159, 11.8F),
                Ruta(generateRandomID(), "Avinguda de Paiporta, 80, 46910 Benetússer, Valencia", "Carrer del Mestre Ramírez, 2, 46220 Picassent, Valencia, España", "IDdeCoonductorInventada", "IDdeClienteInventada", 159, 11.8F),
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