package com.jlhipe.taxiya.navigation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jlhipe.taxiya.ui.screens.splash.SplashScreen
import com.jlhipe.taxiya.ui.screens.login.LoginScreen
import com.jlhipe.taxiya.ui.screens.login.LoginViewModel

@Composable
//fun Navigation(rutaViewModel: RutaViewModel) {
fun Navigation(loginViewModel: LoginViewModel) {
    val navController = rememberNavController()
    //val rutasController = remember { RutasController()}

    NavHost(
        navController = navController,
        startDestination = Routes.Splash, // Ruta por la que comenzará la aplicación,
        enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) },
    ) {
        /*
         * Pantallas:
         * - Pantalla de login/creación nueva cuenta                                                                                                                    *** Pantalla de login/creación nueva cuenta ***
         * - ¿Nueva cuenta?
         *  -> SI: Escoge entre conductor y cliente -> Todas las pantallas de crear cuenta se pueden resumir en una sola con booleans que oculten campos                *** Pantalla para crear cuenta nueva ***
         *      -> Conductor: Introduce datos y se muestra email al que enviar documentación. OPCIONAL: Acceso a cámara/archivos para que envie su documentación        *** Misma pantalla: introducir datos y enviar documentación ***
         *      -> Cliente: Pantalla en la que introducir sus datos                                                                                                     *** Misma pantalla: introducir datos cliente ***
         *  -> NO: Le lleva a la pantalla que le corresponda (conductor/cliente)
         *       -> Conductor: Pantalla con botón para marcarse como activo / Inactivo. Si le llega petición se dejará de ocultar el botón para aceptarla               *** Pantalla modo espera ***
         *          -> Le llega petición y la acepta: COMPRUEBA QUE LA PETICIÓN SIGUE ACTIVA y va a pantalla con GPS que le lleve al cliente                            *** Pantalla GPS: guía a cliente ***
         *          -> Recoge al cliente (lo marca con un botón): El GPS que le guía al destino                                                                         *** Misma pantalla GPS: guía a destino ***
         *          -> Llega al destino (lo marca con un botón o el GPS detecta que está a X distancia)                                                                 *** Pantalla resumen viaje con datos ***
         *              -> Le lleva en X segundos a la pantalla de espera de petición de cliente
         *                  -> Al marcarse como inactivo le puede hacer un resumen de los datos de la sesión o día                                                      *** Resumen datos día/sesión de conductor ***
         *       -> Cliente: Pantalla con las rutas realizadas en el pasado y un floatingActionButton para pedir viaje                                                  *** Pantalla con lista de rutas realizadas ***
         *          -> Ha hecho click en pedir viaje: Pantalla para introducir datos y pedir nueva ruta                                                                 *** Página de ruta nueva ***
         *          -> Ha enviado petición: La app comprueba cada X segundos si alguien la ha aceptado (muestra ETA de conductor a cliente,
         *              estado aceptado/no, botón cancelar, quizás mapa con ubicación del conductor)                                                                    *** Pantalla de espera a conductor ***
         *          -> El conductor llega al cliente: Muestra datos (ETA, mapa...)                                                                                      *** Pantalla de ruta a destino ***
         *          -> Llega a destino: Muestra resumen con tiempos, distancias...                                                                                      *** Pantalla resumen viaje con datos ***
         * - Pantalla de opciones en las que poder hacer logout, cambiar datos, etc                                                                                     *** Pantalla de opciones ***
         *
         * WEB:
         *  - URLs de la política de privacidad y las condiciones del servicio de tu app.
         *  - Asegúrate de que tu app tenga asignados un nombre, un logotipo y una página principal correctos. Estos valores se presentarán a los usuarios en la pantalla
         *      de consentimiento de Acceder con Google durante el registro y en la pantalla Apps y servicios de terceros.
         */

        //SplashScreen
        composable<Routes.Splash> {
            //SplashScreen(navController, rutaViewModel)
            SplashScreen(
                //modifier = Modifier.background(color = MaterialTheme.colorScheme.background),
                navController,
                loginViewModel
            )
        }

        composable<Routes.Login> {
            LoginScreen(navController, loginViewModel)
        }

        composable<Routes.Main> {
            //MainScreen(navController, loginViewModel)
        }
        /*
        composable<Routes.BookInfo> {
            BookInfoScreen(
                onBackArrowClick = {
                    navController.popBackStack()
                },
                rutaViewModel = RutaViewModel
            )
        }
        */
    }
}