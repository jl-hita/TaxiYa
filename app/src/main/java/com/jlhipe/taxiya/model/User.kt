package com.jlhipe.taxiya.model

data class User (
    val id: String  = "",               //TODO Identificador único: se genera al crear cuenta y se guarda en Firebase.auth.uid y se recupera con LoginViewModel.currentUserId
    val email: String = "",             //Email para login con email
    //val password: String ="",           //Contraseña para login con email
    //val identificador: String = "",   //Identificador único: se genera y se comprueba que no exista
    //val primeraVez: Boolean = true,   //¿Es la primera vez que inicia la app?
    val esConductor: Boolean = false,   //¿Es conductor? Si false es usuario
    val validado: Boolean = false,      //Un conductor debe estar validad para poder ejercer de taxista
    val nombre: String = "",
    val apellidos: String = "",
    val activo: Boolean = false,        //¿Está en activo?
    val enRuta: Boolean = false,        //¿Va en ruta en este momento?
    val rutaActual: String = "",        //ID de la ruta actual si está en ruta (en cualquiera de sus estados)
    //A partir de aquí necesarios para FirebaseUser
    val provider: String = "",
    val isAnonymous: Boolean = false
){
    /*
    companion object {
        fun generateRandomID(length: Int = 10): String {
            val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
            return (1..length)
                .map { allowedChars.random() }
                .joinToString("")
        }
    }
     */
}
