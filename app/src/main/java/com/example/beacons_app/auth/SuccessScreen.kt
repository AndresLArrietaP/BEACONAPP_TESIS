package com.example.beacons_app.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.example.beacons_app.FbViewModel
import com.example.beacons_app.R
import com.example.beacons_app.models.AsistenciaUsuario

@Composable
fun SuccessScreen(navController: NavController, asistenciaUsuario: AsistenciaUsuario?) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.lu),
            contentDescription = null
        )

        asistenciaUsuario?.let {
            Text("Asistencia registrada para: ${it.usuario.u_nombres} ${it.usuario.u_apellidos}")
            Text("Fecha: ${it.fecha_registro}")
            Text("Hora: ${it.hora_registro}")
            Text("Modalidad: presencial")
        }
    }
}
