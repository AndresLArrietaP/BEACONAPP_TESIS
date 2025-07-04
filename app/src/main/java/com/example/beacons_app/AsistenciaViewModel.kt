package com.example.beacons_app

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.beacons_app.DataBeacon.GestorAsistenciaReceptor
import com.example.beacons_app.models.Asistencia
import com.example.beacons_app.models.AsistenciaUsuario
import com.example.beacons_app.models.Usuario
import com.example.beacons_app.models.Horario
import com.example.beacons_app.util.Recurso
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

//@HiltViewModel
class AsistenciaViewModel /*@Inject constructor*/(
    private val gestorAsistencia: GestorAsistenciaReceptor,
    private val database: FirebaseDatabase
) : ViewModel() {

    val inProgress = mutableStateOf(false)
    val popupNotification = mutableStateOf<Event<String>?>(null)
    val ultimaAsistencia = mutableStateOf<AsistenciaUsuario?>(null)

    private val _estadoRegistro = MutableStateFlow<Recurso<AsistenciaUsuario>>(Recurso.Idle)
    val estadoRegistro: StateFlow<Recurso<AsistenciaUsuario>> = _estadoRegistro

    fun registrarAsistencia(navController: NavController, usuario: Usuario) {
        viewModelScope.launch {
            Log.d("AsistenciaViewModel", "▶ Iniciando escaneo BLE...")
            gestorAsistencia.startReceiving()

            var yaRegistrado = false

            gestorAsistencia.data.collect { recurso ->
                if (yaRegistrado) return@collect
                yaRegistrado = true

                when (recurso) {
                    is Recurso.Loading -> {
                        inProgress.value = true
                        Log.d("AsistenciaViewModel", "⏳ ${recurso.message}")
                    }

                    is Recurso.Error -> {
                        inProgress.value = false
                        popupNotification.value = Event("Error: ${recurso.errorMessage}")
                        gestorAsistencia.disconnect()
                    }

                    is Recurso.Success -> {
                        inProgress.value = false
                        gestorAsistencia.disconnect()

                        val tz = TimeZone.getTimeZone("America/Lima")
                        val now = Calendar.getInstance(tz)
                        val dia = obtenerDiaActual(now.get(Calendar.DAY_OF_WEEK))
                        val hora = obtenerHoraPedagogicaActual(now)

                        Log.d("AsistenciaViewModel", "⏰ Día detectado: $dia, Hora pedagógica: $hora")

                        val refHorarios = database.getReference("horario_usuario/${usuario.usuario_id}")
                        refHorarios.get().addOnSuccessListener { snapshot ->
                            val horarios = snapshot.children.mapNotNull {
                                val horario = it.child("horario").getValue(Horario::class.java)
                                horario?.let { h -> h to it.key }
                            }

                            val (horarioValido, keyHorario) = horarios.firstOrNull {
                                it.first.dia == dia && it.first.horas.contains(hora.toLong())
                            } ?: Pair(null, null)

                            obtenerNuevoIdAsistencia { nuevoId ->
                                if (nuevoId == null) {
                                    popupNotification.value = Event("⚠️ No se pudo generar ID de asistencia.")
                                    return@obtenerNuevoIdAsistencia
                                }

                                val asistencia = if (horarioValido != null) {
                                    Asistencia(
                                        asistencia_id = nuevoId,
                                        beacons_id = recurso.data.asistencia.beacons_id,
                                        beacon_uuid = recurso.data.asistencia.beacon_uuid,
                                        modalidad_id = horarioValido.id_modalidad.toInt(),
                                        horario_id = horarioValido.id_horario.toInt(),
                                        estado = true,
                                        descripcion = "Asistencia registrada correctamente",
                                        conexion = recurso.data.asistencia.conexion
                                    )
                                } else {
                                    Asistencia(
                                        asistencia_id = nuevoId,
                                        beacons_id = recurso.data.asistencia.beacons_id,
                                        beacon_uuid = recurso.data.asistencia.beacon_uuid,
                                        modalidad_id = 0,
                                        horario_id = 0,
                                        estado = true,
                                        descripcion = "DEBUG: Asistencia fuera de horario",
                                        conexion = recurso.data.asistencia.conexion
                                    )
                                }

                                val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                val formatoHora = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                                val fecha = formatoFecha.format(now.time)
                                val horaTexto = formatoHora.format(now.time)

                                val asistenciaUsuario = AsistenciaUsuario(
                                    usuario = usuario,
                                    asistencia = asistencia,
                                    fecha_registro = fecha,
                                    hora_registro = horaTexto
                                )

                                val pathAU = "asistencia_usuario/${usuario.usuario_id}/$nuevoId"
                                val pathA = "asistencia/$nuevoId"

                                database.getReference(pathAU).setValue(asistenciaUsuario)
                                database.getReference(pathA).setValue(asistenciaUsuario)

                                /*if (horarioValido != null && keyHorario != null) {

                                    val semanaRef = database.getReference("horario_usuario/${usuario.usuario_id}/$keyHorario/semana")
                                    semanaRef.get().addOnSuccessListener { snap ->
                                        val semanaActual = snap.getValue(Int::class.java) ?: 0
                                        semanaRef.setValue(semanaActual + 1)
                                    }

                                }*/
                                /*if (horarioValido != null && keyHorario != null && keyHorario.isNotEmpty()) {
                                    Log.d("AsistenciaViewModel", "🔑 Clave de horario válida detectada: $keyHorario")

                                    val semanaPath = "horario_usuario/${usuario.usuario_id}/$keyHorario/semana"
                                    val semanaRef = database.getReference(semanaPath)

                                    semanaRef.get().addOnSuccessListener { snap ->
                                        val semanaActual = snap.getValue(Int::class.java) ?: 0
                                        Log.d("AsistenciaViewModel", "📅 Semana actual ($semanaPath): $semanaActual")
                                        semanaRef.setValue(semanaActual + 1).addOnSuccessListener {
                                            Log.d("AsistenciaViewModel", "✅ Semana incrementada correctamente a ${semanaActual + 1}")
                                        }.addOnFailureListener {
                                            Log.e("AsistenciaViewModel", "❌ Error al incrementar semana", it)
                                        }
                                    }.addOnFailureListener {
                                        Log.e("AsistenciaViewModel", "❌ Error al obtener semana", it)
                                    }
                                } else {
                                    Log.w("AsistenciaViewModel", "⚠️ No se pudo obtener keyHorario válido.")
                                }*/


                                ultimaAsistencia.value = asistenciaUsuario
                                _estadoRegistro.value = Recurso.Success(asistenciaUsuario)

                                navController.navigate("success")
                                viewModelScope.launch {
                                    delay(6000)
                                    navController.popBackStack("lobby", false)
                                    _estadoRegistro.value = Recurso.Idle
                                }
                            }
                        }.addOnFailureListener {
                            popupNotification.value = Event("⚠️ Error al verificar horario.")
                            Log.e("AsistenciaViewModel", "🔥 Error al leer horario_usuario", it)
                        }
                    }

                    is Recurso.Idle -> {
                        inProgress.value = false
                        Log.d("AsistenciaViewModel", "✅ Asistencia registrada y proceso finalizado.")
                    }
                }
            }
        }
    }

    private fun obtenerNuevoIdAsistencia(onResult: (Int?) -> Unit) {
        val refAsistencias = database.getReference("asistencia")
        refAsistencias.get().addOnSuccessListener { snapshot ->
            val maxId = snapshot.children.mapNotNull {
                it.child("asistencia").child("asistencia_id").getValue(Int::class.java)
            }.maxOrNull() ?: 0
            onResult(maxId + 1)
        }.addOnFailureListener {
            Log.e("AsistenciaViewModel", "❌ Error obteniendo último ID", it)
            onResult(null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        gestorAsistencia.closeConnection()
    }
}

// Funciones utilitarias
fun obtenerDiaActual(dia: Int): String {
    return when (dia) {
        Calendar.MONDAY -> "Lunes"
        Calendar.TUESDAY -> "Martes"
        Calendar.WEDNESDAY -> "Miércoles"
        Calendar.THURSDAY -> "Jueves"
        Calendar.FRIDAY -> "Viernes"
        Calendar.SATURDAY -> "Sábado"
        Calendar.SUNDAY -> "Domingo"
        else -> "Desconocido"
    }
}

fun obtenerHoraPedagogicaActual(calendar: Calendar): Int {
    val hora = calendar.get(Calendar.HOUR_OF_DAY)
    return when (hora) {
        in 8..9 -> 1
        in 10..11 -> 2
        in 12..13 -> 3
        in 14..15 -> 4
        in 16..17 -> 5
        in 18..19 -> 6
        in 20..21 -> 7
        else -> -1
    }
}






/*package com.example.beacons_app

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.beacons_app.DataBeacon.GestorAsistenciaReceptor
import com.example.beacons_app.models.AsistenciaUsuario
import com.example.beacons_app.util.Recurso
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AsistenciaViewModel @Inject constructor(
    private val gestorAsistencia: GestorAsistenciaReceptor
) : ViewModel() {

    val inProgress = mutableStateOf(false)
    val popupNotification = mutableStateOf<Event<String>?>(null)
    val ultimaAsistencia = mutableStateOf<AsistenciaUsuario?>(null)

    private val _estadoRegistro = MutableStateFlow<Recurso<AsistenciaUsuario>>(Recurso.Idle)
    val estadoRegistro: StateFlow<Recurso<AsistenciaUsuario>> = _estadoRegistro

    fun registrarAsistencia(navController: NavController) {
        viewModelScope.launch {
            Log.d("AsistenciaViewModel", "▶ Iniciando escaneo BLE...")
            gestorAsistencia.startReceiving()

            gestorAsistencia.data.collect { recurso ->
                when (recurso) {
                    is Recurso.Loading -> {
                        inProgress.value = true
                        Log.d("AsistenciaViewModel", "⏳ ${recurso.message}")
                    }
                    is Recurso.Error -> {
                        inProgress.value = false
                        popupNotification.value = Event("Error: ${recurso.errorMessage}")
                        gestorAsistencia.disconnect()
                        return@collect
                    }
                    is Recurso.Success -> {
                        inProgress.value = false
                        gestorAsistencia.disconnect()

                        val asistenciaUsuario = recurso.data
                        ultimaAsistencia.value = asistenciaUsuario

                        navController.navigate("success")

                        delay(6000)
                        navController.popBackStack("lobby", false)

                        // Volver al estado Idle para cerrar ciclo correctamente
                        _estadoRegistro.value = Recurso.Idle
                        return@collect
                    }
                    is Recurso.Idle -> {
                        inProgress.value = false
                        Log.d("AsistenciaViewModel", "✅ Asistencia registrada y proceso finalizado.")
                    }
                }
            }
        }
    }




    override fun onCleared() {
        super.onCleared()
        gestorAsistencia.closeConnection()
    }
}*/


