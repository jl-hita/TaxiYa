package com.jlhipe.taxiya.ui.screens.registro

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.jlhipe.taxiya.R
import com.jlhipe.taxiya.model.User
import com.jlhipe.taxiya.navigation.Routes
import com.jlhipe.taxiya.ui.screens.login.LoginViewModel
import com.jlhipe.taxiya.ui.theme.BlueRibbon
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Composable
fun RegistroScreen(
    navController: NavHostController,
    loginViewModel: LoginViewModel
) {
    //Si está logeado envía a página Main
    //TODO solucionarlo con un estado boolean que se modifice al hacer login/out/etc
    //if(loginViewModel.hasUser()) navController.navigate(Routes.Main)
    //val logeado = loginViewModel.logeado.collectAsState()
    //var logeado by rememberSaveable { mutableStateOf(loginViewModel.logeado.value) }
    //if(logeado) navController.navigate(Routes.Main)
    val logeado: Boolean by loginViewModel.logeado.observeAsState(initial = false)
    //if(logeado) navController.navigate(Routes.Main)
    if(logeado) loginViewModel.navegar({ navController.navigate(Routes.Main) })

    //true si es conductor, false si es usuario
    var esConductor by rememberSaveable { mutableStateOf(false) }
    val error: String by loginViewModel.error.observeAsState(initial = "")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            //.background(color = PaleFrost)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        //Selecciona entre conductor y usuario
        Text(stringResource(R.string.esConductorPregunta))
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = esConductor,
                onClick = {
                    esConductor = true
                    loginViewModel.updateEsConductor(true)
                },
            )
            Text(stringResource(R.string.conductor))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = !esConductor,
                onClick = {
                    esConductor = false
                    loginViewModel.updateEsConductor(false)
                },
            )
            Text(stringResource(R.string.usuario))
        }

        //Variables para el formulario
        val email = loginViewModel.email.collectAsState()
        val password = loginViewModel.password.collectAsState()
        //val identificador: String = User.generateRandomID() //Generamos la ID de forma aleatoria
        val validado =
            true //true para pruebas. TODO En producción debe demostrar que tiene licencia taxi
        val nombre = loginViewModel.nombre.collectAsState()
        val apellidos = loginViewModel.apellidos.collectAsState()
        val activo = false //Con la cuenta recién creada no estará en ruta ni activo
        val enRuta = false

        //Bloque de formulario general para todos

        //Campo email
        OutlinedTextField(
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 4.dp)
                .border(
                    BorderStroke(width = 2.dp, color = BlueRibbon),
                    shape = RoundedCornerShape(50)
                ),
            value = email.value,
            onValueChange = { loginViewModel.updateEmail(it) },
            placeholder = { Text(stringResource(R.string.email)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = stringResource(R.string.email)
                )
            }
        )

        //Campo password
        OutlinedTextField(
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 4.dp)
                .border(
                    BorderStroke(width = 2.dp, color = BlueRibbon),
                    shape = RoundedCornerShape(50)
                ),
            value = password.value,
            onValueChange = { loginViewModel.updatePassword(it) },
            placeholder = { Text(stringResource(R.string.password)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = stringResource(R.string.password)
                )
            },
            visualTransformation = PasswordVisualTransformation()
        )

        //Campo nombre
        OutlinedTextField(
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 4.dp)
                .border(
                    BorderStroke(width = 2.dp, color = BlueRibbon),
                    shape = RoundedCornerShape(50)
                ),
            value = nombre.value,
            onValueChange = { loginViewModel.updateNombre(it) },
            placeholder = { Text(stringResource(R.string.nombre)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = stringResource(R.string.nombre)
                )
            },
        )

        //Campo apellidos
        OutlinedTextField(
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 4.dp)
                .border(
                    BorderStroke(width = 2.dp, color = BlueRibbon),
                    shape = RoundedCornerShape(50)
                ),
            value = apellidos.value,
            onValueChange = { loginViewModel.updateApellidos(it) },
            placeholder = { Text(stringResource(R.string.apellidos)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = stringResource(R.string.apellidos)
                )
            },
        )

        //Bloque de formulario específico para conductor
        if (esConductor) {
            //TODO formulario específico para conductor
            Text(text = "Bloque de formulario específico para conductor")

        }

        //Bloque de formulario específico para usuario
        if (!esConductor) {
            //TODO formulario específico para usuario
            Text(text = "Bloque de formulario específico para usuario")
        }

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        )

        //Muestra texto con error al crear cuenta de forma incorrecta
        if(error != "") {
            if(error == "The email address is badly formatted.")
                Text(stringResource(R.string.emailFormatoIncorrecto))
            else if(error == "The given password is invalid. [ Password should be at least 6 characters ]")
                Text(stringResource(R.string.passwordMuyCorto))
            else if(error =="The email address is already in use by another account.")
                Text(stringResource(R.string.emailYaExiste))
            else
                Text(error)
        }

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        )

        Button(
            onClick = { loginViewModel.onSignUpClick() },
            modifier = Modifier
                .fillMaxWidth(0.5F)
                .padding(16.dp, 0.dp)
        ) {
            Text(text = stringResource(R.string.sign_up_description), fontSize = 16.sp)
        }

        Spacer(modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp))

        Button(
            onClick = { navController.navigate(Routes.Login) },
            modifier = Modifier
                .fillMaxWidth(0.5F)
                .padding(16.dp, 0.dp)
        ) {
            Text(text = stringResource(R.string.volver), fontSize = 16.sp)
        }
    }
}