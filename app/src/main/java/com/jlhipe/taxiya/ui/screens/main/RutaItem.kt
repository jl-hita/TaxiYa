package com.jlhipe.taxiya.ui.screens.main

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.jlhipe.taxiya.R
import com.jlhipe.taxiya.model.Ruta
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RutaItem(
    ruta: Ruta,
    distancia: Long,
    buscaTaxi: Boolean = false, //Si buscaTaxi == true -> Muestra distancia entre cliente y taxista
    onClick: () -> Unit
) {
    val distanciaKm = remember(ruta.distancia) {
        "%.2f km".format(ruta.distancia / 1000.0)
    }
    val duracionFormateada = remember(ruta.duracion) {
        formatDuration(ruta.duracion)
    }
    val backgroundColor = if (!ruta.cancelada) {
        Log.d("RutaItem", "Ruta NO CANCELADA")
        Color(0xFFD0F0C0) // verde claro
    } else {
        Log.d("RutaItem", "Ruta CANCELADA")
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
        val formatoFecha = remember {
            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        }

        Column(modifier = Modifier.padding(16.dp)) {
            if(buscaTaxi) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(formatoFecha.format(Date(ruta.fechaCreacion!!)))
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(stringResource(R.string.distanciaACliente))
                            }
                            append("%.1f km".format(distancia.toDouble() / 1000))
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(formatoFecha.format(Date(ruta.fechaCreacion!!)))
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(stringResource(R.string.de) + ": ")
                    }
                    append(ruta.origen)
                },
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(stringResource(R.string.a) + ": ")
                    }
                    append(ruta.destino)
                },
                style = MaterialTheme.typography.bodyMedium
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(stringResource(R.string.distancia) + ": ")
                        }
                        append(distanciaKm)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(stringResource(R.string.duracion) + ": ")
                        }
                        append(duracionFormateada)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
            }

        }
    }
}

