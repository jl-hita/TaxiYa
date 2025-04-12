package com.jlhipe.taxiya.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.jlhipe.taxiya.R
import com.jlhipe.taxiya.model.Ruta
import com.jlhipe.taxiya.navigation.Routes
import com.jlhipe.taxiya.ui.screens.login.LoginViewModel
import com.jlhipe.taxiya.ui.theme.screens.layout.AppScaffold

@Composable
fun MainScreen(
    navController: NavController,
    loginViewModel: LoginViewModel,
    rutaViewModel: RutaViewModel
) {
    AppScaffold (
        showBackArrow = false,
        onBlackArrowClick = { },
        //botonAccion = navController.navigate(Routes.NuevaRuta)
        showActionButton = true, //TODO cambiar por un check si usuario es conductor
        botonAccion = {
            navController.navigate(Routes.NuevaRuta);
        },
        //bottomContent = { BottomBar(modifier = Modifier.padding(vertical = 4.dp), bookViewModel = bookViewModel) }
    ) {
        //Si hace logout se envía a página Login
        val logeado: Boolean by loginViewModel.logeado.observeAsState(initial = true)
        if(!logeado) {
            LaunchedEffect(key1 = true) { loginViewModel.navegar({ navController.navigate(Routes.Login) }) }
        }

        /*
        Text("currentUserId: " + loginViewModel.currentUserId)
        Text("currentUserName: " + loginViewModel.currentUserName)
        Text("currentUserEmail: " + loginViewModel.currentUserEmail)
         */
        //Log.d("ID", loginViewModel.currentUserId)


        //TODO if(user.isConductor = false)

        // Suscripción a la lista de rutas del ViewModel
        val rutas: List<Ruta> by rutaViewModel.rutas.observeAsState(initial = emptyList())
        // Suscripción a la variable que indica si se están consiguiendo la lista de libros
        val isLoadingRutas: Boolean by rutaViewModel.isLoading.observeAsState(initial = false)

        val context = LocalContext.current

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                //.background(MaterialTheme.colorScheme.primary)
                //.padding(vertical = 8.dp)
                .padding(8.dp)
                //.weight(7.7f)
        ) {
            item() {
                Row(){ //Header
                    Text(stringResource(R.string.origen), fontSize = 25.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(10f))
                    Text(stringResource(R.string.destino), fontSize = 25.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(10f))
                    Text(stringResource(R.string.fecha), fontSize = 25.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(10f))
                    Text(stringResource(R.string.duracion), fontSize = 25.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(10f))
                    //Text(stringResource(R.string.distancia), fontSize = 25.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(10f))
                }
            }
            items(rutas) { ruta ->
            //items(rutasFirebase) { ruta ->

                if(ruta.visible) {
                    HorizontalDivider(thickness = 2.dp)
                    Row() {
                        Text(rutaViewModel.getNombreCiudad(ruta.origenGeo, context)!!, modifier = Modifier.weight(10f))
                        Text(rutaViewModel.getNombreCiudad(ruta.destinoGeo, context)!!, modifier = Modifier.weight(10f))
                        //Text(rutaViewModel.getFechaCompleta(ruta.momentoSalida), modifier = Modifier.weight(10f))
                        Text(rutaViewModel.getDia(ruta.momentoSalida), modifier = Modifier.weight(10f))
                        Text(rutaViewModel.getDuracionTiempo(ruta.momentoSalida, ruta.momentoLlegada), modifier = Modifier.weight(10f))
                        //Text("" + ruta.distancia + " km", modifier = Modifier.weight(10f))
                    }
                }
            }
        }

        //Mientras carga la lista de rutas se muestra un mensaje y una linea de progreso
        if (isLoadingRutas) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = stringResource(R.string.cargando),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.height(20.dp))
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        //TODO Borrame - botón para insertar una ruta de prueba en firebase
        Button(
            onClick = { rutaViewModel.insertaRutaFirebase(
                //userID = loginViewModel.currentUserId,
                //identificador = loginViewModel.currentUserId,
                cliente = loginViewModel.currentUserId,
                conductor = "1qw6g1r8ge"
            ) },
            modifier = Modifier
                .fillMaxWidth(0.5F)
                .padding(16.dp, 0.dp)
        ) {
            Text(
                text = "Inserta ruta de prueba",
                fontSize = 16.sp,
                modifier = Modifier.padding(0.dp, 6.dp)
            )
        }

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