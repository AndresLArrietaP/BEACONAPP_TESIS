package com.example.beacons_app.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.beacons_app.DestinationScreen
import com.example.beacons_app.FbViewModel
import com.example.beacons_app.R
import com.example.beacons_app.models.Usuario

@Composable
fun LobbyPersonalScreen(
    navController: NavController,
    usuario: Usuario,
    fbViewModel: FbViewModel
) {
    val context = LocalContext.current
    Image(
        painter = painterResource(id = R.drawable.bl),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = Modifier.fillMaxSize()
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "BIENVENIDO",
            color = Color(0xFFEFF3F5),
            fontSize = 30.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${usuario.u_nombres} ${usuario.u_apellidos}",
            color = Color.LightGray,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Botón Cargar Horarios
        CustomButtonField("Cargar Horarios") {
            navController.navigate(DestinationScreen.ListaProfesores.route)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón Descargar reportes (por ahora no hace nada)
        CustomButtonField("Descargar reportes") {
            // Acción futura
            println("Descargar reportes (no implementado)")
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Botón Cerrar sesión
        CustomButtonField("Cerrar sesión") {
            fbViewModel.logout(context)
            navController.navigate(DestinationScreen.Login.route) {
                popUpTo(DestinationScreen.Lobby.route) { inclusive = true }
            }
        }
    }
}

@Composable
fun CustomButtonField(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(color = Color.White)
    ) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(Color.Transparent),
            modifier = Modifier
                .width(200.dp)
                .padding(10.dp)
        ) {
            Text(
                text = text,
                color = Color.Black,
                fontSize = 16.sp
            )
        }
    }
}
