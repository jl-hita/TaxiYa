package com.jlhipe.taxiya.ui.screens.main

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.jlhipe.taxiya.R
import com.jlhipe.taxiya.navigation.Routes
import com.jlhipe.taxiya.ui.screens.login.LoginViewModel
import com.jlhipe.taxiya.ui.theme.screens.layout.AppScaffold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

//TODO Comprobar al principio si el usuario es conductor
//TODO Si es conductor -> Se filtran rutas por conductor = userId
//TODO Si es usuario -> Se filtran rutas por cliente = userId
//TODO Lo que hace ahora mismo es cargar todas las rutas en las que userId == conductor/usuario <- quizás sea suficiente -> Al mostrar los detalles
// se puede indicar si la participación en la ruta es como cliente o como conductor

//TODO Al llamar al botón crearRuta, si es conductor llevará a BuscarClientePre

@Composable
fun MainScreen(
    navController: NavController,
    loginViewModel: LoginViewModel,
    rutaViewModel: RutaViewModel
) {
    //val usuario = loginViewModel.user.value
    val user by loginViewModel.user.observeAsState()

    AppScaffold(
        showBackArrow = false,
        showActionButton = true,
        botonAccion = {
            if (user == null) {
                //Mientras se carga no hay acción en el botón
            } else {
                if(user!!.esConductor) {
                    //TODO Navegar a BuscarClientePre
                } else {
                    navController.navigate(Routes.NuevaRuta)
                }
            }
        },
        loginViewModel = loginViewModel,
        navController = navController,
    ) {
        val logeado by loginViewModel.logeado.observeAsState(initial = true)
        if (!logeado) {
            LaunchedEffect(Unit) {
                loginViewModel.navegar { navController.navigate(Routes.Login) }
            }
        }

        val rutas by rutaViewModel.rutas.observeAsState(initial = emptyList())
        //val rutas = rutaViewModel.rutas.value
        //val rutasFiltradas = remember(rutas) { rutas?.filter { it.visible && it.finalizado } }
        val isLoadingRutas by rutaViewModel.isLoading.observeAsState(initial = false)
        val rutaActiva by rutaViewModel.selectedRuta.observeAsState()
        //val user = loginViewModel.currentUserId
        //val user = loginViewModel.user.value!!.id

        //val formatoFecha = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
        var yaNavegado by remember { mutableStateOf(false) }

        // Si user es null, lanzamos la carga
        LaunchedEffect(user) {
            if (user == null) {
                loginViewModel.cargarUsuarioFirebase()
            }
        }

        // Mostramos contenido solo si user != null
        user?.let { u ->
            //val userId = u.id
            //TODO: resto de la UI que necesita userId
            LaunchedEffect(user) {
                rutaViewModel.comprobarRutaActivaDelUsuario(user!!.id)
            }
        } ?: run {
            //Mientras se carga el usuario, podemos mostrar un ProgressIndicator
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        /*
        LaunchedEffect(user) {
            rutaViewModel.comprobarRutaActivaDelUsuario(user!!.id)
        }
        */

        // Navegación cuando hay ruta activa
        LaunchedEffect(rutaActiva) {
            if (rutaActiva != null && !yaNavegado) {
                yaNavegado = true
                rutaViewModel.actualizarPuedeVolver(false)
                navController.navigate(Routes.DetallesRuta)
            }
        }

        // Solo cargamos rutas si hay usuario
        LaunchedEffect(logeado) {
            rutaViewModel.loadRutas()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                //.fillMaxHeight(0.7F)
                //.fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.listaDeRutas),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(16.dp))

            // Lista de rutas filtradas
            val rutasFiltradas = remember(rutas) {
                rutas?.filter { it.visible && it.finalizado }
            }

            Log.d("MainScreen", "Rutas -> $rutas")
            Log.d("MainScreen", "Rutas filtradas -> $rutasFiltradas")

            if (rutasFiltradas != null) {
                ListaDeRutas(
                    rutas = rutasFiltradas,
                    /*
                    onRutaClick = {
                        rutaViewModel.setRuta(rutaActiva!!)
                        navController.navigate(Routes.DetallesRuta)
                    },
                     */
                    navController = navController,
                    rutaViewModel
                )
            }

            // Indicador de carga
            if (isLoadingRutas) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x88000000)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        /*
        //TODO BORRAME
        Button(
            //Boton de borrar cuenta
            onClick = { loginViewModel.onDeleteAccountClick() },
            modifier = Modifier
                .fillMaxWidth(0.5F)
                .padding(16.dp, 0.dp)
        ) {
            Text(
                text = stringResource(R.string.borrarCuenta),
                fontSize = 16.sp,
                modifier = Modifier.padding(0.dp, 6.dp)
            )
        }

        //TODO BORRAME
        Button(
            //Boton de borrar cuenta
            onClick = {
                //loginViewModel.signOut()
                //navController.navigate(Routes.Login)
                // Cancelamos cualquier carga pendiente o listener aquí si es necesario
                loginViewModel.launchCatching {
                    loginViewModel.signOut()
                    withContext(Dispatchers.Main) {
                        navController.navigate(Routes.Login) {
                            popUpTo(Routes.Main) { inclusive = true }
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.5F)
                .padding(16.dp, 0.dp)
        ) {
            Text(
                text = stringResource(R.string.cerrarSesion),
                fontSize = 16.sp,
                modifier = Modifier.padding(0.dp, 6.dp)
            )
        }
         */
    }
}

// Ejemplo de formateo duración optimizado
fun formatDuration(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return if (hours > 0) "%d:%02d h".format(hours, minutes)
    else "%d min".format(minutes)
}

/*
@Composable
fun MainScreen(
    navController: NavController,
    loginViewModel: LoginViewModel,
    rutaViewModel: RutaViewModel
) {
    AppScaffold (
        showBackArrow = false,
        onBlackArrowClick = { },
        //botonAccion = navController.navigate(Routes.NuevaRuta)
        showActionButton = true, //TODO cambiar por un check si usuario es conductor
        botonAccion = {
            navController.navigate(Routes.NuevaRuta)
        },
        //bottomContent = { BottomBar(modifier = Modifier.padding(vertical = 4.dp), bookViewModel = bookViewModel) }
    ) {
        //Si hace logout se envía a página Login
        val logeado: Boolean by loginViewModel.logeado.observeAsState(initial = true)
        if(!logeado) {
            LaunchedEffect(key1 = true) { loginViewModel.navegar({ navController.navigate(Routes.Login) }) }
        }

        //Text("currentUserId: " + loginViewModel.currentUserId)
        //Text("currentUserName: " + loginViewModel.currentUserName)
        //Text("currentUserEmail: " + loginViewModel.currentUserEmail)
        //Log.d("ID", loginViewModel.currentUserId)

        //TODO if(user.isConductor = false)

        // Suscripción a la lista de rutas del ViewModel
        val rutas: List<Ruta> by rutaViewModel.rutas.observeAsState(initial = emptyList())
        // Suscripción a la variable que indica si se están cargando las rutas
        val isLoadingRutas: Boolean by rutaViewModel.isLoading.observeAsState(initial = false)

        val context = LocalContext.current

        //Comprobamos si hay alguna ruta activa
        val user = loginViewModel.currentUserId
        val rutaActiva by rutaViewModel.selectedRuta.observeAsState()
        LaunchedEffect(user) {
            Log.d("Main", "Buscando ruta activa para usuario ${user}")
            //Comprobamos si el usuario tiene una ruta activa, sin finalizar
            rutaViewModel.comprobarRutaActivaDelUsuario(user)
        }

        //Si hay ruta activa navegamos a DetallesRuta
        LaunchedEffect(rutaActiva) {
            Log.d("Main", "Ruta activa -> $rutaActiva")
            if(rutaActiva != null) {
                navController.navigate(Routes.DetallesRuta)
                //DetallesRuta(navController: NavController,
                    //loginViewModel: LoginViewModel,
                    //rutaViewModel: RutaViewModel,
                    //localizacionViewModel: LocalizacionViewModel)
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                //.background(MaterialTheme.colorScheme.primary)
                //.padding(vertical = 8.dp)
                .padding(8.dp)
                //.weight(7.7f)
        ) {
            item() {
                Row(){ //Header
                    Text(stringResource(R.string.origen), fontSize = 25.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(10f))
                    Text(stringResource(R.string.destino), fontSize = 25.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(10f))
                    Text(stringResource(R.string.fecha), fontSize = 25.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(10f))
                    Text(stringResource(R.string.duracion), fontSize = 25.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(10f))
                    //Text(stringResource(R.string.distancia), fontSize = 25.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(10f))
                }
            }
            items(rutas) { ruta ->
            //items(rutasFirebase) { ruta ->

                if(ruta.visible) {
                    HorizontalDivider(thickness = 2.dp)
                    Row() {
                        //Text(rutaViewModel.getNombreCiudad(ruta.origenGeo, context)!!, modifier = Modifier.weight(10f))
                        Text(ruta.origen, modifier = Modifier.weight(10f))
                        //Text(rutaViewModel.getNombreCiudad(ruta.destinoGeo, context)!!, modifier = Modifier.weight(10f))
                        Text(ruta.destino, modifier = Modifier.weight(10f))
                        //Text(rutaViewModel.getFechaCompleta(ruta.momentoSalida), modifier = Modifier.weight(10f))
                        if(ruta.momentoSalida != null) {
                            Text(rutaViewModel.getDia(ruta.momentoSalida!!), modifier = Modifier.weight(10f))
                        }
                        Text(stringResource(R.string.duracion) + ": " + ruta.duracion, modifier = Modifier.weight(10f))
                        //Text("" + ruta.distancia + " km", modifier = Modifier.weight(10f))
                    }
                }
            }
        }

        //Mientras carga la lista de rutas se muestra un mensaje y una linea de progreso
        if (isLoadingRutas) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = stringResource(R.string.cargando),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.height(20.dp))
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        //TODO Pasar botón de logOut a un menú en el AppTopBar
        Button(
            //Boton de logout
            onClick = { loginViewModel.onLogOutClick() },
            modifier = Modifier
                .fillMaxWidth(0.5F)
                .padding(16.dp, 0.dp)
        ) {
            Text(
                text = stringResource(R.string.logOut),
                fontSize = 16.sp,
                modifier = Modifier.padding(0.dp, 6.dp)
            )
        }

        Spacer(modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp))

        //TODO Pasar botón de logOut a un menú en el AppTopBar
        Button(
            //Boton de borrar cuenta
            onClick = { loginViewModel.onDeleteAccountClick() },
            modifier = Modifier
                .fillMaxWidth(0.5F)
                .padding(16.dp, 0.dp)
        ) {
            Text(
                text = stringResource(R.string.borrarCuenta),
                fontSize = 16.sp,
                modifier = Modifier.padding(0.dp, 6.dp)
            )
        }

        //TODO BORRAME, solo para pruebas -> Borra todas las rutas de firebase
        Button(
            onClick = { rutaViewModel.borrarTodasLasRutas() },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text("Borrar TODAS las rutas", color = Color.White)
        }
    }
}
*/

/*
@Composable
fun BotonesDeSesion(
    logeado: Boolean,
    onLoginClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (logeado) {
            Button(onClick = onLogoutClick) {
                Text("Cerrar sesión")
            }
        } else {
            Button(onClick = onLoginClick) {
                Text("Iniciar sesión")
            }
        }
    }
}
*/