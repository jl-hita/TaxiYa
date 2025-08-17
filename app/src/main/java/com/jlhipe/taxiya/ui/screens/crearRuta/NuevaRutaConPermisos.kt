package com.jlhipe.taxiya.ui.screens.crearRuta

import android.Manifest
import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.jlhipe.taxiya.ui.screens.login.LoginViewModel
import com.jlhipe.taxiya.ui.screens.main.RutaViewModel
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.firebase.firestore.GeoPoint
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.Marker
import com.jlhipe.taxiya.R
import com.jlhipe.taxiya.model.Ruta
import com.jlhipe.taxiya.navigation.Routes
import com.jlhipe.taxiya.ui.theme.BlueRibbon
import com.jlhipe.taxiya.ui.theme.screens.layout.AppScaffold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(FlowPreview::class)
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

    //var ruta: Ruta = Ruta()
    val ruta by remember { mutableStateOf(Ruta()) }

    // Observa campos del ViewModel
    //TODO pasar variables de localizacionViewModel a rutaViewModel
    //val destinoInput by localizacionViewModel.destino.observeAsState("")
    val ubicacionActualizada by localizacionViewModel.ubicacion.observeAsState()
    val destinoActualizado by localizacionViewModel.destinoLocation.observeAsState()
    val destinationText = remember { mutableStateOf("") }
    //var destinationLatLng by remember { mutableStateOf<LatLng?>(null) }
    val destinoLatLng = remember { mutableStateOf(LatLng(0.0, 0.0)) }

    //Estados locales
    val userLocation = remember { mutableStateOf(LatLng(0.0, 0.0)) }
    val destinoLocation = remember { mutableStateOf(LatLng(0.0, 0.0)) }

    //Cámara del mapa
    val cameraPositionState = rememberCameraPositionState()

    val errorAlCrearRuta = stringResource(R.string.errorAlCrearRuta)

    //Cliente de localización
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

    //Para controlar la ubicación en tiempo real
    LaunchedEffect(Unit) {
        localizacionViewModel.startLocationUpdates()
    }

    DisposableEffect(Unit) {
        onDispose {
            localizacionViewModel.stopLocationUpdates()
        }
    }

    //Obtenemos la api key
    val mapssdkkey: String = stringResource(R.string.mapssdkkey)
    //Clave para Routes API
    val routesApiKey: String = stringResource(R.string.routesAPI)

    // Actualiza destinoLocation desde ViewModel
    LaunchedEffect(destinoActualizado) {
        destinoActualizado?.firstOrNull()?.let {
            destinoLocation.value = LatLng(it.latitude, it.longitude)
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(destinoLocation.value, 15f)
            )
        }
    }

    // Debounce seguro del TextField
    LaunchedEffect(destinationText.value) {
        snapshotFlow { destinationText.value }
            .debounce(500)
            .collectLatest { text ->
                if (text.isNotBlank()) {
                    val coords = rutaViewModel.obtenerCoordenadas(text, context, mapssdkkey)
                    coords?.let {
                        destinoLatLng.value = LatLng(it.latitude, it.longitude)
                        ruta.destinoGeo = GeoPoint(it.latitude, it.longitude)
                        ruta.destino = text
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(destinoLatLng.value, 15f)
                        )
                    }
                }
            }
    }

    AppScaffold(
        showBackArrow = true,
        onBlackArrowClick = { navController.popBackStack() },
        showActionButton = false,
        botonAccion = { navController.navigate(Routes.NuevaRuta) },
        loginViewModel = loginViewModel,
        navController = navController,
    ) {
        if (localizacionViewModel.tienePermisosGPS()) {
            Box(modifier = Modifier.fillMaxSize()) {
                // 🌍 Mapa ocupa todo el espacio
                GoogleMap(
                    modifier = Modifier.matchParentSize(),
                    cameraPositionState = cameraPositionState,
                    onMapClick = { clickedLocation ->
                        destinoLocation.value = clickedLocation
                        localizacionViewModel.setDestino(clickedLocation.latitude, clickedLocation.longitude)
                        scope.launch {
                            ruta.destinoGeo = GeoPoint(clickedLocation.latitude, clickedLocation.longitude)
                            //TODO Revisar este cambio
                            //ruta.destino = rutaViewModel.obtenerDireccion(ruta.destinoGeo, context)
                            ruta.destino = rutaViewModel.obtenerDireccion(GeoPoint(clickedLocation.latitude, clickedLocation.longitude), context, mapssdkkey)//suspend fun obtenerDireccion(latLng: GeoPoint, context: Context, googleApiKey: String): String {
                            destinationText.value = ruta.destino
                            cameraPositionState.animate(CameraUpdateFactory.newLatLng(clickedLocation))
                        }
                    }
                ) {
                    Marker(position = userLocation.value, title = stringResource(R.string.tu))
                    Marker(position = destinoLocation.value, title = stringResource(R.string.destino))
                }

                //TextField para escribir dirección
                OutlinedTextField(
                    value = destinationText.value,
                    onValueChange = { destinationText.value = it },
                    label = { Text(stringResource(R.string.destino)) },
                    modifier = Modifier
                        .align(Alignment.TopCenter) // Arriba centrado
                        .padding(16.dp)
                        .background(Color.White, shape = RoundedCornerShape(8.dp))
                        .fillMaxWidth(0.9f)
                )

                //Botón para crear ruta
                Button(
                    onClick = {
                        ruta.cliente = loginViewModel.user.value!!.id
                        ruta.conductor = ""
                        ruta.origenGeo = GeoPoint(userLocation.value.latitude, userLocation.value.longitude)
                        scope.launch {
                            //TODO Revisar este cambio
                            //ruta.origen = rutaViewModel.obtenerDireccion(ruta.origenGeo, context)
                            ruta.origen = rutaViewModel.obtenerDireccion(ruta.origenGeo, context, mapssdkkey)
                            val idInsertada = rutaViewModel.insertaRutaFirebase(ruta, routesApiKey)
                            if (idInsertada != null) {
                                rutaViewModel.actualizarPuedeVolver(false)
                                navController.navigate(Routes.DetallesRuta)
                            } else {
                                destinationText.value = errorAlCrearRuta
                                delay(2000)
                                navController.navigate(Routes.Main)
                            }
                        }
                    },
                    enabled = userLocation.value != LatLng(0.0, 0.0) &&
                            (ruta.destinoGeo != GeoPoint(0.0, 0.0) || ruta.destino.isNotBlank()),
                    modifier = Modifier
                        .align(Alignment.BottomCenter) // Abajo centrado
                        .padding(16.dp)
                        //.background(Color.Blue/*, shape = RoundedCornerShape(8.dp) */),
                        .background(BlueRibbon)
                    /*
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Blue,      // 👈 Fondo del botón
                        contentColor = Color.White         // 👈 Texto/Iconos en blanco
                    )
                     */
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

    /*
    AppScaffold(
        showBackArrow = true,
        onBlackArrowClick = { navController.popBackStack() },
        showActionButton = false,
        botonAccion = { navController.navigate(Routes.NuevaRuta) },
        loginViewModel = loginViewModel,
        navController = navController,
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
                            //ruta.destinoGeo = clickedLocation
                            ruta.destinoGeo = GeoPoint(clickedLocation.latitude, clickedLocation.longitude)
                            //ruta.destino = rutaViewModel.getAddressFromLatLng(clickedLocation, mapssdkkey)
                            ruta.destino = rutaViewModel.obtenerDireccion(ruta.destinoGeo, context)
                            Log.d("NuevaRuta", "Coordenadas obtenidas: lat=${clickedLocation.latitude}, lng=${clickedLocation.longitude}")
                            Log.d("NuevaRuta", "Dirección: ${ruta.destino}")
                            cameraPositionState.animate(CameraUpdateFactory.newLatLng(clickedLocation))
                        }
                    }
                ) {
                    Marker(position = userLocation.value, title = stringResource(R.string.tu))
                    Marker(position = destinoLocation.value, title = stringResource(R.string.destino))
                }

                //var searchJob: Job? = null

                OutlinedTextField(
                    value = destinationText.value,
                    onValueChange = { destinationText.value = it },
                    label = { Text(stringResource(R.string.destino)) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Button(
                    onClick = {
                        //ruta.cliente = loginViewModel.currentUserId
                        ruta.cliente = loginViewModel.user.value!!.id
                        ruta.conductor = ""
                        ruta.origenGeo = GeoPoint(userLocation.value.latitude, userLocation.value.longitude)
                        scope.launch {
                            ruta.origen = rutaViewModel.obtenerDireccion(ruta.origenGeo, context)
                            //val idInsertada = rutaViewModel.insertaRutaFirebase(mapssdkkey, ruta, routesApiKey)
                            val idInsertada = rutaViewModel.insertaRutaFirebase(ruta, routesApiKey)
                            if (idInsertada != null) {
                                rutaViewModel.actualizarPuedeVolver(false)
                                navController.navigate(Routes.DetallesRuta)
                            } else {
                                destinationText.value = errorAlCrearRuta
                                delay(2000)
                                navController.navigate(Routes.Main)
                            }
                        }
                    },
                    enabled = userLocation.value != LatLng(0.0, 0.0) &&
                            (ruta.destinoGeo != GeoPoint(0.0, 0.0) || ruta.destino.isNotBlank()),
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
    */
}