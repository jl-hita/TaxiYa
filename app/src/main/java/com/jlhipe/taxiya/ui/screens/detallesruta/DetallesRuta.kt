package com.jlhipe.taxiya.ui.screens.detallesruta

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.jlhipe.taxiya.R
import com.jlhipe.taxiya.model.Ruta
import com.jlhipe.taxiya.navigation.Routes
import com.jlhipe.taxiya.ui.screens.crearRuta.LocalizacionViewModel
import com.jlhipe.taxiya.ui.screens.login.LoginViewModel
import com.jlhipe.taxiya.ui.screens.main.RutaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/*
@Composable
fun DetallesRuta(
    navController: NavController,
    loginViewModel: LoginViewModel,
    rutaViewModel: RutaViewModel,
    localizacionViewModel: LocalizacionViewModel
) {
    val formatoFecha = remember {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    }

    val rutaActiva by rutaViewModel.selectedRuta.observeAsState()

    // Inicia y detiene el refresco automático
    LaunchedEffect(rutaActiva?.id) {
        rutaActiva?.id?.takeIf { it.isNotBlank() }?.let { rutaId ->
            rutaViewModel.startAutoRefresh(rutaId)
        }
    }

    DisposableEffect(Unit) {
        onDispose { rutaViewModel.stopAutoRefresh() }
    }

    rutaActiva?.let { ruta ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(R.string.detallesDeRuta),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    RutaInfoItem(stringResource(R.string.origen), ruta.origen)
                    RutaInfoItem(stringResource(R.string.destino), ruta.destino)

                    // Hora salida
                    if (ruta.haciaDestino || ruta.finalizado) {
                        ruta.momentoSalida?.let {
                            RutaInfoItem(
                                stringResource(R.string.salida),
                                formatoFecha.format(Date(it * 1000))
                            )
                        }
                    }

                    // Hora llegada real o estimada
                    if (ruta.finalizado && ruta.momentoLlegada != null) {
                        RutaInfoItem(
                            stringResource(R.string.llegada),
                            formatoFecha.format(Date(ruta.momentoLlegada!! * 1000))
                        )
                    } else if (ruta.haciaDestino && ruta.momentoSalida != null && ruta.duracion != null) {
                        val estimada = Date((ruta.momentoSalida!! + ruta.duracion.toLong()) * 1000)
                        RutaInfoItem(
                            stringResource(R.string.horaEstimadaLlegada),
                            formatoFecha.format(estimada)
                        )
                    }

                    // Duración
                    ruta.duracion?.let {
                        RutaInfoItem(
                            stringResource(R.string.duracion),
                            rutaViewModel.formatDuration(it.toInt())
                        )
                    }

                    // Distancia
                    RutaInfoItem(
                        stringResource(R.string.distancia),
                        "%.1f km".format(ruta.distancia.toDouble() / 1000)
                    )

                    // Estado
                    RutaInfoItem(
                        stringResource(R.string.estado),
                        when {
                            ruta.finalizado -> stringResource(R.string.trayectoFinalizado)
                            ruta.haciaDestino -> stringResource(R.string.enRutaHaciaElDestino)
                            ruta.haciaCliente -> stringResource(R.string.taxiEnCaminoHaciaElCliente)
                            ruta.asignado -> stringResource(R.string.rutaAsignadaATaxista)
                            else -> stringResource(R.string.buscandoTaxi)
                        }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            if (!ruta.finalizado) {
                Button(
                    onClick = {
                        //Log.d("NuevaRuta", "Finalizando ruta con id = ${ruta.id}")
                        Log.d("NuevaRuta", "Finalizando ruta $ruta")
                        rutaViewModel.marcarRutaFinalizada(ruta.id)
                        rutaViewModel.deseleccionarRuta()
                        navController.navigate(Routes.Main)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.cancelarRuta))
                }
            }
        }
    }
}
 */

@Composable
fun RutaInfoItem(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        Divider(modifier = Modifier.padding(vertical = 8.dp))
    }
}

