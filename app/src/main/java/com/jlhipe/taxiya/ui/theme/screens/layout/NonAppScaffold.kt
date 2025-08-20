package com.jlhipe.taxiya.ui.theme.screens.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.jlhipe.taxiya.navigation.Routes

@Composable
fun NonAppScaffold(
    navController: NavController,
    showBack: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier,// = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        //color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                //.padding(16.dp)
        ) {
            Spacer(Modifier.height(20.dp))

            CabezalAlt(
                showBack = showBack,
                onBackClick = {
                    //Si le pasamos un parámetro lo usamos
                    if (onBackClick != null) {
                        onBackClick()
                    } else {
                        //comportamiento por defecto
                        navController.navigate(Routes.Main)
                    }
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize(),
                    //.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                //verticalArrangement = Arrangement.Center,
                content = content
            )
        }
    }
}