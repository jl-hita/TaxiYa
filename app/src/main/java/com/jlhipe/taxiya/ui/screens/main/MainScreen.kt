package com.jlhipe.taxiya.ui.screens.main

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.navigation.NavController
import com.jlhipe.taxiya.R
import com.jlhipe.taxiya.navigation.Routes
import com.jlhipe.taxiya.ui.screens.login.LoginViewModel
import com.jlhipe.taxiya.ui.theme.screens.layout.AppScaffold

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


@Composable
fun MainScreen(
    navController: NavController,
    loginViewModel: LoginViewModel,
    rutaViewModel: RutaViewModel
) {
    AppScaffold(
        showBackArrow = false,
        showActionButton = true,
        botonAccion = { navController.navigate(Routes.NuevaRuta) }
    ) {
        val logeado by loginViewModel.logeado.observeAsState(initial = true)
        if (!logeado) {
            LaunchedEffect(Unit) {
                loginViewModel.navegar { navController.navigate(Routes.Login) }
            }
        }

        //val rutas by rutaViewModel.rutas.observeAsState(initial = emptyList())
        val rutas = rutaViewModel.rutas.value
        //val rutasFiltradas = remember(rutas) { rutas?.filter { it.visible && it.finalizado } }
        val isLoadingRutas by rutaViewModel.isLoading.observeAsState(initial = false)
        val rutaActiva by rutaViewModel.selectedRuta.observeAsState()
        val user = loginViewModel.currentUserId
        //val formatoFecha = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
        var yaNavegado by remember { mutableStateOf(false) }

        LaunchedEffect(user) {
            rutaViewModel.comprobarRutaActivaDelUsuario(user)
        }

        // Navegación cuando hay ruta activa
        LaunchedEffect(rutaActiva) {
            if (rutaActiva != null && !yaNavegado) {
                yaNavegado = true
                navController.navigate(Routes.DetallesRuta)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
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

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { loginViewModel.onLogOutClick() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(R.string.logOut))
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { loginViewModel.onDeleteAccountClick() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text(stringResource(R.string.borrarCuenta))
            }
        }
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