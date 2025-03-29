package com.jlhipe.taxiya.ui.theme.screens.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun PlainLayout(
    navController: NavHostController,
    //rutaViewModel: RutaViewModel,
    content: @Composable () -> Unit         // Se le pasa el contenido principal mediante parámetro
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        content()
    }
}