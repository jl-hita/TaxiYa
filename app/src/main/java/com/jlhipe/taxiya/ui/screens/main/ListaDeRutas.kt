package com.jlhipe.taxiya.ui.screens.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.jlhipe.taxiya.model.Ruta
import com.jlhipe.taxiya.navigation.Routes

@Composable
fun ListaDeRutas(
    rutas: List<Ruta>,
    //onRutaClick: (Ruta) -> Unit,
    navController: NavController,
    rutaViewModel: RutaViewModel
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(rutas, key = { it.id }) { ruta ->
            RutaItem(ruta = ruta, onClick = {
                rutaViewModel.setRuta(ruta)
                navController.navigate(Routes.DetallesRuta)
            })
        }
    }
}