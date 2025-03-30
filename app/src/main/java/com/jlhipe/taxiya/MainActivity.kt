package com.jlhipe.taxiya

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.jlhipe.taxiya.navigation.Navigation
import com.jlhipe.taxiya.ui.screens.login.LoginViewModel
import com.jlhipe.taxiya.ui.theme.TaxiYaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TaxiYaTheme {
                val loginViewModel by viewModels<LoginViewModel>()
                /*
                val rutaViewModel by viewModels<RutaViewModel>()
                Navigation(rutaViewModel)
                 */
                Navigation(loginViewModel)
            }
        }
    }
}