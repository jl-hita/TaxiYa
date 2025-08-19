package com.jlhipe.taxiya.ui.screens.login

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CustomCredential
import androidx.navigation.NavHostController
import com.jlhipe.taxiya.R
import com.jlhipe.taxiya.navigation.Routes
import com.jlhipe.taxiya.ui.theme.BlueRibbon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavHostController, loginViewModel: LoginViewModel) {
    val logeado by loginViewModel.logeado.observeAsState(false)
    val email = loginViewModel.email.collectAsState()
    val password = loginViewModel.password.collectAsState()
    val error by loginViewModel.error.observeAsState("")
    val usuario by loginViewModel.user.observeAsState()

    //Navegar a Main si está logeado y haya usuario cargado
    LaunchedEffect(logeado, usuario) {
        if (logeado && usuario != null) {
            navController.navigate(Routes.Main) {
                popUpTo(Routes.Login) { inclusive = true }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
        //color = Color.Red
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val logo = if(isSystemInDarkTheme()) R.drawable.logo_inverso_dark else R.drawable.logo_inverso
            // Imagen
            Image(
                painter = painterResource(id = logo),
                contentDescription = "Auth image",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 4.dp),
                alpha = 1f
            )

            // Email
            OutlinedTextField(
                value = email.value,
                onValueChange = { loginViewModel.updateEmail(it) },
                placeholder = { Text(stringResource(R.string.email)) },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            // Password
            OutlinedTextField(
                value = password.value,
                onValueChange = { loginViewModel.updatePassword(it) },
                placeholder = { Text(stringResource(R.string.password)) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            // Error
            if (error.isNotEmpty()) {
                Text(text = error, color = Color.Red)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(0.8F),
                horizontalArrangement = Arrangement.Center
            ) {
                // Botón Login
                Button(
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            loginViewModel.signIn(email.value, password.value)
                        }
                    },
                    modifier = Modifier.weight(0.2f)
                ) {
                    Text(stringResource(R.string.iniciarSesion))
                }

                Spacer(modifier = Modifier.width(24.dp))

                // Botón Registro
                Button(
                    onClick = {
                        navController.navigate(Routes.Registro)
                    },
                    modifier = Modifier.weight(0.2f)
                ) {
                    Text(stringResource(R.string.crearCuenta))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Login con Google
            AuthenticationButton(
                modifier = Modifier.fillMaxWidth(0.83F),
                buttonText = R.string.loginConGoogle,
                //onRequestResult = { loginViewModel.setLogeado(true) }
            ) { credential ->
                CoroutineScope(Dispatchers.Main).launch {
                    loginViewModel.onSignInWithGoogle(credential)
                }
            }
            /*
            AuthenticationButton(
                buttonText = R.string.loginConGoogle,
                modifier = Modifier.fillMaxWidth(0.8f)
            ) { credential ->
                val bundle = (credential as? CustomCredential)?.data ?: return@AuthenticationButton
                val idToken = bundle.getString("google.idToken") ?: return@AuthenticationButton

                loginViewModel.launchCatching {
                    loginViewModel.signInWithGoogle(idToken)
                }
            }
             */
        }
    }
}

/*
@Composable
fun LoginScreen(
    navController: NavHostController,
    loginViewModel: LoginViewModel
) {
    val scope = rememberCoroutineScope() // CoroutineScope para llamadas suspend
    val context = LocalContext.current

    // Observamos estado de login
    val logeado: Boolean by loginViewModel.logeado.observeAsState(initial = false)
    if (logeado) {
        LaunchedEffect(logeado) {
            loginViewModel.navegar {
                navController.navigate(Routes.Main)
            }
        }
    }

    val email = loginViewModel.email.collectAsState()
    val password = loginViewModel.password.collectAsState()
    val error: String by loginViewModel.error.observeAsState(initial = "")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Login con Google
        LaunchedEffect(Unit) {
            launchCredManBottomSheet(context) { result ->
                scope.launch {
                    loginViewModel.onSignInWithGoogle(result)
                }
            }
        }

        Image(
            painter = painterResource(id = R.drawable.logo_inverso),
            contentDescription = "Auth image",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 4.dp)
        )

        OutlinedTextField(
            value = email.value,
            onValueChange = { loginViewModel.updateEmail(it) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(16.dp, 4.dp)
                .border(BorderStroke(2.dp, BlueRibbon), RoundedCornerShape(50)),
            placeholder = { Text(stringResource(R.string.email)) },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = stringResource(R.string.email)) }
        )

        OutlinedTextField(
            value = password.value,
            onValueChange = { loginViewModel.updatePassword(it) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(16.dp, 4.dp)
                .border(BorderStroke(2.dp, BlueRibbon), RoundedCornerShape(50)),
            placeholder = { Text(stringResource(R.string.password)) },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = stringResource(R.string.password)) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (error.isNotEmpty()) {
            Text(
                text = if (error == "The supplied auth credential is incorrect, malformed or has expired.")
                    stringResource(R.string.errorCredenciales)
                else error,
                color = Color.Red
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { loginViewModel.onSignInClick() },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(16.dp, 0.dp)
        ) {
            Text(stringResource(R.string.sign_in), fontSize = 16.sp, modifier = Modifier.padding(vertical = 6.dp))
        }

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = { navController.navigate(Routes.Registro) },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(16.dp, 0.dp)
        ) {
            Text(stringResource(R.string.sign_up_description), fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(4.dp))

        AuthenticationButton(
            modifier = Modifier.fillMaxWidth(0.8f),
            buttonText = R.string.loginConGoogle
        ) { credential ->
            scope.launch {
                loginViewModel.onSignInWithGoogle(credential)
            }
        }
    }
}
*/

/*
@Composable
fun LoginScreen(
    navController: NavHostController,
    loginViewModel: LoginViewModel
) {
    //Si está logeado envía a página Main
    val logeado: Boolean by loginViewModel.logeado.observeAsState(initial = false)
    if(logeado) {
        LaunchedEffect(key1 = true) { loginViewModel.navegar({ navController.navigate(Routes.Main) }) }
    }

    val email = loginViewModel.email.collectAsState()
    val password = loginViewModel.password.collectAsState()
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
        val context = LocalContext.current
        //Muestra el "login con google"
        LaunchedEffect(Unit) {
            launchCredManBottomSheet(context) { result ->
                //Lanzamos una coroutine para poder llamar a funciones suspend
                launch {
                    loginViewModel.onSignInWithGoogle(result)
                }
            }
        }

        Image(
            //painter = painterResource(id = R.drawable.logo),
            painter = painterResource(id = R.drawable.logo_inverso),
            contentDescription = "Auth image",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 4.dp)
        )

        /*
        Spacer(modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp))
         */

        OutlinedTextField(
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth(0.8F)
                .padding(16.dp, 4.dp)
                .border(
                    BorderStroke(width = 2.dp, color = BlueRibbon),
                    shape = RoundedCornerShape(50)
                ),
            /*
            colors = TextFieldDefaults.colors(
                containerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
             */
            value = email.value,
            onValueChange = { loginViewModel.updateEmail(it) },
            placeholder = { Text(stringResource(R.string.email)) },
            leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = stringResource(R.string.email)) }
        )

        OutlinedTextField(
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth(0.8F)
                .padding(16.dp, 4.dp)
                .border(
                    BorderStroke(width = 2.dp, color = BlueRibbon),
                    shape = RoundedCornerShape(50)
                ),
            /*
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
             */
            value = password.value,
            onValueChange = { loginViewModel.updatePassword(it) },
            placeholder = { Text(stringResource(R.string.password)) },
            leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = stringResource(R.string.password)) },
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        )

        //Muestra texto con error al iniciar sesión de forma incorrecta
        if(error != "") {
            if(error == "The supplied auth credential is incorrect, malformed or has expired.")
                Text(stringResource(R.string.errorCredenciales))
            else
                Text(error)
        }

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        )

        Button(
            //Si hacemos click en "Login", después de hacer login navega a la página MainScreen
            onClick = { loginViewModel.onSignInClick() },
            modifier = Modifier
                .fillMaxWidth(0.8F)
                .padding(16.dp, 0.dp)
        ) {
            Text(
                text = stringResource(R.string.sign_in),
                fontSize = 16.sp,
                modifier = Modifier.padding(0.dp, 6.dp)
            )
        }
        
        Spacer(modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp))

        //Si hacemos click en "Registrar" navega a la página registrar usuario
        Button(
            onClick = { navController.navigate(Routes.Registro) },
            modifier = Modifier
                .fillMaxWidth(0.8F)
                .padding(16.dp, 0.dp)
        ) {
            Text(text = stringResource(R.string.sign_up_description), fontSize = 16.sp)
        }

        Spacer(modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp))

        AuthenticationButton(
            modifier = Modifier.fillMaxWidth(0.8F),
            buttonText = R.string.loginConGoogle,
            //onRequestResult = { loginViewModel.setLogeado(true) }
        ) { credential ->
            CoroutineScope(Dispatchers.Main).launch {
                loginViewModel.onSignInWithGoogle(credential)
            }
        }
    }
}
 */
