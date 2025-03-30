package com.jlhipe.taxiya.ui.screens.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.jlhipe.taxiya.R
import com.jlhipe.taxiya.model.service.LoginService
import com.jlhipe.taxiya.navigation.Routes
import com.jlhipe.taxiya.ui.screens.login.LoginViewModel
import kotlinx.coroutines.delay

@Composable
//fun SplashScreen(navController: NavHostController, rutaViewModel: RutaViewModel) {
fun SplashScreen(
    navController: NavHostController,
    loginViewModel: LoginViewModel,
) {
    Splash()

    var loginService = LoginService()

    LaunchedEffect(key1 = true) {
        delay(5000)
        navController.popBackStack() // Avoid going back to Splash Screen
        if(loginViewModel.hasUser()) navController.navigate(Routes.Main) //Si está logeado va a la página principal
        else navController.navigate(Routes.Login) //Si no está logeado se le redirige a la pantalla de login
        //navController.navigate(Routes.Login)   //Ruta a la que navegar tras la SplashScreen
    }
}

@Composable
fun Splash() {
    var animateAlpha by rememberSaveable { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if(animateAlpha) 1f else 0f,
        animationSpec = tween(
            durationMillis = 3000
        ),
        label = "alpha animation"
    )
    var greetingVisible by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(key1 = true) {
        animateAlpha = true
        delay(2000)
        greetingVisible = true
    }

    Column(
        modifier = Modifier.fillMaxSize()
            .background(color = Color(4, 174,236)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        /*
        Text(
            "Task Manager",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )
        */

        Image(
            painter = painterResource(id= R.drawable.logo),
            contentDescription = "TaxiYa",
            modifier = Modifier
                .size(250.dp, 166.dp)
                //.size(200.dp, 133.dp)
                .alpha(alpha)
                /*
                .clip(CircleShape)
                .border(
                    width = 10.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
                 */
        )
        /*
        AnimatedVisibility(visible = greetingVisible) {
            Text(
                text ="By Rick Sanchez",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
         */
    }
}