package com.example.beacons_app.ui.personal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.beacons_app.FbViewModel
import com.example.beacons_app.models.Horario

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CargarHorarioScreen(navController: NavController, idUsuario: Int, vm: FbViewModel) {
    var modalidad by remember { mutableStateOf("") }
    val modalidades = listOf("Presencial", "Virtual")
    var modalidadExpanded by remember { mutableStateOf(false) }

    var cursoSeleccionado by remember { mutableStateOf<Pair<Int, String>?>(null) }
    var cursoExpanded by remember { mutableStateOf(false) }
    var filtroCurso by remember { mutableStateOf("") }
    var cursosDisponibles by remember { mutableStateOf<List<Pair<Int, String>>>(emptyList()) }

    var diaSeleccionado by remember { mutableStateOf("") }
    var diasDisponibles by remember { mutableStateOf<List<String>>(emptyList()) }
    var diaExpanded by remember { mutableStateOf(false) }
    var mapaDisponibilidad by remember { mutableStateOf<Map<String, List<Int>>>(emptyMap()) }

    val todasHoras = listOf(
        1 to "8:00 - 10:00", 2 to "10:00 - 12:00", 3 to "12:00 - 14:00",
        4 to "14:00 - 16:00", 5 to "16:00 - 18:00", 6 to "18:00 - 20:00", 7 to "20:00 - 22:00"
    )
    var horasDisponibles by remember { mutableStateOf(todasHoras) }
    var horasSeleccionadas by remember { mutableStateOf<List<Int>>(emptyList()) }
    var mapaDiasHoras by remember { mutableStateOf<Map<String, List<Int>>>(emptyMap()) }

    var aulaSeleccionada by remember { mutableStateOf("") }
    var aulasDisponibles by remember { mutableStateOf<List<String>>(emptyList()) }
    var aulaExpanded by remember { mutableStateOf(false) }

    fun resetearCampos() {
        cursoSeleccionado = null
        filtroCurso = ""
        diaSeleccionado = ""
        horasSeleccionadas = emptyList()
        aulaSeleccionada = ""
        cursosDisponibles = emptyList()
        diasDisponibles = emptyList()
        horasDisponibles = todasHoras
        mapaDiasHoras = emptyMap()
    }

    LaunchedEffect(modalidad) {
        if (modalidad.isNotEmpty()) {
            vm.obtenerCursosDisponiblesFiltrados(idUsuario) {
                cursosDisponibles = it
            }
        }
    }

    // LaunchedEffect para obtener disponibilidad cuando cambia el curso
    LaunchedEffect(cursoSeleccionado) {
        val cursoId = cursoSeleccionado?.first?.toLong() ?: return@LaunchedEffect
        vm.obtenerDiasYHorasDisponibles(idUsuario, cursoId) { mapa ->
            mapaDisponibilidad = mapa
            diasDisponibles = mapa.keys.toList()
            // Si ya había un día seleccionado, actualiza las horas inmediatamente
            if (diaSeleccionado.isNotEmpty()) {
                horasDisponibles = todasHoras.filter { hora ->
                    mapa[diaSeleccionado]?.contains(hora.first) == true
                }
            }
        }
    }


    // Nuevo LaunchedEffect para actualizar horas disponibles cuando se cambia el día
    LaunchedEffect(diaSeleccionado) {
        if (diaSeleccionado.isNotEmpty()) {
            horasDisponibles = todasHoras.filter { hora ->
                mapaDisponibilidad[diaSeleccionado]?.contains(hora.first) == true
            }
            horasSeleccionadas = emptyList() // Reinicia horas seleccionadas si cambia el día
        }
    }

    // LaunchedEffect para aulas según horas seleccionadas
    LaunchedEffect(modalidad, diaSeleccionado, horasSeleccionadas) {
        if (modalidad == "Presencial" && diaSeleccionado.isNotEmpty() && horasSeleccionadas.isNotEmpty()) {
            vm.obtenerAulasDisponibles(diaSeleccionado, horasSeleccionadas.map { it.toLong() }) {
                aulasDisponibles = it
            }
        } else {
            aulasDisponibles = emptyList()
            aulaSeleccionada = ""
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("CARGAR HORARIO", fontSize = 20.sp, color = Color(0xFF1A5276))
        Spacer(modifier = Modifier.height(16.dp))

        Box {
            OutlinedTextField(
                value = modalidad,
                onValueChange = {},
                readOnly = true,
                label = { Text("Modalidad") },
                modifier = Modifier.fillMaxWidth().clickable { modalidadExpanded = true },
                trailingIcon = {
                    IconButton(onClick = { modalidadExpanded = !modalidadExpanded }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Expandir")
                    }
                }
            )
            DropdownMenu(expanded = modalidadExpanded, onDismissRequest = { modalidadExpanded = false }) {
                modalidades.forEach { tipo ->
                    DropdownMenuItem(
                        text = { Text(tipo) },
                        onClick = {
                            modalidad = tipo
                            modalidadExpanded = false
                            resetearCampos()
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = filtroCurso,
                onValueChange = {
                    if (modalidad.isNotEmpty()) {
                        filtroCurso = it
                        cursoExpanded = true
                    }
                },
                label = { Text("Curso") },
                enabled = modalidad.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().clickable(enabled = modalidad.isNotEmpty()) { cursoExpanded = true },
                trailingIcon = {
                    IconButton(onClick = { cursoExpanded = !cursoExpanded }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }
            )
            DropdownMenu(
                expanded = cursoExpanded,
                onDismissRequest = { cursoExpanded = false }
            ) {
                cursosDisponibles.filter { it.second.contains(filtroCurso, true) }.forEach { (id, nombre) ->
                    DropdownMenuItem(
                        text = { Text(nombre) },
                        onClick = {
                            cursoSeleccionado = id to nombre
                            filtroCurso = nombre
                            cursoExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box {
            OutlinedTextField(
                value = diaSeleccionado,
                onValueChange = {},
                readOnly = true,
                label = { Text("Día") },
                enabled = cursoSeleccionado != null,
                modifier = Modifier.fillMaxWidth().clickable(enabled = cursoSeleccionado != null) { diaExpanded = true },
                trailingIcon = {
                    IconButton(onClick = { diaExpanded = !diaExpanded }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }
            )
            DropdownMenu(expanded = diaExpanded, onDismissRequest = { diaExpanded = false }) {
                diasDisponibles.forEach { dia ->
                    DropdownMenuItem(text = { Text(dia) }, onClick = {
                        diaSeleccionado = dia
                        diaExpanded = false
                    })
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("Horas")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(horasDisponibles) { (id, label) ->
                val selected = horasSeleccionadas.contains(id)
                Button(
                    onClick = {
                        horasSeleccionadas = if (selected) horasSeleccionadas - id else horasSeleccionadas + id
                    },
                    enabled = diaSeleccionado.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selected) Color(0xFF58D68D) else Color.LightGray
                    )
                ) {
                    Text(label)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (modalidad == "Presencial") {
            Box {
                OutlinedTextField(
                    value = aulaSeleccionada,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Aula") },
                    enabled = horasSeleccionadas.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().clickable(enabled = horasSeleccionadas.isNotEmpty()) {
                        aulaExpanded = true
                    },
                    trailingIcon = {
                        IconButton(onClick = { aulaExpanded = !aulaExpanded }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                )
                DropdownMenu(expanded = aulaExpanded, onDismissRequest = { aulaExpanded = false }) {
                    aulasDisponibles.forEach { aula ->
                        DropdownMenuItem(text = { Text(aula) }, onClick = {
                            aulaSeleccionada = aula
                            aulaExpanded = false
                        })
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(

            onClick = {

                val curso = cursoSeleccionado ?: return@Button
                val horario = Horario(
                    id_horario = 0L,
                    id_curso = curso.first.toLong(),
                    id_modalidad = if (modalidad == "Presencial") 2L else 1L,
                    dia = diaSeleccionado,
                    horas = horasSeleccionadas.map { it.toLong() },
                    aula = if (modalidad == "Presencial") aulaSeleccionada else "",
                    grupo = 0L
                )
                println("Guardando horario: $horario")
                vm.guardarHorarioCompleto(idUsuario, horario) { success, msg ->
                    if (success) {
                        modalidad = ""
                        resetearCampos()
                    }
                    println(msg)
                }
            },
            enabled = modalidad.isNotEmpty() && cursoSeleccionado != null && diaSeleccionado.isNotEmpty() && horasSeleccionadas.isNotEmpty() && (modalidad == "Virtual" || aulaSeleccionada.isNotEmpty()),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ECC71))
        ) {
            Text("Guardar", color = Color.White)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { navController.popBackStack() }, colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)) {
            Text("Volver")
        }
    }
}


/*package com.example.beacons_app.ui.personal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
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
import com.example.beacons_app.models.Curso
import com.example.beacons_app.models.Horario
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CargarHorarioScreen(navController: NavController, idUsuario: Int, vm: FbViewModel) {
    val scope = rememberCoroutineScope()

    var modalidad by remember { mutableStateOf("") }
    val modalidades = listOf("Presencial", "Virtual")
    var modalidadExpanded by remember { mutableStateOf(false) }

    var cursoSeleccionado by remember { mutableStateOf<Pair<Int, String>?>(null) }
    var cursoExpanded by remember { mutableStateOf(false) }
    var filtroCurso by remember { mutableStateOf("") }
    var cursosDisponibles by remember { mutableStateOf<List<Pair<Int, String>>>(emptyList()) }

    var diaSeleccionado by remember { mutableStateOf("") }
    val dias = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")
    var diaExpanded by remember { mutableStateOf(false) }

    val horasDisponibles = listOf(
        1 to "8:00 - 10:00",
        2 to "10:00 - 12:00",
        3 to "12:00 - 14:00",
        4 to "14:00 - 16:00",
        5 to "16:00 - 18:00",
        6 to "18:00 - 20:00",
        7 to "20:00 - 22:00"
    )
    var horasSeleccionadas by remember { mutableStateOf<List<Int>>(emptyList()) }

    var aulaSeleccionada by remember { mutableStateOf("") }
    var aulasDisponibles by remember { mutableStateOf<List<String>>(emptyList()) }
    var aulaExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        vm.obtenerCursosDisponibles { cursosDisponibles = it }
    }

    LaunchedEffect(modalidad, diaSeleccionado, horasSeleccionadas) {
        if (modalidad == "Presencial" && diaSeleccionado.isNotEmpty() && horasSeleccionadas.isNotEmpty()) {
            vm.obtenerAulasDisponibles(diaSeleccionado, horasSeleccionadas.map { it.toLong() }) {
                aulasDisponibles = it
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Text("CARGAR HORARIO", fontSize = 20.sp, color = Color(0xFF1A5276))
        Spacer(modifier = Modifier.height(16.dp))

        // Modalidad
        Box {
            OutlinedTextField(
                value = modalidad,
                onValueChange = {},
                readOnly = true,
                label = { Text("Modalidad") },
                placeholder = { Text("*Selecciona la modalidad*") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { modalidadExpanded = true },
                trailingIcon = {
                    IconButton(onClick = { modalidadExpanded = !modalidadExpanded }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Expandir modalidad")
                    }
                }
            )
            DropdownMenu(expanded = modalidadExpanded, onDismissRequest = { modalidadExpanded = false }) {
                modalidades.forEach { tipo ->
                    DropdownMenuItem(
                        text = { Text(tipo) },
                        onClick = {
                            modalidad = tipo
                            modalidadExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Curso
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = filtroCurso,
                onValueChange = {
                    if (modalidad.isNotEmpty()) {
                        filtroCurso = it
                        cursoExpanded = true
                    }
                },
                label = { Text("Curso") },
                enabled = modalidad.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = modalidad.isNotEmpty()) { cursoExpanded = true },
                trailingIcon = {
                    IconButton(onClick = { cursoExpanded = !cursoExpanded }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Expandir")
                    }
                }
            )
            DropdownMenu(
                expanded = cursoExpanded,
                onDismissRequest = { cursoExpanded = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp)
            ) {
                cursosDisponibles
                    .filter { it.second.contains(filtroCurso, ignoreCase = true) }
                    .forEach { (id, nombre) ->
                        DropdownMenuItem(
                            text = { Text(nombre) },
                            onClick = {
                                cursoSeleccionado = id to nombre
                                filtroCurso = nombre
                                cursoExpanded = false
                            }
                        )
                    }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Día
        Box {
            OutlinedTextField(
                value = diaSeleccionado,
                onValueChange = {},
                readOnly = true,
                label = { Text("Día") },
                enabled = cursoSeleccionado != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = cursoSeleccionado != null) { diaExpanded = true },
                trailingIcon = {
                    IconButton(onClick = { diaExpanded = !diaExpanded }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Expandir día")
                    }
                }
            )
            DropdownMenu(expanded = diaExpanded, onDismissRequest = { diaExpanded = false }) {
                dias.forEach { dia ->
                    DropdownMenuItem(text = { Text(dia) }, onClick = {
                        diaSeleccionado = dia
                        diaExpanded = false
                    })
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Horas
        Text("Horas", color = if (diaSeleccionado.isNotEmpty()) Color.Unspecified else Color.Gray)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(horasDisponibles) { (id, label) ->
                val selected = horasSeleccionadas.contains(id)
                Button(
                    onClick = {
                        if (diaSeleccionado.isNotEmpty()) {
                            horasSeleccionadas = if (selected) horasSeleccionadas - id else horasSeleccionadas + id
                        }
                    },
                    enabled = diaSeleccionado.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selected) Color(0xFF58D68D) else Color.LightGray
                    )
                ) {
                    Text(label)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Aula
        if (modalidad == "Presencial") {
            Box {
                OutlinedTextField(
                    value = aulaSeleccionada,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Aula") },
                    enabled = horasSeleccionadas.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = horasSeleccionadas.isNotEmpty()) { aulaExpanded = true },
                    trailingIcon = {
                        IconButton(onClick = { aulaExpanded = !aulaExpanded }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Expandir aula")
                        }
                    }
                )
                DropdownMenu(
                    expanded = aulaExpanded,
                    onDismissRequest = { aulaExpanded = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                ) {
                    aulasDisponibles.forEach { aula ->
                        DropdownMenuItem(
                            text = { Text(aula) },
                            onClick = {
                                aulaSeleccionada = aula
                                aulaExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))

        // Guardar
        Button(
            onClick = {
                val curso = cursoSeleccionado ?: return@Button
                if (modalidad.isNotEmpty() && diaSeleccionado.isNotEmpty() && horasSeleccionadas.isNotEmpty()) {
                    val horario = Horario(
                        id_horario = "",
                        id_curso = curso.first.toLong(),
                        id_modalidad = if (modalidad == "Presencial") 2L else 1L,
                        dia = diaSeleccionado,
                        horas = horasSeleccionadas.map { it.toLong() },
                        aula = if (modalidad == "Presencial") aulaSeleccionada else "",
                        grupo = 0L
                    )
                    vm.guardarHorarioCompleto(idUsuario, horario) { success, msg ->
                        println(msg)
                        if (success) {
                            modalidad = ""
                            cursoSeleccionado = null
                            filtroCurso = ""
                            diaSeleccionado = ""
                            horasSeleccionadas = emptyList()
                            aulaSeleccionada = ""
                            navController.popBackStack()
                        }
                    }
                }
            },
            enabled = modalidad.isNotEmpty() &&
                    cursoSeleccionado != null &&
                    diaSeleccionado.isNotEmpty() &&
                    horasSeleccionadas.isNotEmpty() &&
                    (modalidad == "Virtual" || aulaSeleccionada.isNotEmpty()),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ECC71))
        ) {
            Text("Guardar", color = Color.White)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { navController.popBackStack() },
            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
        ) {
            Text("Volver")
        }
    }
}*/

