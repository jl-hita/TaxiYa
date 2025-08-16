package com.jlhipe.taxiya.ui.screens.buscarCliente

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.jlhipe.taxiya.R
import com.jlhipe.taxiya.ui.screens.crearRuta.LocalizacionViewModel
import com.jlhipe.taxiya.ui.screens.login.LoginViewModel
import com.jlhipe.taxiya.ui.screens.main.ListaDeRutas
import com.jlhipe.taxiya.ui.screens.main.RutaViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(FlowPreview::class)
@RequiresPermission(
    anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION],
)

@Composable
fun BuscarClienteConPermisos(
    navController: NavController,
    loginViewModel: LoginViewModel,
    rutaViewModel: RutaViewModel,
    localizacionViewModel: LocalizacionViewModel
) {
    //TODO Cargar una lista de rutas ordenadas por proximidad al taxi, limitada a 5 elementos (en el viewmodel)
    val user = loginViewModel.user
    val userLocation by localizacionViewModel.ubicacion.observeAsState()
    //val userLocation = remember { mutableStateOf(LatLng(0.0, 0.0)) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    //val rutasBuscandoTaxi by rutaViewModel.rutasBuscandoTaxi.observeAsState(emptyList())
    val rutasBuscandoTaxi by rutaViewModel.rutasBuscandoTaxi.observeAsState()

    //Cliente de localización
    val locationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    //Obtenemos la ubicación del usuario
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

        //localizacionViewModel.setDestino(0.0, 0.0)
    }
    /*
    // Actualiza userLocation cuando el ViewModel tenga valor
    LaunchedEffect(ubicacionActualizada) {
        ubicacionActualizada?.firstOrNull()?.let {
            val nuevaUbicacion = LatLng(it.latitude, it.longitude)
            userLocation.value = nuevaUbicacion
        }
    }
    */

    LaunchedEffect(user) {
        user.value?.let { userLocation?.let { it1 -> rutaViewModel.cargarRutasBuscandoTaxi(it.id, it1.first()) } }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            //.fillMaxHeight(0.7F)
            //.fillMaxWidth()
            .padding(16.dp)
    ) {
        Spacer(Modifier.height(16.dp))

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

        if (rutasBuscandoTaxi != null) {
            ListaDeRutas(
                rutas = rutasBuscandoTaxi!!,
                navController = navController,
                rutaViewModel,
                localizacionViewModel,
                buscaTaxi = true,
            )
        }
    }

}