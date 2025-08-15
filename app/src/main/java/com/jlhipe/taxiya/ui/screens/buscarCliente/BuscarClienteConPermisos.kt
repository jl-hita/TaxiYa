package com.jlhipe.taxiya.ui.screens.buscarCliente

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.jlhipe.taxiya.ui.screens.crearRuta.LocalizacionViewModel
import com.jlhipe.taxiya.ui.screens.login.LoginViewModel
import com.jlhipe.taxiya.ui.screens.main.RutaViewModel
import kotlinx.coroutines.FlowPreview

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
}