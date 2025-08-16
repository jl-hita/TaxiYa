package com.jlhipe.taxiya.ui.screens.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.google.android.gms.maps.model.LatLng
import com.jlhipe.taxiya.model.Ruta
import com.jlhipe.taxiya.navigation.Routes
import com.jlhipe.taxiya.ui.screens.crearRuta.LocalizacionViewModel

@Composable
fun ListaDeRutas(
    rutas: List<Ruta>,
    //onRutaClick: (Ruta) -> Unit,
    navController: NavController,
    rutaViewModel: RutaViewModel,
    localizacionViewModel: LocalizacionViewModel,
    buscaTaxi: Boolean = false,
) {
    val userLocation by localizacionViewModel.ubicacion.observeAsState()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(rutas, key = { it.id }) { ruta ->
            val p1 = LatLng(ruta.origenGeo.latitude, ruta.origenGeo.longitude)
            val p2 = LatLng(userLocation?.first()!!.latitude, userLocation?.first()!!.longitude)
            val distancia: Long = rutaViewModel.calcularDistancia(p1, p2)

            RutaItem(ruta = ruta, distancia, buscaTaxi, onClick = {
                rutaViewModel.setRuta(ruta)
                rutaViewModel.actualizarPuedeVolver(true)
                navController.navigate(Routes.DetallesRuta)
            })
        }
    }
}