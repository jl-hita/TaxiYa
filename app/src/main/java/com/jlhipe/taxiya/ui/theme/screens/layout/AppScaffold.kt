package com.jlhipe.taxiya.ui.theme.screens.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AppScaffold(
    showBackArrow: Boolean = false,         // Sirve para indicar si se mostrará o no la flecha atrás
    onBlackArrowClick: () -> Unit = {},     // Se le pasa la acción de la flecha mediante parámetro
    bottomContent: @Composable () -> Unit,  // Se le pasa el contenido del campo inferior mediante parámetro
    content: @Composable () -> Unit         // Se le pasa el contenido principal mediante parámetro
) {
    Scaffold(
        topBar = {
            AppTopBar(
                showBackArrow = showBackArrow,
                onClickBlackArrow = onBlackArrowClick,
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues)
        ) {
            Box(
                modifier = Modifier.weight(9f).fillMaxWidth()
            ) {
                content()
            }
            HorizontalDivider(
                modifier = Modifier.background(MaterialTheme.colorScheme.onPrimary).height(2.dp)
            )
            bottomContent()
        }
    }
}