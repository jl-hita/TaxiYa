package com.jlhipe.taxiya.ui.screens.crearRuta

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.jlhipe.taxiya.ui.screens.login.LoginViewModel
import com.jlhipe.taxiya.ui.screens.main.RutaViewModel
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.Marker
import com.jlhipe.taxiya.R
import com.jlhipe.taxiya.model.Ruta
import com.jlhipe.taxiya.navigation.Routes
import com.jlhipe.taxiya.ui.theme.screens.layout.AppScaffold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@RequiresPermission(
    anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION],
)
@Composable
fun NuevaRutaConPermisos(
    navController: NavController,
    loginViewModel: LoginViewModel,
    rutaViewModel: RutaViewModel,
    localizacionViewModel: LocalizacionViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Observa campos del ViewModel
    val destinoInput by localizacionViewModel.destino.observeAsState("")
    val ubicacionActualizada by localizacionViewModel.ubicacion.observeAsState()
    val destinoActualizado by localizacionViewModel.destinoLocation.observeAsState()

    // Estados locales
    val userLocation = remember { mutableStateOf(LatLng(0.0, 0.0)) }
    val destinoLocation = remember { mutableStateOf(LatLng(0.0, 0.0)) }

    // Cámara del mapa
    val cameraPositionState = rememberCameraPositionState()

    // Cliente de localización
    val locationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    // Obtener ubicación al inicio
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

        localizacionViewModel.setDestino(0.0, 0.0)
    }

    // Actualiza userLocation cuando el ViewModel tenga valor
    LaunchedEffect(ubicacionActualizada) {
        ubicacionActualizada?.firstOrNull()?.let {
            val nuevaUbicacion = LatLng(it.latitude, it.longitude)
            userLocation.value = nuevaUbicacion
            cameraPositionState.move(
                CameraUpdateFactory.newLatLngZoom(nuevaUbicacion, 15f)
            )
        }
    }

    //Obtenemos la api key
    val key: String = stringResource(R.string.apimakey)

    // Actualiza destinoLocation desde ViewModel
    LaunchedEffect(destinoActualizado) {
        destinoActualizado?.firstOrNull()?.let {
            destinoLocation.value = LatLng(it.latitude, it.longitude)
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(destinoLocation.value, 15f)
            )
        }
    }

    AppScaffold(
        showBackArrow = true,
        onBlackArrowClick = { navController.popBackStack() },
        showActionButton = true,
        botonAccion = { navController.navigate(Routes.NuevaRuta) }
    ) {
        if (localizacionViewModel.tienePermisosGPS()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                GoogleMap(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    cameraPositionState = cameraPositionState,
                    onMapClick = { clickedLocation ->
                        destinoLocation.value = clickedLocation
                        localizacionViewModel.setDestino(clickedLocation.latitude, clickedLocation.longitude)
                        scope.launch {
                            cameraPositionState.animate(CameraUpdateFactory.newLatLng(clickedLocation))
                        }
                    }
                ) {
                    Marker(position = userLocation.value, title = stringResource(R.string.tu))
                    Marker(position = destinoLocation.value, title = stringResource(R.string.destino))
                }

                TextField(
                    value = destinoInput,
                    onValueChange = { input ->
                        localizacionViewModel.setDestinoInput(input)
                        localizacionViewModel.requestCoordsFromAdress(input)
                    },
                    label = { Text(text = stringResource(R.string.escogeDestino)) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                //TODO deshabilitar el botón si no hay origen y destino definidos
                Button(
                    onClick = {
                        //Indicamos que se está cargando una ruta
                        //rutaViewModel.signalIsLoading();

                        //Creamos el objeto Ruta
                        var ruta: Ruta = Ruta();
                        ruta.cliente = loginViewModel.currentUserId
                        ruta.conductor = ""
                        ruta.origen = localizacionViewModel.requestGeocodeLocation(userLocation.value)
                        ruta.destino = destinoInput
                        ruta.origenGeo = userLocation.value
                        ruta.destinoGeo = userLocation.value
                        ruta.momentoSalida = null
                        ruta.momentoLlegada = null
                        ruta.asignado = false
                        ruta.haciaCliente = false
                        ruta.haciaDestino = false
                        ruta.finalizado = false
                        ruta.visible = true

                        //Lo insertamos en Firebase
                        rutaViewModel.insertaRutaFirebase(key, ruta)

                        //TODO Navegamos a DetallesRuta
                    },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(text = stringResource(R.string.buscaTaxiLibre))
                }
            }
        } else {
            Text(
                text = stringResource(R.string.necesitaPermisos),
                modifier = Modifier.padding(32.dp)
            )
        }
    }
}