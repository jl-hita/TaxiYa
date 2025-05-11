package com.jlhipe.taxiya

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.jlhipe.taxiya.navigation.Navigation
import com.jlhipe.taxiya.ui.screens.login.LoginViewModel
import com.jlhipe.taxiya.ui.screens.main.RutaViewModel
import com.jlhipe.taxiya.ui.theme.TaxiYaTheme
import android.Manifest
import android.content.pm.PackageManager
import com.jlhipe.taxiya.ui.screens.nuevaruta.LocalizacionViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        /*
         * Pedimos permisos
         */
        //Variable para crear la ventana de petición de permisos
        val requestPermissionResultLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            //TODO realizar acciones cuando el usuario otorga o deniega los permisos
            if(isGranted) {} else {}
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionResultLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        setContent {
            TaxiYaTheme {
                val loginViewModel by viewModels<LoginViewModel>()
                val rutaViewModel by viewModels<RutaViewModel>()
                val localizacionViewModel: LocalizacionViewModel by viewModels()
                Navigation(loginViewModel, rutaViewModel, localizacionViewModel)
            }
        }
    }
}

