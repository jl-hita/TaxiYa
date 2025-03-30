package com.jlhipe.taxiya.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.jlhipe.taxiya.R
import com.jlhipe.taxiya.navigation.Routes
import com.jlhipe.taxiya.ui.screens.login.LoginViewModel
import com.jlhipe.taxiya.ui.theme.screens.layout.AppScaffold
import com.jlhipe.taxiya.ui.theme.screens.layout.AppTopBar
import kotlinx.coroutines.delay

@Composable
fun MainScreen(
    navController: NavController,
    loginViewModel: LoginViewModel
) {
    AppScaffold (
        showBackArrow = false,
        onBlackArrowClick = { },
        //bottomContent = { BottomBar(modifier = Modifier.padding(vertical = 4.dp), bookViewModel = bookViewModel) }
    ) {
        //Si no está logeado envía a página Login
        //TODO solucionarlo con un estado boolean que se modifice al hacer login/out/etc
        //if(!loginViewModel.hasUser()) navController.navigate(Routes.Login)
        //var logeado by rememberSaveable { mutableStateOf(loginViewModel.logeado.value) }
        //val logeado = loginViewModel.logeado.collectAsState()
        //if(logeado) navController.navigate(Routes.Login)
        //val logeado = loginViewModel.logeado.collectAsState()
        //var logeado by rememberSaveable { mutableStateOf(loginViewModel.logeado.value) }
        val logeado: Boolean by loginViewModel.logeado.observeAsState(initial = true)
        //if(!logeado) navController.navigate(Routes.Login)
        if(!logeado) loginViewModel.navegar({ navController.navigate(Routes.Login) })

        Text("currentUserId: " + loginViewModel.currentUserId)
        Text("currentUserName: " + loginViewModel.currentUserName)
        Text("currentUserEmail: " + loginViewModel.currentUserEmail)

        //TODO Pasar botón de logOut a un menú en el AppTopBar
        Button(
            //Boton de logout
            onClick = { loginViewModel.onLogOutClick() },
            modifier = Modifier
                .fillMaxWidth(0.5F)
                .padding(16.dp, 0.dp)
        ) {
            Text(
                text = stringResource(R.string.logOut),
                fontSize = 16.sp,
                modifier = Modifier.padding(0.dp, 6.dp)
            )
        }

        Spacer(modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp))

        //TODO Pasar botón de logOut a un menú en el AppTopBar
        Button(
            //Boton de borrar cuenta
            onClick = { loginViewModel.onDeleteAccountClick() },
            modifier = Modifier
                .fillMaxWidth(0.5F)
                .padding(16.dp, 0.dp)
        ) {
            Text(
                text = stringResource(R.string.borrarCuenta),
                fontSize = 16.sp,
                modifier = Modifier.padding(0.dp, 6.dp)
            )
        }
    }
}