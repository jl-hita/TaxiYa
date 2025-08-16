package com.jlhipe.taxiya.ui.screens.detallesruta

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import kotlin.math.log

@Composable
fun RutaInfoItem(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        Divider(modifier = Modifier.padding(vertical = 8.dp))
    }
}

//TODO Modificar para que si el usuario es conductor -> muestra la posibilidad de asignar ruta

@Composable
fun DetallesRuta(
    navController: NavController,
    loginViewModel: LoginViewModel,
    rutaViewModel: RutaViewModel,
    localizacionViewModel: LocalizacionViewModel,
    //esConductor: Boolean = false, //Si esConductor == true -> Muestra botones específicos para conductor
) {
    val formatoFecha = remember {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    }

    val esConductor = loginViewModel.esConductor.collectAsState()
    val rutaActiva by rutaViewModel.selectedRuta.observeAsState()
    val user by loginViewModel.user.observeAsState()
    val routesApiKey: String = stringResource(R.string.routesAPI)
    //Cambiamos el color de fondo según estado de la ruta
    val colorFondo = if (rutaActiva?.finalizado == false) Color.White
        else if(rutaActiva?.cancelada == true) Color.Red
        else if(rutaActiva?.enDestino == true) Color.Green
        else if(rutaActiva?.asignado == true) Color(0xFFE3F2FD)
        else Color.White
    val ubicacionActualizada by localizacionViewModel.ubicacion.observeAsState()
    var showDialog by remember { mutableStateOf(false) }

    //val puedeVolver by rutaViewModel.puedeVolver.observeAsState()
    //val puedeVolver by rutaViewModel.puedeVolver.observeAsState(initial = false)

    LaunchedEffect(rutaActiva?.id) {
        rutaActiva?.id?.takeIf { it.isNotBlank() }?.let { rutaId ->
            rutaViewModel.startAutoRefresh(rutaId)
        }
    }

    // Cada vez que cambia la ubicación, se actualiza la ubicacion en el objeto ruta
    LaunchedEffect(ubicacionActualizada) {
        ubicacionActualizada?.firstOrNull()?.let { nuevaUbicacion ->
            if(esConductor.value) {
                rutaViewModel.cambiaUbicacionConductor(nuevaUbicacion)
            } else {
                rutaViewModel.cambiaUbicacionCliente(nuevaUbicacion)
            }
        }
    }

    //Para controlar la ubicación en tiempo real
    LaunchedEffect(Unit) {
        if(esConductor.value) {
            localizacionViewModel.startLocationUpdates()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            rutaViewModel.stopAutoRefresh()
            localizacionViewModel.stopLocationUpdates()
        }
    }

    /*
     * TODO:
     *  - Revisar botones Conductor
     *  - Añadir botón cancelar ruta una vez asignado, por si tuviera problemas con el cliente -> Notifiación a usuario
     */

    rutaActiva?.let { ruta ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorFondo)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(30.dp))

            Log.d("DetallesRuta", "puedeVolver = ${rutaViewModel.puedeVolver}")

            /**
             * Muestra flecha para volver si:
             *  - (user.id == rutaActiva.conductor || user.id == rutaActiva.cliente) && rutaActiva.finalizado)
             *
             *  - rutaViewModel.puedeVolver == true
             *  - esConductor == true && rutaActiva.asignada == false
             *  => Quizás podría sustituirse todo por:
             *  - (user.id == rutaActiva.conductor || user.id == rutaActiva.cliente) &&
             *
             */
            if(rutaViewModel.puedeVolver || ((user!!.id == rutaActiva!!.conductor || user!!.id == rutaActiva!!.cliente) && rutaActiva!!.finalizado)) {
                Row(
                    modifier = Modifier
                        .clickable {
                            rutaViewModel.deseleccionarRuta()
                            navController.popBackStack()
                        }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver"
                    )
                    Text(
                        text = stringResource(R.string.volver), //"Volver",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            if(!ruta.finalizado) {
                Text(
                    text = stringResource(R.string.detallesDeRutaActiva),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = stringResource(R.string.detallesDeRuta),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

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

            //Mostrar mapa si asignado y haciaCliente son true
            if (!ruta.finalizado && ruta.asignado && ruta.haciaCliente) {
                Spacer(Modifier.height(24.dp))

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

                val titleCliente = if (esConductor.value) {
                    stringResource(R.string.cliente)
                } else {
                    stringResource(R.string.tu)
                }

                val titleConductor = if (esConductor.value) {
                    stringResource(R.string.tu)
                } else {
                    stringResource(R.string.conductor)
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
                            title = titleCliente,
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                        )
                        Marker(
                            position = conductorPos,
                            title = titleConductor,
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                        )
                        Marker(
                            position = conductorPos,
                            title = stringResource(R.string.destino),
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
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

            /**
             * Botón de cancelar ruta
             * La podrá cancelar el cliente cuando no esté finalizada
             * La podrá cancelar el conductor cuando la tenga asignada y no haya finalizado
             */
            if ((!esConductor.value && !ruta.finalizado) || (esConductor.value && ruta.conductor == user!!.id)) {
                Button(
                    onClick = { showDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.cancelarRuta), color = Color.White)
                }
            }

            // Diálogo de confirmación
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text(stringResource(R.string.confirmarCancelar)) },
                    text = { Text(stringResource(R.string.estasSeguroCancelarRuta)) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDialog = false
                                Log.d("DetallesRuta", "Finalizando ruta $ruta")
                                rutaViewModel.marcarRutaCancelada(ruta.id)
                                //rutaViewModel.marcarRutaFinalizada(ruta.id)
                                rutaViewModel.deseleccionarRuta()
                                rutaViewModel.loadRutas()
                                navController.navigate(Routes.Main)
                            }
                        ) {
                            Text(stringResource(R.string.siEliminar), color = MaterialTheme.colorScheme.error) //"Sí, eliminar"
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text(stringResource(R.string.cancelar))
                        }
                    }
                )
            }

            /**
             * Botón de eliminar ruta
             */
            if(ruta.finalizado) {
                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        Log.d("DetallesRuta", "Eliminando ruta $ruta")
                        rutaViewModel.eliminarRuta(ruta.id, esConductor.value)
                        rutaViewModel.deseleccionarRuta()
                        rutaViewModel.loadRutas()
                        navController.navigate(Routes.Main)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    //enabled = ruta.finalizado == false && ruta.asignado == false //Una vez se asigna la ruta ya no se puede cancelar
                    //enabled = ruta.finalizado == false
                ) {
                    Text(stringResource(R.string.eliminarRuta))
                }
            }

            /**
             * Botón de aceptar ruta (asignar)
             */
            if(esConductor.value && !ruta.finalizado && !ruta.asignado) {
                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        Log.d("DetallesRuta", "Asignando ruta $ruta")
                        //Calculamos el tiempo que tardará en llegar el conductor al cliente y editar ruta.duracionConductor
                        //Asignamos la ruta al conductor
                        user?.let { rutaViewModel.asignarRuta(ruta.id, it, routesApiKey) }
                        //TODO mandar notificación al cliente
                        //rutaViewModel.loadRutas()
                        //navController.navigate(Routes.Main)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    //enabled = ruta.finalizado == false && ruta.asignado == false //Una vez se asigna la ruta ya no se puede cancelar
                    //enabled = ruta.finalizado == false
                ) {
                    Text(stringResource(R.string.aceptarCliente))
                }
            }

            /**
             * Botón de desasignar ruta
             */
            if(esConductor.value && !ruta.finalizado && !ruta.asignado && ruta.conductor == user!!.id && !ruta.haciaDestino) {
                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        Log.d("DetallesRuta", "Desasignando ruta $ruta")
                        rutaViewModel.desasignarRuta(ruta.id)
                        //rutaViewModel.loadRutas()
                        //navController.navigate(Routes.Main)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    //enabled = ruta.finalizado == false && ruta.asignado == false //Una vez se asigna la ruta ya no se puede cancelar
                    //enabled = ruta.finalizado == false
                ) {
                    Text(stringResource(R.string.desasignarRuta))
                }
            }
                Spacer(Modifier.height(24.dp))

            /**
             * Botón de iniciar ruta
             */
            if(esConductor.value && !ruta.finalizado && !ruta.asignado && ruta.conductor == user!!.id && !ruta.haciaDestino && !ruta.haciaCliente) {
                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        Log.d("DetallesRuta", "Iniciando ruta $ruta")
                        rutaViewModel.iniciarRutaHaciaCliente(ruta.id)
                        //Una vez el conductor se dirige hacia el cliente se controla la distancia entre conductor y cliente
                        rutaViewModel.startTrackingDistancia()
                        //rutaViewModel.loadRutas()
                        //navController.navigate(Routes.Main)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    //enabled = ruta.finalizado == false && ruta.asignado == false //Una vez se asigna la ruta ya no se puede cancelar
                    //enabled = ruta.finalizado == false
                ) {
                    Text(stringResource(R.string.iniciarRuta))
                }
            }

            /**
             * Botón de ir hacia destino
             */
            if(esConductor.value && !ruta.finalizado && !ruta.asignado && ruta.conductor == user!!.id && !ruta.haciaDestino && ruta.haciaCliente) {
                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        Log.d("DetallesRuta", "Iniciando ruta $ruta")
                        rutaViewModel.iniciarRutaHaciaDestino()
                        //Una vez el conductor se dirige hacia el cliente se controla la distancia entre conductor y cliente
                        rutaViewModel.startTrackingDistancia()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    //enabled = ruta.finalizado == false && ruta.asignado == false //Una vez se asigna la ruta ya no se puede cancelar
                    //enabled = ruta.finalizado == false
                ) {
                    Text(stringResource(R.string.iniciarRutaHaciaDestino))
                }
            }

            /**
             * Indicamos de forma visual que el conductor se dirige hacia el cliente y tiempo aproximado (ruta.duracionConductor)
             * TODO Plantear cambiar condición de distancia a tiempo
             */
            if(!esConductor.value && !ruta.finalizado && ruta.asignado && ruta.haciaCliente && !ruta.haciaDestino && ruta.distanciaConductor >= 500) {
                Spacer(Modifier.height(24.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsBus, //un cochecito
                        contentDescription = stringResource(R.string.conductorEnCamino), //"Conductor en camino",
                        tint = Color(0xFF4CAF50), // verde
                        modifier = Modifier.size(32.dp)
                    )

                    Spacer(Modifier.width(8.dp))

                    val texto = if(esConductor.value) {
                        stringResource(R.string.conductorLejosCliente)
                    } else {
                        stringResource(R.string.clienteLejosConductor)
                    }
                    Text(
                        text = texto + " · " +
                                "${ruta.duracionConductor / 60}" + stringResource(R.string.minAprox),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            /**
             * Indicar de forma visual que el conductor está llegando a la posición del cliente
             * TODO Plantear cambiar condición de distancia a tiempo
             */
            if(!esConductor.value && !ruta.finalizado && ruta.asignado && ruta.haciaCliente && !ruta.haciaDestino && ruta.distanciaConductor < 500) {
                Spacer(Modifier.height(24.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn, // icono de llegada
                        contentDescription = stringResource(R.string.conductorLlegando),
                        tint = Color(0xFFFF9800), // naranja
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(8.dp))

                    val texto = if(esConductor.value) {
                        stringResource(R.string.llegandoACliente)
                    } else {
                        stringResource(R.string.conductorLlegando)
                    }
                    Text(
                        text = texto,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            /**
             * El conductor ha llegado al destino
             *
             * TODO check -> Otro if que controle que el conductor ha llegado a destino con la variable ruta.distanciaDestino < 50
             * TODO El conductor debe:
             *  - check -> Indicar de forma visual que el conductor/cliente ha llegado al destino <- El cliente debe tener su propio Text
             *  - check -> Marcar la ruta como finalizada, no cancelada
             *  - check -> Marcar ruta.finalizado = true
             *  - check -> Marcar ruta.cancelado = false
             *  - check -> Editar ruta.momentoLlegada
             *  - check -> Editar ruta.duracion
             *  - check -> ¿Detener el tracking de ubicación del cliente?
             *  - check -> Cambiar el color de fondo de la pantalla a verde o azul claro
             *  - check -> Mostrar botón para volver a MainScreen
             */
            //if(ruta.finalizado && ruta.asignado && ruta.distanciaDestino < 50) {
            if(ruta.finalizado && !ruta.cancelada && ruta.asignado && ruta.enDestino) {
                Spacer(Modifier.height(24.dp))

                //Indicar de forma visual que el conductor/cliente ha llegado al destino
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn, // icono de llegada
                        contentDescription = stringResource(R.string.hasLlegadoADestino),
                        tint = Color(0xFFFF9800), // naranja
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.hasLlegadoADestino),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                //Se detiene el tracking de ubicación
                LaunchedEffect(true) {
                    localizacionViewModel.stopLocationUpdates()
                }

                /**
                 * Botón para volver a MainScreen
                 */
                Button(
                    onClick = {
                        rutaViewModel.deseleccionarRuta()
                        rutaViewModel.loadRutas()
                        navController.navigate(Routes.Main)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.salir))
                }
            }
        }
    }
}