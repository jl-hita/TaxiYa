package com.jlhipe.taxiya.ui.screens.nuevaruta

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.jlhipe.taxiya.ui.screens.login.LoginViewModel
import com.jlhipe.taxiya.ui.screens.main.RutaViewModel
import com.jlhipe.taxiya.ui.theme.screens.layout.Mapa
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.CameraPositionState
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.Marker
import com.jlhipe.taxiya.R
import com.jlhipe.taxiya.navigation.Routes
import com.jlhipe.taxiya.ui.theme.screens.layout.AppScaffold


@SuppressLint("MissingPermission")
@Composable
fun NuevaRuta(
    navController: NavController,
    loginViewModel: LoginViewModel,
    rutaViewModel: RutaViewModel,
    localizacionViewModel: LocalizacionViewModel
) {
    //string dirección destino
    val destinoInput by localizacionViewModel.destino.observeAsState(initial = "")
    val destinoLocation = remember{ mutableStateOf((LatLng(localizacionViewModel.destinoLocation.value?.get(0)?.latitude!!, localizacionViewModel.destinoLocation.value?.get(0)?.longitude!!)))}

    //Ubicación del user
    val userLocation = remember{ mutableStateOf((LatLng(localizacionViewModel.ubicacion.value?.get(0)?.latitude!!, localizacionViewModel.ubicacion.value?.get(0)?.longitude!!)))}
    //TODO Hacer que en el init se obtenga la ubicación, ahora mismo es 0,0
    //val userLocation by localizacionViewModel.ubicacion.observeAsState()

    //Estado posición cámara
    var cameraPositionState = rememberCameraPositionState{ position = CameraPosition.fromLatLngZoom(userLocation.value, 10f) }

    AppScaffold (
        showBackArrow = true,
        onBlackArrowClick = { navController.popBackStack() },
        //botonAccion = navController.navigate(Routes.NuevaRuta)
        showActionButton = true, //TODO cambiar por un check si usuario es conductor
        botonAccion = {
            navController.navigate(Routes.NuevaRuta);
        },
        //bottomContent = { BottomBar(modifier = Modifier.padding(vertical = 4.dp), bookViewModel = bookViewModel) }
    ) {
        if (localizacionViewModel.tienePermisosGPS()) {
            //Mapa(localizacionViewModel)

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                /*
                Text(
                    stringResource(R.string.escogeDestino),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
                 */
                var newLocation : LatLng

                TextField(value = destinoInput,
                    onValueChange = {
                        //Guarda el nombre de calle
                        localizacionViewModel.setDestinoInput(it)
                        //Convierte de nombre de calle a coordenadas
                        localizacionViewModel.requestCoordsFromAdress(it)
                        //TODO Establece la cámara del mapa
                        //newLocation = (LatLng(destinoLocation.value.latitude,destinoLocation.value.longitude)) <- Quizás cambiar por userLocation
                        //onLocationSelected(newLocation)
                    },
                    label = { Text(text = stringResource(R.string.escogeDestino)) }
                )

                Button(onClick = {
                    //TODO establece destino y busca conductor libre
                    //TODO cambia estado de la ruta
                    //TODO Guarda la ruta en firebase
                }) {
                    Text(text = stringResource(R.string.buscaTaxiLibre))
                }

                GoogleMap(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    cameraPositionState = cameraPositionState,
                    onMapClick = {
                        userLocation.value = it
                    }
                ) {
                    //Marker(state = MarkerState(userLocation.value))
                    Marker(position = userLocation.value)
                }
            }
        } else {
            Text(stringResource(R.string.necesitaPermisos))
        }
    }
}
