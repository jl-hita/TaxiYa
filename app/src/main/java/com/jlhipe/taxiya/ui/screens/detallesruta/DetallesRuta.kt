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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jlhipe.taxiya.R
import com.jlhipe.taxiya.model.Ruta
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DetallesRuta(
    ruta: Ruta,
    onCancelarRuta: (rutaId: String) -> Unit
) {
    val formatoFecha = remember {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    }
    val fechaSalida = Date(ruta.momentoSalida!! * 1000) // de segundos a milisegundos
    val fechaLlegada = Date(ruta.momentoLlegada!! * 1000) // de segundos a milisegundos

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(stringResource(R.string.detallesDeRuta), style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        Text(stringResource(R.string.origen)+": ${ruta.origen}")
        Text(stringResource(R.string.destino)+": ${ruta.destino}")
        if(ruta.haciaDestino) {
            Text(stringResource(R.string.salida)+": ${formatoFecha.format(fechaSalida)}")
        }
        if(ruta.finalizado && ruta.momentoLlegada != null) {
            Text(stringResource(R.string.llegada)+": ${formatoFecha.format(fechaLlegada)}")
        }

        //TODO calcular hora llegada y mostrar
        //Text("Precio: %.2f €".format(ruta.precio.toDouble() / 100))
        Text(stringResource(R.string.distancia)+": %.1f km".format(ruta.distancia.toDouble()))

        Text(stringResource(R.string.estado)+": " + when {
            ruta.finalizado -> stringResource(R.string.trayectoFinalizado) //"Finalizada"
            ruta.haciaDestino -> stringResource(R.string.enRutaHaciaElDestino)//"En ruta hacia el destino"
            ruta.haciaCliente -> stringResource(R.string.taxiEnCaminoHaciaElCliente)//"En camino al cliente"
            ruta.asignado -> stringResource(R.string.rutaAsignadaATaxista)//"Asignada"
            else -> stringResource(R.string.buscandoTaxi)//"Buscando taxi"
        })

        Spacer(modifier = Modifier.height(24.dp))

        if (!ruta.finalizado) {
            Button(
                onClick = { onCancelarRuta(ruta.id) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(R.string.cancelarRuta))
            }
        }
    }
}