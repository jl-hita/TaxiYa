package com.jlhipe.taxiya.ui.theme.screens.layout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jlhipe.taxiya.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.res.painterResource

@Composable
fun CabezalAlt(
    showBack: Boolean,
    onBackClick: () -> Unit = {}
) {
    //Spacer(Modifier.height(20.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (showBack) {
            Row(
                modifier = Modifier.clickable { onBackClick() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.volver)
                )
                Text(
                    text = stringResource(R.string.volver),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        } else {
            //Mantiene el espacio para que el logo no se mueva
            Spacer(Modifier.width(48.dp))
        }

        // Logo a la derecha
        Image(
            painter = painterResource(id = R.drawable.logo_inverso_peque),
            contentDescription = "Logo",
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1f)
        )
    }
}