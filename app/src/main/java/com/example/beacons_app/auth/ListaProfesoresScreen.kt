package com.example.beacons_app.ui.personal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.beacons_app.FbViewModel
import com.example.beacons_app.models.Usuario

@Composable
fun ListaProfesoresScreen(navController: NavController, vm: FbViewModel) {
    val profesores = remember { mutableStateListOf<Usuario>() }

    // Cargar docentes
    LaunchedEffect(Unit) {
        vm.getDocentes { lista ->
            profesores.clear()
            profesores.addAll(lista)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "CARGAR HORARIOS",
            fontSize = 20.sp,
            color = Color(0xFF00695C), // Verde azulado
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Selecciona el docente",
            fontSize = 14.sp,
            color = Color(0xFF00897B)
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(profesores) { docente ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFFAB47BC), Color(0xFF7B1FA2))
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp)
                ) {
                    // Mostrar inicial + apellidos
                    val inicialNombre = if (docente.u_nombres.isNotEmpty()) {
                        docente.u_nombres.first() + "."
                    } else {
                        ""
                    }
                    val textoNombre = "$inicialNombre ${docente.u_apellidos}"

                    Text(
                        text = textoNombre,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Botón CARGAR (verde)
                        Button(
                            onClick = {
                                navController.navigate("cargar_horario/${docente.usuario_id}")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cargar", color = Color.White, fontSize = 12.sp)
                        }

                        // Botón EDITAR (ámbar)
                        Button(
                            onClick = {
                                // TODO: Implementar lógica de editar
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA000)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Editar", color = Color.White, fontSize = 12.sp)
                        }

                        // Botón BAJAR (rojo)
                        Button(
                            onClick = {
                                // TODO: Implementar lógica de eliminar
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Bajar", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.popBackStack() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81D4FA)),
            modifier = Modifier
                .width(120.dp)
                .height(40.dp)
        ) {
            Text("Volver", color = Color.Black)
        }
    }
}
