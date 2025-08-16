package com.jlhipe.taxiya.ui.screens.crearRuta

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.jlhipe.taxiya.R
import com.jlhipe.taxiya.navigation.Routes
import com.jlhipe.taxiya.ui.screens.locationpermissions.RequestLocationPermissions
import com.jlhipe.taxiya.ui.screens.login.LoginViewModel
import com.jlhipe.taxiya.ui.screens.main.RutaViewModel

@SuppressLint("MissingPermission")
@Composable
fun NuevaRuta(
    navController: NavController,
    loginViewModel: LoginViewModel,
    rutaViewModel: RutaViewModel,
    localizacionViewModel: LocalizacionViewModel
) {
    var permisosConcedidos by remember { mutableStateOf(false) }
    val rutaActiva by rutaViewModel.selectedRuta.observeAsState()
    val user = loginViewModel.user.value
    val esConductor = loginViewModel.esConductor.collectAsState()

    LaunchedEffect(user) {
        //Comprobamos si el usuario tiene una ruta activa, sin finalizar
        user?.id?.let { rutaViewModel.comprobarRutaActivaDelUsuario(it, esConductor.value) }
    }

    //Si hay ruta activa navegamos a DetallesRuta
    LaunchedEffect(rutaActiva) {
        if(rutaActiva != null) {
            navController.navigate(Routes.DetallesRuta)
        }
    }

    RequestLocationPermissions(
        onPermissionsGranted = { permisosConcedidos = true },
        onPermissionsDenied = { permisosConcedidos = false }
    )

    if (permisosConcedidos) {
        NuevaRutaConPermisos(
            navController,
            loginViewModel,
            rutaViewModel,
            localizacionViewModel
        )
    } else {
        Text(
            text = stringResource(R.string.necesitaPermisos),
            modifier = Modifier.padding(32.dp)
        )
    }
}