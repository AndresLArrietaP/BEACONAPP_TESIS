package com.example.beacons_app.DataBeacon.BLE

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.*
import android.bluetooth.le.ScanFilter
import android.content.Context
import android.location.LocationManager
import android.util.Log
import com.example.beacons_app.DataBeacon.EstadoConexion
import com.example.beacons_app.DataBeacon.GestorAsistenciaReceptor
import com.example.beacons_app.models.Asistencia
import com.example.beacons_app.models.AsistenciaUsuario
import com.example.beacons_app.models.Beacon
import com.example.beacons_app.models.Horario
import com.example.beacons_app.models.Usuario
import com.example.beacons_app.util.Recurso
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.tasks.await
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("MissingPermission")
class GestorAsistenciaBLEReceptor(
    private val bluetoothAdapter: BluetoothAdapter,
    private val context: Context,
    private val database: FirebaseDatabase,
    private val auth: FirebaseAuth,
    private val horarioActivo: Horario // ← se pasa desde LobbyScreen
) : GestorAsistenciaReceptor {

    companion object {
        private const val TAG = "GestorAsistBLEReceptor"
        private const val TARGET_DEVICE_MAC = "DD:34:02:09:C2:6C"
        private const val RSSI_THRESHOLD = -70
        private const val REQUIRED_DETECTIONS = 2
    }

    override val data = MutableSharedFlow<Recurso<AsistenciaUsuario>>()

    private val bleScanner by lazy { bluetoothAdapter.bluetoothLeScanner }
    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()
    private var isScanning = false
    private var detectionCount = 0
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun startReceiving() {
        Log.d(TAG, "▶ Iniciando registro de asistencia (BLE)")

        if (!bluetoothAdapter.isEnabled) {
            scope.launch { data.emit(Recurso.Error("Bluetooth apagado")) }
            return
        }
        if (!isLocationEnabled(context)) {
            Log.w(TAG, "⚠️ Ubicación del sistema DESACTIVADA")
            scope.launch {
                data.emit(Recurso.Error("Debes activar la ubicación del dispositivo para escanear beacons."))
            }
            return
        }

        disconnect()
        detectionCount = 0

        scope.launch {
            data.emit(Recurso.Loading(message = "Iniciando scan iBeacon…"))
        }

        val filters = listOf(ScanFilter.Builder().setDeviceAddress(TARGET_DEVICE_MAC).build())
        bleScanner.startScan(filters, scanSettings, scanCallback)
        isScanning = true
        Log.d(TAG, "✅ startScan() ejecutado con filtro de MAC")

        scope.launch {
            delay(10_000)
            if (isScanning) {
                Log.w(TAG, "⚠️ Timeout: no se encontró beacon en 10s")
                disconnect()
                data.emit(Recurso.Error("No se detectó el beacon de asistencia."))
            }
        }
    }

    override fun disconnect() {
        if (isScanning) {
            bleScanner.stopScan(scanCallback)
            isScanning = false
            Log.d(TAG, "disconnect(): scan detenido")
        }
    }

    override fun reconnect() {}

    override fun closeConnection() {
        disconnect()
        scope.cancel()
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val scanRecord = result.scanRecord
            val bytes = scanRecord?.bytes

            Log.d(TAG, "📡 Dispositivo detectado: ${device.address} (${device.name})")
            bytes?.let {
                val hex = it.joinToString(" ") { b -> "%02X".format(b) }
                Log.d(TAG, "📦 Bytes recibidos: $hex")
            }

            if (result.device.address.equals(TARGET_DEVICE_MAC, ignoreCase = true)) {
                val rssi = result.rssi
                if (rssi >= RSSI_THRESHOLD) {
                    val beaconBytes = result.scanRecord?.bytes
                    val iBeacon = beaconBytes?.parseIBeacon()
                    if (iBeacon != null) {
                        if (detectionCount < REQUIRED_DETECTIONS - 1) {
                            detectionCount++
                            Log.d(TAG, "🔍 Beacon detectado una vez (RSSI=$rssi), esperando segunda confirmación...")
                        } else {
                            detectionCount++
                            Log.d(TAG, "✅ Beacon confirmado con $detectionCount detecciones consecutivas.")
                            disconnect()
                            scope.launch {
                                data.emit(Recurso.Loading(message = "Confirmando asistencia…"))
                                checkInUserWithBeacon(iBeacon)
                            }
                        }
                    } else {
                        Log.d(TAG, "↩ Datos recibidos del beacon no corresponden a iBeacon, ignorando…")
                    }
                } else {
                    Log.d(TAG, "↩ Beacon detectado con RSSI $rssi < $RSSI_THRESHOLD, ignorando por señal débil…")
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "❌ onScanFailed: código=$errorCode")
            scope.launch { data.emit(Recurso.Error("Scan fallido: $errorCode")) }
        }
    }

    private fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    suspend fun checkInUserWithBeacon(ibeacon: IBeacon) {
        try {
            val fbUser = auth.currentUser ?: return data.emit(Recurso.Error("No session"))

            val userSnap = database.getReference("users").child(fbUser.uid).get().await()
            val usuario = userSnap.getValue(Usuario::class.java)
                ?: return data.emit(Recurso.Error("Usuario no encontrado"))

            val uuidStr = ibeacon.uuid.toString()
            val beaconsRef = database.getReference("beacons")

            val q = beaconsRef.orderByChild("uuid").equalTo(uuidStr).limitToFirst(1).get().await()
            val (beaconId, beaconDesc) = if (q.exists()) {
                val b = q.children.first().getValue(Beacon::class.java)!!
                b.beacons_id to b.descripcion
            } else {
                val newRef = beaconsRef.push()
                val id = newRef.key!!.hashCode()
                val nuevoBeacon = Beacon(id, "Beacon $uuidStr", uuidStr)
                newRef.setValue(nuevoBeacon).await()
                id to nuevoBeacon.descripcion
            }

            val asistenciaRef = database.getReference("asistencia")
            val lastAsistSnapshot = asistenciaRef.orderByChild("asistencia_id").limitToLast(1).get().await()
            val newAsistenciaId = if (lastAsistSnapshot.exists()) {
                lastAsistSnapshot.children.mapNotNull {
                    it.child("asistencia_id").getValue(Int::class.java)
                }.maxOrNull() ?: 0
            } else {
                0
            } + 1

            val asistencia = Asistencia(
                asistencia_id = newAsistenciaId,
                beacons_id = beaconId,
                beacon_uuid = uuidStr,
                modalidad_id = horarioActivo.id_modalidad.toInt(),
                horario_id = horarioActivo.id_horario.toInt(),
                estado = true,
                descripcion = beaconDesc,
                conexion = "Connected"
            )

            val aKey = asistenciaRef.push().key!!
            asistenciaRef.child(aKey).setValue(asistencia).await()

            val peruTimeZone = TimeZone.getTimeZone("America/Lima")
            val date = Calendar.getInstance(peruTimeZone).time

            val fFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
                timeZone = peruTimeZone
            }.format(date)

            val fHora = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).apply {
                timeZone = peruTimeZone
            }.format(date)

            val asu = AsistenciaUsuario(
                usuario = usuario,
                asistencia = asistencia,
                fecha_registro = fFecha,
                hora_registro = fHora
            )

            database.getReference("asistencia_usuario")
                .child(usuario.usuario_id.toString())
                .child(aKey).setValue(asu).await()

            val semanaRef = database.getReference("horario_usuario/${usuario.usuario_id}/${horarioActivo.id_horario}/semana")
            val semanaActual = semanaRef.get().await().getValue(Int::class.java) ?: 0
            semanaRef.setValue(semanaActual + 1).await()

            data.emit(Recurso.Success(asu))
            data.emit(Recurso.Idle)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error en checkInUserWithBeacon", e)
            data.emit(Recurso.Error(e.localizedMessage ?: "Error inesperado"))
        }
    }

    data class IBeacon(val uuid: UUID, val major: Int, val minor: Int, val txPower: Byte)

    private fun ByteArray.parseIBeacon(): IBeacon? {
        for (i in 0 until size - 30) {
            if ((this[i].toInt() and 0xFF) == 0x02 && (this[i + 1].toInt() and 0xFF) == 0x15) {
                return try {
                    val uuidBytes = copyOfRange(i + 2, i + 18)
                    val bb = ByteBuffer.wrap(uuidBytes).order(ByteOrder.BIG_ENDIAN)
                    val uuid = UUID(bb.long, bb.long)
                    val major = ((this[i + 18].toInt() and 0xFF) shl 8) + (this[i + 19].toInt() and 0xFF)
                    val minor = ((this[i + 20].toInt() and 0xFF) shl 8) + (this[i + 21].toInt() and 0xFF)
                    val txPwr = this[i + 22]
                    IBeacon(uuid, major, minor, txPwr)
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error al parsear iBeacon: ${e.message}")
                    null
                }
            }
        }
        return null
    }
}



