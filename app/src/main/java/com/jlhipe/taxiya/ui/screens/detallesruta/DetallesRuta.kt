package com.jlhipe.taxiya.ui.screens.detallesruta

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.jlhipe.taxiya.R
import com.jlhipe.taxiya.model.Ruta
import com.jlhipe.taxiya.ui.screens.crearRuta.LocalizacionViewModel
import com.jlhipe.taxiya.ui.screens.login.LoginViewModel
import com.jlhipe.taxiya.ui.screens.main.RutaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    var fechaLlegada: Date = Date()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(stringResource(R.string.detallesDeRuta), style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        Text(stringResource(R.string.origen)+": ${rutaActiva?.origen}")
        Text(stringResource(R.string.destino)+": ${rutaActiva?.destino}")
        if(rutaActiva?.haciaDestino == true) {
            val fechaSalida = Date(rutaActiva?.momentoSalida!! * 1000) // de segundos a milisegundos
            fechaLlegada = Date(rutaActiva?.momentoLlegada!! * 1000) // de segundos a milisegundos
            Text(stringResource(R.string.salida)+": ${formatoFecha.format(fechaSalida)}")
        }
        if(rutaActiva?.finalizado == true && rutaActiva?.momentoLlegada != null) {
            Text(stringResource(R.string.llegada)+": ${formatoFecha.format(fechaLlegada)}")
        }

        //TODO calcular hora llegada y mostrar
        //Text("Precio: %.2f €".format(ruta.precio.toDouble() / 100))
        Text(stringResource(R.string.distancia)+ rutaActiva?.distancia?.let { ": %.1f km".format(it.toDouble()) })

        Text(stringResource(R.string.estado)+": " + when {
            rutaActiva?.finalizado == true -> stringResource(R.string.trayectoFinalizado) //"Finalizada"
            rutaActiva?.haciaDestino == true -> stringResource(R.string.enRutaHaciaElDestino)//"En ruta hacia el destino"
            rutaActiva?.haciaCliente == true -> stringResource(R.string.taxiEnCaminoHaciaElCliente)//"En camino al cliente"
            rutaActiva?.asignado == true -> stringResource(R.string.rutaAsignadaATaxista)//"Asignada"
            else -> stringResource(R.string.buscandoTaxi)//"Buscando taxi"
        })

        Spacer(modifier = Modifier.height(24.dp))

        if (rutaActiva?.finalizado == false) {
            Button(
                onClick = {
                    //TODO Marcar la ruta como finalizada en firebase
                    //TODO Navegar a página principal, quizás
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(R.string.cancelarRuta))
            }
        }
        //TODO añadir botón para volver
    }
}