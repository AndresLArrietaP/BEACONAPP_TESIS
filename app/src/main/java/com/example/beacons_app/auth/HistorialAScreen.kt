package com.example.beacons_app.auth

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.beacons_app.R
import com.example.beacons_app.models.AsistenciaUsuario
import com.example.beacons_app.models.Usuario
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistorialAScreen(navController: NavController, usuario: Usuario) {
    val db = Firebase.database.reference
    val asistenciaUsuarioList = remember { mutableStateListOf<AsistenciaUsuario>() }
    var orderDescending by remember { mutableStateOf(true) }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val calendar = Calendar.getInstance()
    val currentDate = calendar.time
    calendar.add(Calendar.DAY_OF_YEAR, -30)
    val limitDate = calendar.time

    LaunchedEffect(usuario.usuario_id) {
        db.child("asistencia_usuario")
            .child(usuario.usuario_id.toString())
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.children.mapNotNull { it.getValue(AsistenciaUsuario::class.java) }
                    .filter {
                        try {
                            val fecha = dateFormat.parse(it.fecha_registro)
                            fecha?.after(limitDate) == true
                        } catch (e: Exception) {
                            false
                        }
                    }
                asistenciaUsuarioList.clear()
                asistenciaUsuarioList.addAll(list)
            }
    }

    Image(
        painter = painterResource(id = R.drawable.gr),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = Modifier.fillMaxSize()
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp)) // Bajamos más el título

        Text(
            text = "HISTORIAL DE ASISTENCIAS",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "para ${usuario.u_nombres} ",
            color = Color.LightGray,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth().padding(end = 16.dp)
        ) {
            Button(
                onClick = { orderDescending = !orderDescending },
                shape = RoundedCornerShape(20),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBBDEFB)),
                modifier = Modifier.height(32.dp)
            ) {
                Text(
                    text = if (orderDescending) "Descendente" else "Ascendente",
                    color = Color.Black,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.White)
                .padding(vertical = 4.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE3F2FD))
                        .padding(vertical = 8.dp, horizontal = 4.dp)
                ) {
                    Text(
                        "Fecha",
                        modifier = Modifier.weight(1.2f),
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        "Hora",
                        modifier = Modifier.weight(1.2f),
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        "Curso",
                        modifier = Modifier.weight(1f),
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        "Grupo",
                        modifier = Modifier.weight(0.8f),
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        "Aula",
                        modifier = Modifier.weight(0.8f),
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            val sortedList = if (orderDescending) {
                asistenciaUsuarioList.sortedByDescending { it.fecha_registro }
            } else {
                asistenciaUsuarioList.sortedBy { it.fecha_registro }
            }

            items(sortedList) { asistenciaUsuario ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp, horizontal = 4.dp)
                        .background(Color(0xFFF5F5F5), shape = RoundedCornerShape(6.dp))
                        .padding(vertical = 8.dp, horizontal = 8.dp)
                ) {
                    Text(
                        asistenciaUsuario.fecha_registro,
                        modifier = Modifier.weight(1.2f),
                        color = Color.Black,
                        fontSize = 14.sp
                    )
                    Text(
                        asistenciaUsuario.hora_registro,
                        modifier = Modifier.weight(1.2f),
                        color = Color.Black,
                        fontSize = 14.sp
                    )
                    Text("-", modifier = Modifier.weight(1f), color = Color.Gray, fontSize = 14.sp) // Curso
                    Text("-", modifier = Modifier.weight(0.8f), color = Color.Gray, fontSize = 14.sp) // Grupo
                    Text("-", modifier = Modifier.weight(0.8f), color = Color.Gray, fontSize = 14.sp) // Aula
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(color = Color(0xFFBBDEFB))
        ) {
            Button(
                onClick = {
                    navController.popBackStack()
                },
                colors = ButtonDefaults.buttonColors(Color.Transparent),
                modifier = Modifier
                    .width(115.dp)
                    .padding(5.dp)
            ) {
                Text(
                    text = "Volver",
                    color = Color.Black,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}


