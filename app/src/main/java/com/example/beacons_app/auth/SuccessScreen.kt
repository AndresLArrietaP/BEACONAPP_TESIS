package com.example.beacons_app.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.beacons_app.DestinationScreen
import com.example.beacons_app.R
import com.example.beacons_app.models.AsistenciaUsuario
import kotlinx.coroutines.delay

@Composable
fun SuccessScreen(navController: NavController, asistenciaUsuario: AsistenciaUsuario?) {
    LaunchedEffect(Unit) {
        delay(7000)
        navController.popBackStack(DestinationScreen.Lobby.route, false)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Imagen superior (logo o éxito)
        Image(
            painter = painterResource(id = R.drawable.ck),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Título principal
        Text(
            text = "Asistencia registrada",
            color = Color(0xFF4CAF50), // verde éxito
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        asistenciaUsuario?.let {
            Text(
                text = "Prof. ${it.usuario.u_apellidos}",
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "📅 ${it.fecha_registro}",
                fontSize = 18.sp,
                color = Color.Gray
            )
            Text(
                text = "🕒 ${it.hora_registro}",
                fontSize = 18.sp,
                color = Color.Gray
            )
            Text(
                text = "🏫 Modalidad: Presencial",
                fontSize = 18.sp,
                color = Color.Gray
            )
        }
    }
}
