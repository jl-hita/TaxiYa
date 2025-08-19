package com.jlhipe.taxiya.ui.theme.screens.layout

import android.content.res.Resources.Theme
import android.graphics.drawable.Icon
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.jlhipe.taxiya.R
import com.jlhipe.taxiya.ui.screens.login.LoginViewModel

@Composable
fun AppScaffold(
    showBackArrow: Boolean = false,         // Sirve para indicar si se mostrará o no la flecha atrás
    onBlackArrowClick: () -> Unit = {},     // Se le pasa la acción de la flecha mediante parámetro
    //bottomContent: @Composable () -> Unit,  // Se le pasa el contenido del campo inferior mediante parámetro
    showActionButton: Boolean = false,
    botonAccion: () -> Unit = {},  //Botón de acción (nueva ruta)
    loginViewModel: LoginViewModel,
    navController: NavController,
    actionButtonIcon: ImageVector = Icons.Filled.Add,
    //textoBotonAccion: String,
    content: @Composable () -> Unit         // Se le pasa el contenido principal mediante parámetro
) {
    Scaffold(
        topBar = {
            AppTopBar(
                showBackArrow = showBackArrow,
                onClickBlackArrow = onBlackArrowClick,
                loginViewModel = loginViewModel,
                navController = navController,
            )
        },
        floatingActionButton = {
            if(showActionButton) {
                Button(
                    onClick = botonAccion
                ) {
                    Icon(actionButtonIcon, contentDescription = "")
                    //Text(textoBotonAccion)
                }
            }
        },
        //bottomBar = bottomContent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(paddingValues),
                //.background(color = MaterialTheme.colorScheme.secondary),
                //.background(color = PaleFrost)
                //.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            content()
        }
    }
}