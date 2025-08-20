package com.jlhipe.taxiya.ui.screens.detallesruta

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.jlhipe.taxiya.R
import com.jlhipe.taxiya.model.Ruta
import com.jlhipe.taxiya.navigation.Routes
import com.jlhipe.taxiya.ui.screens.crearRuta.LocalizacionViewModel
import com.jlhipe.taxiya.ui.screens.login.LoginViewModel
import com.jlhipe.taxiya.ui.screens.main.RutaViewModel
import com.jlhipe.taxiya.ui.theme.screens.layout.NonAppScaffold
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * Vista que simula la función de navegación de google maps usando la polyline obtenida con la API Directions
 */

@Composable
fun MapaRuta(
    navController: NavController,
    loginViewModel: LoginViewModel,
    rutaViewModel: RutaViewModel,
    localizacionViewModel: LocalizacionViewModel,
) {
    val ruta by rutaViewModel.selectedRuta.observeAsState()
    val esConductor = loginViewModel.esConductor.collectAsState()
    var estaCentrado by remember { mutableStateOf(false) }

    //Si por algún motivo no hay ruta navegamos a la vista principal
    if(ruta == null) navController.navigate(Routes.Main)

    //Si no hay polyline no tiene sentido el mapa, volvemos
    if(ruta!!.haciaCliente && ruta!!.polylineCliente.isEmpty()) return
    if(ruta!!.haciaDestino && ruta!!.polylineDestino.isEmpty()) return

    val scope = rememberCoroutineScope()

    //TODO -> nuevo estado ruta.haciaOrigenConductor o algo así, que mostrará la ruta al punto original del conductor pero solo a él
    //Si ruta.finalizado -> la posicionCamara mostrará la ruta completa de origen a fin
    //Si !ruta.finalizado && haciaCliente o haciaDestino -> se centrará en el usuario
    val ubicacion by localizacionViewModel.ubicacion.observeAsState()
    val cameraPositionState = rememberCameraPositionState()

    val bounds: LatLngBounds = if (esConductor.value) {
        LatLngBounds.builder()
            .include(LatLng(ruta!!.origenGeo.latitude, ruta!!.origenGeo.longitude))
            .include(LatLng(ruta!!.destinoGeo.latitude, ruta!!.destinoGeo.longitude))
            .build()
    } else {
        LatLngBounds.builder()
            .include(LatLng(ruta!!.origenGeo.latitude, ruta!!.origenGeo.longitude))
            .include(LatLng(ruta!!.destinoGeo.latitude, ruta!!.destinoGeo.longitude))
            .include(LatLng(ruta!!.posicionConductor.latitude, ruta!!.posicionConductor.longitude))
            .build()
    }

    LaunchedEffect(ruta!!.finalizado, ruta!!.haciaCliente, ruta!!.haciaDestino, ubicacion) {
        if (esConductor.value && !ruta!!.finalizado && (ruta!!.haciaCliente || ruta!!.haciaDestino)) { //TODO aquí también controlar ruta.haciaOrigenConductor
            //Si la ruta está activa -> centrar en la ubicación actual
            ubicacion?.let {
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(it, 15f)
                )
            }
            estaCentrado = true
        } else {
            //Si la ruta ha finalizado -> encuadrar origen y destino
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngBounds(bounds, 100)
            )
            estaCentrado = false
        }
    }

    NonAppScaffold(
        navController = navController,
        showBack = true,
        onBackClick = { navController.popBackStack() },
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.matchParentSize(),
                cameraPositionState = cameraPositionState
            ) {
                //Polylines
                //val puntos = if (ruta!!.haciaCliente) {
                val puntos = if (!ruta!!.finalizado && !ruta!!.haciaDestino) {
                    ruta!!.polylineCliente.map { LatLng(it.latitude, it.longitude) }
                } else {
                    ruta!!.polylineDestino.map { LatLng(it.latitude, it.longitude) }
                }
                Polyline(
                    points = puntos,
                    color = if (ruta!!.haciaCliente) Color.Blue else Color.Red,
                    width = 10f
                )

                //Marcadores
                //Marcador de conductor solo para el conductor
                if(esConductor.value) {
                    Marker(
                        position = LatLng(ruta!!.posicionConductor.latitude, ruta!!.posicionConductor.longitude),
                        title = stringResource(R.string.conductor),
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                    )
                }//Marcador de origen de ruta para todos
                Marker(
                    position = LatLng(ruta!!.origenGeo.latitude, ruta!!.origenGeo.longitude),
                    title = stringResource(R.string.inicio),
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                )
                //Marcador de destino si el trayecto es hacia el destino o si la ruta ha finalizado
                if (ruta!!.haciaDestino || ruta!!.finalizado) {
                    Marker(
                        position = LatLng(ruta!!.destinoGeo.latitude, ruta!!.destinoGeo.longitude),
                        title = stringResource(R.string.destino),
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                    )
                }
                /* TODO descomentar si implemento "volver a origen de conductor"
                if (ruta!!.haciaCliente) {
                    Marker(
                        position = LatLng(ruta!!.posicionInicialConductor.latitude, ruta!!.posicionInicialConductor.longitude),
                        title = stringResource(R.string.origenConductor),
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                    )
                }
                */
            }

            //Si es conductor podrá centrar la cámara
            if (esConductor.value) {
                FloatingActionButton(
                    onClick = {
                        scope.launch {
                            if(estaCentrado) {
                                cameraPositionState.animate(
                                    update = CameraUpdateFactory.newLatLngBounds(bounds, 100)
                                )
                                estaCentrado = false
                            } else {
                                ubicacion?.let {
                                    cameraPositionState.animate(
                                        update = CameraUpdateFactory.newLatLngZoom(it, 15f)
                                    )
                                }
                                estaCentrado = true
                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = stringResource(R.string.centrarEnMiPosicion) //Centrar en mi posición
                    )
                }
            }
        }
    }
}