package com.jlhipe.taxiya.model

data class User (
    val identificador: String = "",     //Identificador único: se genera y se comprueba que no exista
    val esConductor: Boolean = false,   //¿Es conductor? Si false es usuario
    val nombre: String = "",
    val apellidos: String = "",
){

}
