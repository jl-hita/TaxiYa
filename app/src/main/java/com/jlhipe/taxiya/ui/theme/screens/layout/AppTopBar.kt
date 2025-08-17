package com.jlhipe.taxiya.ui.theme.screens.layout

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.jlhipe.taxiya.R
import com.jlhipe.taxiya.navigation.Routes
import com.jlhipe.taxiya.ui.screens.login.LoginViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    showBackArrow: Boolean = false,     // Sirve para indicar si se mostrará o no la flecha atrás
    onClickBlackArrow: () -> Unit,      // Se le pasa la acción de la flecha mediante parámetro
    loginViewModel: LoginViewModel,
    navController: NavController,
) {
    var expanded by remember { mutableStateOf(false) }

    CenterAlignedTopAppBar(
        title = {
            Text(
                text = stringResource(R.string.titulo),
                fontSize = 30.sp
            )
        },
        navigationIcon = {
            if (showBackArrow) {
                IconButton(onClick = onClickBlackArrow) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.goBack),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = { expanded = true }) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menú"
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.perfil)) },
                    onClick = {
                        expanded = false
                        navController.navigate(Routes.PerfilUsuario)
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.cerrarSesion)) },
                    onClick = {
                        expanded = false
                        loginViewModel.launchCatching {
                            loginViewModel.signOut()
                            withContext(Dispatchers.Main) {
                                navController.navigate(Routes.Login) {
                                    popUpTo(Routes.Main) { inclusive = true }
                                }
                            }
                        }
                    }
                )
                val context = LocalContext.current

                DropdownMenuItem(
                    text = { Text(stringResource(R.string.salir)) },
                    onClick = {
                        (context as? Activity)?.finishAffinity()
                    }
                )
            }
        },
        colors = TopAppBarDefaults.largeTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}