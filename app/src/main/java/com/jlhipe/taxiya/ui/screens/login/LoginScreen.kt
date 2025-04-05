package com.jlhipe.taxiya.ui.screens.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
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
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.credentials.GetCredentialRequest
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.jlhipe.taxiya.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.jlhipe.taxiya.navigation.Routes
import com.jlhipe.taxiya.ui.theme.BlueRibbon
import com.jlhipe.taxiya.ui.theme.PaleFrost
import kotlinx.coroutines.delay

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
                loginViewModel.onSignInWithGoogle(result)
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
            loginViewModel.onSignInWithGoogle(credential)
        }
    }
}