package com.jlhipe.taxiya.ui.screens.perfilUsuario

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.jlhipe.taxiya.R
import com.jlhipe.taxiya.ui.screens.login.LoginViewModel
import com.jlhipe.taxiya.ui.screens.main.RutaViewModel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import com.jlhipe.taxiya.ui.theme.screens.layout.NonAppScaffold

@Composable
fun PerfilUsuarioScreen(
    navController: NavController,
    loginViewModel: LoginViewModel,
    rutaViewModel: RutaViewModel,
    //onSave: () -> Unit
) {
    val user by loginViewModel.user.observeAsState()

    var nombre by remember { mutableStateOf(user?.nombre ?: "") }
    var apellidos by remember { mutableStateOf(user?.apellidos ?: "") }
    var esConductor by remember { mutableStateOf(user?.esConductor ?: false) }
    var showDialog by remember { mutableStateOf(false) }

    NonAppScaffold(
        navController = navController,
        showBack = false,
    ) {
        Text(stringResource(R.string.perfilDeUsuario), style = MaterialTheme.typography.headlineSmall)

        // ID (solo lectura)
        OutlinedTextField(
            value = user?.id ?: "",
            onValueChange = {},
            label = { Text(stringResource(R.string.id)) },
            readOnly = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Email (solo lectura)
        OutlinedTextField(
            value = user?.email ?: "",
            onValueChange = {},
            label = { Text(stringResource(R.string.email)) },
            readOnly = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Nombre
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text(stringResource(R.string.nombre)) },
            modifier = Modifier.fillMaxWidth()
        )

        // Apellidos
        OutlinedTextField(
            value = apellidos,
            onValueChange = { apellidos = it },
            label = { Text(stringResource(R.string.apellidos)) },
            modifier = Modifier.fillMaxWidth()
        )

        // Switch esConductor
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.esConductor))
            Switch(
                checked = esConductor,
                onCheckedChange = { esConductor = it }
            )
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            //Botón Guardar
            Button(
                onClick = {
                    loginViewModel.actualizarUsuario(
                        nombre = nombre,
                        apellidos = apellidos,
                        esConductor = esConductor
                    )
                    //navController.navigate(Routes.Main)
                    navController.popBackStack()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.guardarCambios))
            }

            //Botón cancelar
            Button(
                onClick = {
                    //navController.navigate(Routes.Main)
                    navController.popBackStack()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.cancelar))
            }
        }

        // Botón eliminar cuenta
        Button(
            onClick = { showDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.eliminarCuenta), color = Color.White)
        }

        // Diálogo de confirmación
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(stringResource(R.string.confirmarEliminacion)) }, //"Confirmar eliminación"
                text = { Text(stringResource(R.string.estasSeguroEliminar)) }, //"¿Estás seguro de que quieres eliminar tu cuenta? Esta acción no se puede deshacer."
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDialog = false
                            loginViewModel.eliminarCuentaYDatos()
                        }
                    ) {
                        Text(stringResource(R.string.siEliminar), color = MaterialTheme.colorScheme.error) //"Sí, eliminar"
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text(stringResource(R.string.cancelar))
                    }
                }
            )
        }
    }

    /*
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(Modifier.height(30.dp))

        Text(stringResource(R.string.perfilDeUsuario), style = MaterialTheme.typography.headlineSmall)

        // ID (solo lectura)
        OutlinedTextField(
            value = user?.id ?: "",
            onValueChange = {},
            label = { Text(stringResource(R.string.id)) },
            readOnly = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Email (solo lectura)
        OutlinedTextField(
            value = user?.email ?: "",
            onValueChange = {},
            label = { Text(stringResource(R.string.email)) },
            readOnly = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Nombre
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text(stringResource(R.string.nombre)) },
            modifier = Modifier.fillMaxWidth()
        )

        // Apellidos
        OutlinedTextField(
            value = apellidos,
            onValueChange = { apellidos = it },
            label = { Text(stringResource(R.string.apellidos)) },
            modifier = Modifier.fillMaxWidth()
        )

        // Switch esConductor
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.esConductor))
            Switch(
                checked = esConductor,
                onCheckedChange = { esConductor = it }
            )
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            //Botón Guardar
            Button(
                onClick = {
                    loginViewModel.actualizarUsuario(
                        nombre = nombre,
                        apellidos = apellidos,
                        esConductor = esConductor
                    )
                    //navController.navigate(Routes.Main)
                    navController.popBackStack()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.guardarCambios))
            }

            //Botón cancelar
            Button(
                onClick = {
                    //navController.navigate(Routes.Main)
                    navController.popBackStack()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.cancelar))
            }
        }

        // Botón eliminar cuenta
        Button(
            onClick = { showDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.eliminarCuenta), color = Color.White)
        }

        // Diálogo de confirmación
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(stringResource(R.string.confirmarEliminacion)) }, //"Confirmar eliminación"
                text = { Text(stringResource(R.string.estasSeguroEliminar)) }, //"¿Estás seguro de que quieres eliminar tu cuenta? Esta acción no se puede deshacer."
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDialog = false
                            loginViewModel.eliminarCuentaYDatos()
                        }
                    ) {
                        Text(stringResource(R.string.siEliminar), color = MaterialTheme.colorScheme.error) //"Sí, eliminar"
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text(stringResource(R.string.cancelar))
                    }
                }
            )
        }
    }
     */
}