/*package com.example.beacons_app.DataBeacon.BLE

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.*
import android.content.Context
import android.location.LocationManager
import android.util.Log
import com.example.beacons_app.DataBeacon.EstadoConexion
import com.example.beacons_app.DataBeacon.GestorAsistenciaReceptor
import com.example.beacons_app.models.Asistencia
import com.example.beacons_app.models.AsistenciaUsuario
import com.example.beacons_app.models.Beacon
import com.example.beacons_app.models.Usuario
import com.example.beacons_app.util.Recurso
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.tasks.await
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("MissingPermission")
class GestorAsistenciaBLEReceptor(
    private val bluetoothAdapter: BluetoothAdapter,
    private val context: Context,
    private val database: FirebaseDatabase,
    private val auth: FirebaseAuth
) : GestorAsistenciaReceptor {

    companion object {
        private const val TAG = "GestorAsistBLEReceptor"
    }

    override val data = MutableSharedFlow<Recurso<Asistencia>>()

    private val bleScanner by lazy { bluetoothAdapter.bluetoothLeScanner }
    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()
    private var isScanning = false
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun startReceiving() {
        Log.d(TAG, "▶ Iniciando registro de asistencia (BLE)")

        if (!bluetoothAdapter.isEnabled) {
            scope.launch { data.emit(Recurso.Error("Bluetooth apagado")) }
            return
        }

        if (!isLocationEnabled(context)) {
            Log.w(TAG, "⚠️ Ubicación del sistema DESACTIVADA")
            scope.launch {
                data.emit(Recurso.Error("Debes activar la ubicación del dispositivo para escanear beacons."))
            }
            return
        }

        disconnect()

        scope.launch {
            data.emit(Recurso.Loading<Asistencia>(message = "Iniciando scan iBeacon…"))
        }

        bleScanner.startScan(null, scanSettings, scanCallback)
        isScanning = true
        Log.d(TAG, "✅ startScan() ejecutado SIN filtros (modo libre)")

        scope.launch {
            delay(10_000)
            if (isScanning) {
                Log.w(TAG, "⚠️ Timeout: no se encontró beacon en 10s")
                disconnect()
                data.emit(Recurso.Error("No se detectó el beacon de asistencia."))
            }
        }
    }

    override fun disconnect() {
        if (isScanning) {
            bleScanner.stopScan(scanCallback)
            isScanning = false
            Log.d(TAG, "disconnect(): scan detenido")
        }
    }

    override fun reconnect() {}

    override fun closeConnection() {
        disconnect()
        scope.cancel()
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val scanRecord = result.scanRecord
            val bytes = scanRecord?.bytes

            Log.d(TAG, "📡 Dispositivo detectado: ${device.address} (${device.name})")
            bytes?.let {
                val hex = it.joinToString(" ") { b -> "%02X".format(b) }
                Log.d(TAG, "📦 Bytes recibidos: $hex")
            }

            val ibeacon = bytes?.parseIBeacon()
            if (ibeacon != null) {
                Log.d(TAG, "✔ iBeacon detectado: uuid=${ibeacon.uuid}, major=${ibeacon.major}, minor=${ibeacon.minor}")
                disconnect()
                scope.launch {
                    data.emit(Recurso.Loading<Asistencia>(message = "Beacon encontrado, registrando…"))
                    checkInUserWithBeacon(ibeacon)
                }
            } else {
                Log.d(TAG, "↩ No es un iBeacon válido, ignorando…")
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "❌ onScanFailed: código=$errorCode")
            scope.launch { data.emit(Recurso.Error("Scan fallido: $errorCode")) }
        }
    }

    private fun ByteArray.parseIBeacon(): IBeacon? {
        for (i in 0 until size - 30) {
            if ((this[i].toInt() and 0xFF) == 0x02 && (this[i + 1].toInt() and 0xFF) == 0x15) {
                try {
                    val uuidBytes = copyOfRange(i + 2, i + 18)
                    val bb = ByteBuffer.wrap(uuidBytes).order(ByteOrder.BIG_ENDIAN)
                    val uuid = UUID(bb.long, bb.long)
                    val major = ((this[i + 18].toInt() and 0xFF) shl 8) + (this[i + 19].toInt() and 0xFF)
                    val minor = ((this[i + 20].toInt() and 0xFF) shl 8) + (this[i + 21].toInt() and 0xFF)
                    val txPwr = this[i + 22]
                    return IBeacon(uuid, major, minor, txPwr)
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error al parsear iBeacon: ${e.message}")
                    return null
                }
            }
        }
        return null
    }

    private fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    suspend fun checkInUserWithBeacon(ibeacon: IBeacon) {
        try {
            val fbUser = auth.currentUser ?: return data.emit(Recurso.Error("No session"))
            val userSnap = database.getReference("usuarios")
                .child(fbUser.uid).get().await()
            val usuario = userSnap.getValue(Usuario::class.java)
                ?: return data.emit(Recurso.Error("Usuario no encontrado"))

            val uuidStr = ibeacon.uuid.toString()
            val beaconsRef = database.getReference("beacons")
            val q = beaconsRef.orderByChild("uuid").equalTo(uuidStr).limitToFirst(1).get().await()
            val (beaconId, beaconDesc) = if (q.exists()) {
                val b = q.children.first().getValue(Beacon::class.java)!!
                b.beacons_id to b.descripcion
            } else {
                val newRef = beaconsRef.push()
                val id = newRef.key!!.hashCode()
                val nuevoBeacon = Beacon(id, "Beacon $uuidStr", uuidStr)
                newRef.setValue(nuevoBeacon).await()
                id to nuevoBeacon.descripcion
            }

            val asistencia = Asistencia(
                beacons_id = beaconId,
                beacon_uuid = uuidStr,
                modalidad_id = 1,
                horario_id = 1,
                estado = true,
                descripcion = beaconDesc,
                conexion = EstadoConexion.Connected
            )

            val aKey = database.getReference("asistencia").push().key!!
            database.getReference("asistencia").child(aKey).setValue(asistencia).await()

            val ahora = Date()
            val fFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(ahora)
            val fHora = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(ahora)
            val asu = AsistenciaUsuario(
                usuario = usuario,
                asistencia = asistencia.copy(asistencia_id = aKey.hashCode()),
                fecha_registro = fFecha,
                hora_registro = fHora
            )

            database.getReference("asistencia_usuario")
                .child(usuario.usuario_id.toString())
                .child(aKey).setValue(asu).await()

            data.emit(Recurso.Success(asistencia))
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error en checkInUserWithBeacon", e)
            data.emit(Recurso.Error(e.localizedMessage ?: "Error inesperado"))
        }
    }

    data class IBeacon(val uuid: UUID, val major: Int, val minor: Int, val txPower: Byte)
}*/