@Composable
fun DetallesRuta(
    navController: NavController,
    loginViewModel: LoginViewModel,
    rutaViewModel: RutaViewModel,
    localizacionViewModel: LocalizacionViewModel
) {
    val formatoFecha = remember {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    }

    val rutaActiva by rutaViewModel.selectedRuta.observeAsState()

    LaunchedEffect(rutaActiva?.id) {
        rutaActiva?.id?.takeIf { it.isNotBlank() }?.let { rutaId ->
            rutaViewModel.startAutoRefresh(rutaId)
        }
    }

    DisposableEffect(Unit) {
        onDispose { rutaViewModel.stopAutoRefresh() }
    }

    rutaActiva?.let { ruta ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(R.string.detallesDeRuta),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    RutaInfoItem(stringResource(R.string.origen), ruta.origen)
                    RutaInfoItem(stringResource(R.string.destino), ruta.destino)

                    if (ruta.haciaDestino || ruta.finalizado) {
                        ruta.momentoSalida?.let {
                            RutaInfoItem(
                                stringResource(R.string.salida),
                                formatoFecha.format(Date(it * 1000))
                            )
                        }
                    }

                    if (ruta.finalizado && ruta.momentoLlegada != null) {
                        RutaInfoItem(
                            stringResource(R.string.llegada),
                            formatoFecha.format(Date(ruta.momentoLlegada!! * 1000))
                        )
                    } else if (ruta.haciaDestino && ruta.momentoSalida != null && ruta.duracion != null) {
                        val estimada = Date((ruta.momentoSalida!! + ruta.duracion.toLong()) * 1000)
                        RutaInfoItem(
                            stringResource(R.string.horaEstimadaLlegada),
                            formatoFecha.format(estimada)
                        )
                    }

                    ruta.duracion?.let {
                        RutaInfoItem(
                            stringResource(R.string.duracion),
                            rutaViewModel.formatDuration(it.toInt())
                        )
                    }

                    RutaInfoItem(
                        stringResource(R.string.distancia),
                        "%.1f km".format(ruta.distancia.toDouble() / 1000)
                    )

                    RutaInfoItem(
                        stringResource(R.string.estado),
                        when {
                            ruta.finalizado -> stringResource(R.string.trayectoFinalizado)
                            ruta.haciaDestino -> stringResource(R.string.enRutaHaciaElDestino)
                            ruta.haciaCliente -> stringResource(R.string.taxiEnCaminoHaciaElCliente)
                            ruta.asignado -> stringResource(R.string.rutaAsignadaATaxista)
                            else -> stringResource(R.string.buscandoTaxi)
                        }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            //Mostrar mapa si asignado y haciaCliente son true
            if (!ruta.finalizado && ruta.asignado && ruta.haciaCliente) {
                val clientePos = LatLng(ruta.origenGeo.latitude, ruta.origenGeo.longitude)
                val conductorPos = LatLng(ruta.posicionConductor.latitude, ruta.posicionConductor.longitude)
                val cameraPositionState = rememberCameraPositionState()

                // Ajustar cámara para que se vean ambos puntos
                LaunchedEffect(clientePos, conductorPos) {
                    val bounds = LatLngBounds.builder()
                        .include(clientePos)
                        .include(conductorPos)
                        .build()
                    cameraPositionState.animate(
                        update = CameraUpdateFactory.newLatLngBounds(bounds, 100)
                    )
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState
                    ) {
                        Marker(
                            position = clientePos,
                            title = stringResource(R.string.tu),
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                        )
                        Marker(//37.4219983 , -122.084
                            position = conductorPos,
                            title = stringResource(R.string.conductor),
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Distancia hasta el cliente: %.1f km".format(ruta.distanciaConductor / 1000f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Tiempo estimado: ${rutaViewModel.formatDuration(ruta.duracionConductor)}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            if (!ruta.finalizado) {
                Button(
                    onClick = {
                        Log.d("DetallesRuta", "Finalizando ruta $ruta")
                        rutaViewModel.marcarRutaCancelada(ruta.id)
                        //rutaViewModel.marcarRutaFinalizada(ruta.id)
                        rutaViewModel.deseleccionarRuta()
                        navController.navigate(Routes.Main)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    //enabled = ruta.finalizado == false && ruta.asignado == false //Una vez se asigna la ruta ya no se puede cancelar
                    enabled = ruta.finalizado == false
                ) {
                    Text(stringResource(R.string.cancelarRuta))
                }
            }
        }
    }
}