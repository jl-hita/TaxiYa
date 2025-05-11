package com.jlhipe.taxiya.ui.theme.screens.layout

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.google.maps.android.compose.GoogleMap
import com.jlhipe.taxiya.ui.screens.login.LoginViewModel
import com.jlhipe.taxiya.ui.screens.main.RutaViewModel
import com.jlhipe.taxiya.ui.theme.screens.layout.AppScaffold
import android.Manifest
import android.annotation.SuppressLint
import androidx.annotation.RequiresPermission
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.firebase.firestore.GeoPoint
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.jlhipe.taxiya.R
import com.jlhipe.taxiya.model.Ruta
import com.jlhipe.taxiya.ui.screens.nuevaruta.LocalizacionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@RequiresPermission(
    anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION],
)
@SuppressLint("MissingPermission")
@Composable
fun Mapa(
    localizacionViewModel: LocalizacionViewModel
) {
    


}
    /*
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val locationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }
    var locationInfo by remember {
        mutableStateOf("")
    }
    val usePreciseLocation = true

    //val ubicacion: LatLng by localizacionViewModel.ubicacion.observeAsState()
    val ubicacion: List<LatLng> by localizacionViewModel.ubicacion.observeAsState(initial = emptyList())
    val latitud: Double by localizacionViewModel.latitud.observeAsState(0.0)
    val longitud: Double by localizacionViewModel.longitud.observeAsState(0.0)

    //var ubicacion: LatLng by remember { mutableStateOf(LatLng(latitud, longitud)) }


    val singapore = LatLng(1.35, 103.87)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(ubicacion.firstOrNull()!!, 10f)
    }

    //Seguimos la ubicación del dispositivo para pasarlo al mapa
    LaunchedEffect(key1 = true) {
        scope.launch(Dispatchers.IO) {
            val priority = if (usePreciseLocation) {
                Priority.PRIORITY_HIGH_ACCURACY
            } else {
                Priority.PRIORITY_BALANCED_POWER_ACCURACY
            }
            val result = locationClient.getCurrentLocation(
                priority,
                CancellationTokenSource().token,
            ).await()
            result?.let { fetchedLocation ->
                //TODO no se puede invocar setValue en un background thread
                localizacionViewModel.setUbicacion(fetchedLocation.latitude, fetchedLocation.longitude)
            }
        }
    }


    AppScaffold() {
        /*
        GoogleMap(
            modifier = Modifier.fillMaxHeight(0.5F),
        ) {

        }
         */

        GoogleMap(
            modifier = Modifier.fillMaxHeight(0.8F),
            //cameraPositionState = cameraState,
            properties = MapProperties(
                isMyLocationEnabled = true,
                mapType = MapType.NORMAL,
                isTrafficEnabled = true
            )
        ) {
            Marker(
                title = stringResource(R.string.yo),
                snippet = stringResource(R.string.miPosicion),
                draggable = true,
                position = ubicacion.firstOrNull()!!
            )
        }
/*
        Column(
            Modifier
                .fillMaxWidth()
                .animateContentSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(
                onClick = {
                    // getting last known location is faster and minimizes battery usage
                    // This information may be out of date.
                    // Location may be null as previously no client has access location
                    // or location turned of in device setting.
                    // Please handle for null case as well as additional check can be added before using the method
                    scope.launch(Dispatchers.IO) {
                        val result = locationClient.lastLocation.await()
                        locationInfo = if (result == null) {
                            "No last known location. Try fetching the current location first"
                        } else {
                            "Current location is \n" + "lat : ${result.latitude}\n" +
                                    "long : ${result.longitude}\n" + "fetched at ${System.currentTimeMillis()}"
                        }
                    }
                },
            ) {
                Text("Get last known location")
            }

            Button(
                onClick = {
                    //To get more accurate or fresher device location use this method
                    scope.launch(Dispatchers.IO) {
                        val priority = if (usePreciseLocation) {
                            Priority.PRIORITY_HIGH_ACCURACY
                        } else {
                            Priority.PRIORITY_BALANCED_POWER_ACCURACY
                        }
                        val result = locationClient.getCurrentLocation(
                            priority,
                            CancellationTokenSource().token,
                        ).await()
                        result?.let { fetchedLocation ->
                            localizacionViewModel.setUbicacion(fetchedLocation.latitude, fetchedLocation.longitude)
                            /*
                            locationInfo =
                                "Current location is \n" + "lat : ${fetchedLocation.latitude}\n" +
                                        "long : ${fetchedLocation.longitude}\n" + "fetched at ${System.currentTimeMillis()}"
                             */
                        }
                    }
                },
            ) {
                Text(text = "Get current location")
            }
            Text(
                text = locationInfo,
            )
        }
         */

     /*
    }
}
*/