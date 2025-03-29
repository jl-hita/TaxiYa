package com.jlhipe.taxiya.ui.screens

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.jlhipe.taxiya.ui.theme.screens.layout.PlainLayout

@Composable
//fun LoginScreen(navController: NavHostController, rutaViewModel: RutaViewModel) {
fun LoginScreen(navController: NavHostController) {
    PlainLayout(
        navController
    ) {
        /*
        val googleSignInLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if(result.resultCode == Activity.RESULT_OK) {
                //val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                //val task = GoogleSi
                try {
                    val account = task.GetResult(ApiException::class.java)
                    if(account != null) {
                        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {
                            if(it.isSuccessful){
                                //Autenticación correcta
                                Log.i("LOGIN Google", "Correcto")
                            } else {
                                //Autenticación erronea
                                Log.i("LOGIN Google", "Incorrecto")
                            }
                        }
                    }
                } catch (apiExcepction: ApiException) {
                    //Error durante la autenticación
                    Log.i("LOGIN Google", "Excepción")
                }
            }
        }gs
         */
    }
}