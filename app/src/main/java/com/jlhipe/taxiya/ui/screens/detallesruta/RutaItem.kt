package com.jlhipe.taxiya.ui.screens.detallesruta

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.jlhipe.taxiya.ui.screens.main.formatDuration

@Composable
fun RutaItem(
    ruta: Ruta,
    onClick: () -> Unit
) {
    val distanciaKm = remember(ruta.distancia) {
        "%.2f km".format(ruta.distancia / 1000.0)
    }
    val duracionFormateada = remember(ruta.duracion) {
        formatDuration(ruta.duracion)
    }
    val backgroundColor = if (!ruta.cancelada) {
        Color(0xFFD0F0C0) // verde claro
    } else {
        Color(0xFFFFC0C0) // rojo claro
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            /*
            Text("Origen: ${ruta.origen}", fontWeight = FontWeight.Bold)
            Text("Destino: ${ruta.destino}")
            Text("Distancia: $distanciaKm")
            Text("Duración: $duracionFormateada")
             */
            /*
            Text(
                text = "${ruta.origen} → ${ruta.destino}",
                style = MaterialTheme.typography.titleMedium
            )
             */
            Text(
                text = stringResource(R.string.origen) +": ${ruta.origen}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = stringResource(R.string.destino) +": ${ruta.destino}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Distancia: $distanciaKm",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Duración: $duracionFormateada",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}