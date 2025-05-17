package com.example.beacons_app

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
}






/*package com.example.beacons_app

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.beacons_app.DataBeacon.GestorAsistenciaReceptor
import com.example.beacons_app.Event
import com.example.beacons_app.util.Recurso
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AsistenciaViewModel @Inject constructor(
    private val gestorAsistencia: GestorAsistenciaReceptor
) : ViewModel() {

    companion object {
        private const val TAG = "AsistenciaViewModel"
    }

    // Indica si se está procesando el registro de asistencia (escaneo en curso)
    val inProgress = mutableStateOf(false)

    // Evento de mensaje para notificaciones (p. ej., resultados o errores)
    val popupNotification = mutableStateOf<Event<String>?>(null)

    /**
     * Inicia el proceso de escaneo BLE para registrar la asistencia del usuario actual.
     * Escucha los estados del [gestorAsistencia] y actualiza [inProgress] y [popupNotification] según el resultado.
     */
    fun registrarAsistencia() {
        viewModelScope.launch {
            Log.d(TAG, "▶ Iniciando registro de asistencia (BLE)")
            gestorAsistencia.startReceiving()  // Comenzar a escanear el beacon de asistencia

            // Colectar emisiones del flujo de datos del gestor (Loading/Success/Error)
            gestorAsistencia.data.collect { recurso ->
                when (recurso) {
                    is Recurso.Loading -> {
                        // Escaneo en progreso
                        Log.d(TAG, "BLE Loading: ${recurso.message}")
                        inProgress.value = true
                    }
                    is Recurso.Error -> {
                        // Ocurrió un error durante el escaneo o registro
                        Log.e(TAG, "BLE Error: ${recurso.errorMessage}")
                        inProgress.value = false
                        popupNotification.value = Event("Error BLE: ${recurso.errorMessage}")
                        gestorAsistencia.disconnect()  // Detener cualquier escaneo activo
                        return@collect  // Salir de la recopilación tras error
                    }
                    is Recurso.Success -> {
                        // Asistencia registrada con éxito
                        Log.d(TAG, "BLE Success: asistencia=${recurso.data}")
                        inProgress.value = false
                        popupNotification.value = Event("✅ Asistencia registrada correctamente")
                        gestorAsistencia.disconnect()  // Detener el escaneo ya que se obtuvo resultado
                        return@collect  // Salir tras el éxito
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Liberar recursos BLE cuando el ViewModel sea destruido
        gestorAsistencia.closeConnection()
    }
}*/


