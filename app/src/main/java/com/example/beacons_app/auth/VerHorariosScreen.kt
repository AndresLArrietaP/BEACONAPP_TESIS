package com.example.beacons_app.ui.docente

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.beacons_app.FbViewModel
import com.example.beacons_app.models.Horario
import com.example.beacons_app.models.Usuario

@Composable
fun VerHorariosScreen(
    usuario: Usuario,
    fbViewModel: FbViewModel,
    navController: NavController
) {
    val horarios = remember { mutableStateListOf<Horario>() }
    val coloresCursos = remember { mutableStateMapOf<Long, Color>() }
    val nombresCursos = remember { mutableStateMapOf<Long, String>() }
    val coloresDisponibles = listOf(
        Color(0xFFAED581), Color(0xFF81D4FA), Color(0xFFFFF176),
        Color(0xFFFF8A65), Color(0xFFBA68C8), Color(0xFF4DB6AC), Color(0xFFFFB74D)
    )

    LaunchedEffect(true) {
        fbViewModel.obtenerHorariosPorUsuario(usuario.usuario_id) { lista ->
            horarios.clear()
            horarios.addAll(lista)

            val clavesCursoGrupo = lista.map { it.id_curso * 100 + it.grupo }.distinct()
            clavesCursoGrupo.forEachIndexed { i, clave ->
                coloresCursos[clave] = coloresDisponibles[i % coloresDisponibles.size]
                val idCurso = clave / 100
                fbViewModel.obtenerNombreCursoPorId(idCurso.toLong()) { nombre ->
                    nombresCursos[clave] = nombre
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Horario de ${usuario.u_nombres}",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            TablaHorario(horarios, coloresCursos, nombresCursos)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(8.dp)
        ) {
            Text("Volver")
        }
    }
}

@Composable
fun TablaHorario(
    horarios: List<Horario>,
    coloresCursos: Map<Long, Color>,
    nombresCursos: Map<Long, String>
) {
    val horasText = listOf(
        "8:00-9:00", "9:00-10:00", "10:00-11:00", "11:00-12:00",
        "12:00-13:00", "13:00-14:00", "14:00-15:00", "15:00-16:00",
        "16:00-17:00", "17:00-18:00", "18:00-19:00", "19:00-20:00",
        "20:00-21:00", "21:00-22:00"
    )
    val dias = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")

    // Mapa día → lista de (inicio, fin, horario)
    val bloques = remember(horarios) {
        dias.associateWith { mutableListOf<Triple<Int, Int, Horario>>() }
            .toMutableMap()
    }
    // Agrupar rangos contiguos
    for (h in horarios) {
        val orden = h.horas.sortedBy { it.toInt() }.map { it.toInt() }
        if (orden.isEmpty()) continue
        var inicio = orden.first()
        var fin = inicio
        for (i in 1 until orden.size) {
            val v = orden[i]
            if (v == fin + 1) fin = v else {
                bloques[h.dia]?.add(Triple(inicio, fin, h))
                inicio = v; fin = v
            }
        }
        bloques[h.dia]?.add(Triple(inicio, fin, h))
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        // Columna de horarios
        Column(modifier = Modifier.width(100.dp)) {
            Spacer(modifier = Modifier.height(40.dp))
            horasText.forEach {
                Box(
                    modifier = Modifier
                        .height(30.dp)
                        .padding(1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(it, fontSize = 11.sp)
                }
            }
        }

        // Columnas por día
        dias.forEach { dia ->
            Column(modifier = Modifier.width(120.dp)) {
                Box(
                    modifier = Modifier.height(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(dia, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                val bloquesDia = bloques[dia] ?: emptyList()
                var fila = 1
                while (fila <= 14) {
                    // Cada dos filas corresponde a un bloque pedagógico
                    val bloque = bloquesDia.find { it.first == ((fila + 1) / 2) }
                    if (bloque != null) {
                        val (ini, fin, curso) = bloque
                        // <número de bloques> * 2 filas por bloque
                        val filasAltura = (fin - ini + 1) * 2
                        val clave = curso.id_curso * 100 + curso.grupo
                        val color = coloresCursos[clave] ?: Color.Gray
                        val nombre = nombresCursos[clave] ?: "Curso"

                        Box(
                            modifier = Modifier
                                .height((filasAltura * 30).dp)
                                .fillMaxWidth()
                                .padding(2.dp)
                                .background(color, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "$nombre\nSección ${curso.grupo}",
                                fontSize = 11.sp
                            )
                        }
                        fila += filasAltura
                    } else {
                        Box(
                            modifier = Modifier
                                .height(30.dp)
                                .padding(2.dp)
                        )
                        fila++
                    }
                }
            }
        }
    }
}






/*package com.example.beacons_app.ui.docente

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.beacons_app.FbViewModel
import com.example.beacons_app.models.Horario
import com.example.beacons_app.models.Usuario

@Composable
fun VerHorariosScreen(
    usuario: Usuario,
    fbViewModel: FbViewModel,
    navController: NavController
) {
    val horarios = remember { mutableStateListOf<Horario>() }
    val coloresCursos = remember { mutableStateMapOf<Long, Color>() }
    val nombresCursos = remember { mutableStateMapOf<Long, String>() }
    val coloresDisponibles = listOf(
        Color(0xFFAED581), Color(0xFF81D4FA), Color(0xFFFFF176),
        Color(0xFFFF8A65), Color(0xFFBA68C8), Color(0xFF4DB6AC), Color(0xFFFFB74D)
    )

    LaunchedEffect(true) {
        fbViewModel.obtenerHorariosPorUsuario(usuario.usuario_id) { lista ->
            horarios.clear()
            horarios.addAll(lista)

            val clavesCursoGrupo = lista.map { Pair(it.id_curso, it.grupo) }.distinct()
            clavesCursoGrupo.forEachIndexed { i, (idCurso, grupo) ->
                val clave = idCurso * 100 + grupo
                coloresCursos[clave] = coloresDisponibles[i % coloresDisponibles.size]
                fbViewModel.obtenerNombreCursoPorId(idCurso) { nombre ->
                    nombresCursos[clave] = nombre
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Horario de ${usuario.u_nombres}", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            TablaHorario(
                horarios = horarios,
                coloresCursos = coloresCursos,
                nombresCursos = nombresCursos
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(8.dp)
        ) {
            Text("Volver")
        }
    }
}

@Composable
fun TablaHorario(
    horarios: List<Horario>,
    coloresCursos: Map<Long, Color>,
    nombresCursos: Map<Long, String>
) {
    val horasText = listOf(
        "8:00-9:00", "9:00-10:00", "10:00-11:00", "11:00-12:00",
        "12:00-13:00", "13:00-14:00", "14:00-15:00", "15:00-16:00",
        "16:00-17:00", "17:00-18:00", "18:00-19:00", "19:00-20:00",
        "20:00-21:00", "21:00-22:00"
    )

    val dias = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")

    val bloques = remember(horarios) {
        dias.associateWith { mutableListOf<Triple<Int, Int, Horario>>() }.toMutableMap()
    }

    for (horario in horarios) {
        val dia = horario.dia
        val horas = horario.horas.sorted()
        if (horas.isEmpty()) continue

        var inicio = horas[0].toInt()
        var fin = horas[0].toInt()
        for (i in 1 until horas.size) {
            if (horas[i].toInt() == fin + 1) {
                fin = horas[i].toInt()
            } else {
                bloques[dia]?.add(Triple(inicio, fin, horario))
                inicio = horas[i].toInt()
                fin = horas[i].toInt()
            }
        }
        bloques[dia]?.add(Triple(inicio, fin, horario))
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.width(100.dp)) {
            Spacer(modifier = Modifier.height(40.dp))
            horasText.forEach {
                Box(
                    modifier = Modifier
                        .height(30.dp)
                        .padding(1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = it, fontSize = 11.sp)
                }
            }
        }

        dias.forEach { dia ->
            Column(modifier = Modifier.width(120.dp)) {
                Box(modifier = Modifier.height(40.dp), contentAlignment = Alignment.Center) {
                    Text(dia, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                var fila = 1
                while (fila <= 14) {
                    val bloque = bloques[dia]?.find { it.first == ((fila + 1) / 2) }
                    if (bloque != null) {
                        val bloquesGraficos = (bloque.second - bloque.first + 1) * 2
                        val curso = bloque.third
                        val clave = curso.id_curso * 100 + curso.grupo
                        val color = coloresCursos[clave] ?: Color.Gray
                        val nombreCurso = nombresCursos[clave] ?: "Curso"

                        Box(
                            modifier = Modifier
                                .height((bloquesGraficos * 30).dp)
                                .fillMaxWidth()
                                .padding(2.dp)
                                .background(color, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$nombreCurso\nSección ${curso.grupo}",
                                fontSize = 11.sp,
                                color = Color.Black
                            )
                        }
                        fila += bloquesGraficos
                    } else {
                        Box(
                            modifier = Modifier
                                .height(30.dp)
                                .padding(2.dp)
                        )
                        fila++
                    }
                }
            }
        }
    }
}*/


