package com.jlhipe.taxiya.ui.screens.buscarCliente

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.jlhipe.taxiya.R
import com.jlhipe.taxiya.navigation.Routes
import com.jlhipe.taxiya.ui.screens.crearRuta.LocalizacionViewModel
import com.jlhipe.taxiya.ui.screens.login.LoginViewModel
import com.jlhipe.taxiya.ui.screens.main.ListaDeRutas
import com.jlhipe.taxiya.ui.screens.main.RutaViewModel
import com.jlhipe.taxiya.ui.theme.screens.layout.NonAppScaffold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(FlowPreview::class)
@RequiresPermission(
    anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION],
)

@Composable
fun BuscarClienteConPermisos(
    navController: NavController,
    loginViewModel: LoginViewModel,
    rutaViewModel: RutaViewModel,
    localizacionViewModel: LocalizacionViewModel
) {
    val user = loginViewModel.user
    val userLocation by localizacionViewModel.ubicacion.observeAsState()
    //val userLocation = remember { mutableStateOf(LatLng(0.0, 0.0)) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    //val rutasBuscandoTaxi by rutaViewModel.rutasBuscandoTaxi.observeAsState(emptyList())
    val rutasBuscandoTaxi by rutaViewModel.rutasBuscandoTaxi.observeAsState()
    val primeraCarga by rutaViewModel.primeraCargaTaxis.observeAsState()
    val coroutineScope = rememberCoroutineScope()

    //Cliente de localización
    val locationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    //Primera carga
    LaunchedEffect(true) {
        rutaViewModel.setPrimeraCarga(false)
    }

    //Obtenemos la ubicación del usuario
    LaunchedEffect(true) {
        scope.launch(Dispatchers.IO) {
            try {
                val result = locationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    CancellationTokenSource().token
                ).await()

                result?.let {
                    localizacionViewModel.setUbicacion(it.latitude, it.longitude)
                }
            } catch (e: Exception) {
                //Log.e("NuevaRuta", "Error obteniendo ubicación: ${e.message}")
            }
        }
    }

    /*
    LaunchedEffect(user) {
        user.value?.let { userLocation?.let { it1 -> rutaViewModel.cargarRutasBuscandoTaxi(it.id, it1.first()) } }
    }
     */

    LaunchedEffect(user, userLocation) {
        val usuario = user.value ?: return@LaunchedEffect
        val ubicacion = userLocation ?: return@LaunchedEffect

        do {
            //Carga las rutas
            rutaViewModel.cargarRutasBuscandoTaxi(usuario.id, ubicacion.first())

            //Espera 5 segundos antes de la siguiente recarga
            delay(5000)
        } while (true)
    }

    NonAppScaffold(
        navController = navController,
        showBack = true,
        onBackClick = {
            coroutineScope.launch {
                navController.navigate(Routes.Main)
            }
        }
    ) {
        if (rutasBuscandoTaxi != null && rutasBuscandoTaxi!!.isNotEmpty()) {
            ListaDeRutas(
                rutas = rutasBuscandoTaxi!!,
                navController = navController,
                rutaViewModel,
                localizacionViewModel,
                buscaTaxi = true,
            )
        } else if (primeraCarga == false) {
            Column(
                modifier = Modifier.fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.oops),
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(Modifier.height(12.dp))

                Image(
                    painter = painterResource(id = R.drawable.perrete),
                    contentDescription = "No hay rutas",
                    modifier = Modifier
                        .size(200.dp)
                        .padding(bottom = 16.dp)
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.noHayRutasEnEsteMomento),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}