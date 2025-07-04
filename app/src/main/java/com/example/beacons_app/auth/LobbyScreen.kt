package com.example.beacons_app.auth

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.beacons_app.*
import com.example.beacons_app.DataBeacon.BLE.GestorAsistenciaBLEReceptor
import com.example.beacons_app.R
import com.example.beacons_app.models.Horario
import com.example.beacons_app.models.Usuario
import com.example.beacons_app.permissions.SystemBroadcastReceiver
import com.example.beacons_app.util.Recurso
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

@Composable
fun LobbyScreen(
    navController: NavController,
    fbViewModel: FbViewModel,
    usuario: Usuario,
    sharedViewModel: SharedViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val puedeRegistrar = remember { mutableStateOf(false) }
    val horarioDetectado = remember { mutableStateOf<Horario?>(null) }

    val bluetoothAdapter = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    val auth = Firebase.auth
    val database = FirebaseDatabase.getInstance()

    val gestorAsistencia = remember(horarioDetectado.value) {
        horarioDetectado.value?.let {
            GestorAsistenciaBLEReceptor(
                bluetoothAdapter = bluetoothAdapter,
                context = context,
                database = database,
                auth = auth,
                horarioActivo = it
            )
        }
    }

    // Ya no necesitas crear AsistenciaViewModel si usas gestor directamente
    // val asistenciaViewModel = remember { ... } ← eliminada

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (!granted) {
                Toast.makeText(context, "Debes aceptar el permiso para recibir notificaciones", Toast.LENGTH_LONG).show()
            }
        }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    LaunchedEffect(true) {
        createNotificationChannel(context)
    }

    LaunchedEffect(usuario.usuario_id) {
        scope.launch(Dispatchers.IO) {
            val tz = TimeZone.getTimeZone("America/Lima")
            val ahora = Calendar.getInstance(tz)
            val diaNombre = obtenerDiaActual(ahora.get(Calendar.DAY_OF_WEEK))
            val horaActual = obtenerHoraPedagogicaActual(ahora)

            fbViewModel.verificarAsistenciaPermitida(
                usuario.usuario_id,
                diaNombre,
                horaActual
            ) { permitido, horarioValido ->
                scope.launch(Dispatchers.Main) {
                    puedeRegistrar.value = permitido
                    horarioDetectado.value = horarioValido
                }
            }

            fbViewModel.obtenerHorariosPorUsuario(usuario.usuario_id) { lista ->
                val diasHoras = mutableListOf<Pair<Int, Int>>()
                for (horario in lista) {
                    val diaSemana = when (horario.dia) {
                        "Lunes" -> Calendar.MONDAY
                        "Martes" -> Calendar.TUESDAY
                        "Miércoles" -> Calendar.WEDNESDAY
                        "Jueves" -> Calendar.THURSDAY
                        "Viernes" -> Calendar.FRIDAY
                        "Sábado" -> Calendar.SATURDAY
                        "Domingo" -> Calendar.SUNDAY
                        else -> continue
                    }
                    for (hora in horario.horas) {
                        diasHoras.add(Pair(diaSemana, hora.toInt()))
                    }
                }
                fbViewModel.programarNotificaciones(context, diasHoras)
            }

            fbViewModel.guardarUsuarioEnLocal(context, usuario)
        }
    }
    /*LaunchedEffect(gestorAsistencia) {
        gestorAsistencia?.data?.collect { recurso ->
            when (recurso) {
                is Recurso.Success -> {
                    sharedViewModel.ultimaAsistencia.value = recurso.data
                    navController.navigate(DestinationScreen.Success.route)

                }
                is Recurso.Error -> {
                    Toast.makeText(context, recurso.errorMessage ?: "Error", Toast.LENGTH_LONG).show()
                }
                else -> {} // ignorar otros estados
            }
        }
    }*/

    LaunchedEffect(gestorAsistencia) {
        gestorAsistencia?.data?.collect { recurso ->
            when (recurso) {
                is Recurso.Success -> {
                    sharedViewModel.ultimaAsistencia.value = recurso.data

                    // 🔄 Volver a verificar si puede registrar asistencia (probablemente ya no)
                    val tz = TimeZone.getTimeZone("America/Lima")
                    val ahora = Calendar.getInstance(tz)
                    val diaNombre = obtenerDiaActual(ahora.get(Calendar.DAY_OF_WEEK))
                    val horaActual = obtenerHoraPedagogicaActual(ahora)

                    fbViewModel.verificarAsistenciaPermitida(
                        usuario.usuario_id,
                        diaNombre,
                        horaActual
                    ) { permitido, horarioValido ->
                        puedeRegistrar.value = permitido
                        horarioDetectado.value = horarioValido
                        // Navega después de actualizar el estado
                        navController.navigate(DestinationScreen.Success.route)
                    }
                }

                is Recurso.Error -> {
                    Toast.makeText(context, recurso.errorMessage ?: "Error", Toast.LENGTH_LONG).show()
                }
                else -> {} // ignorar otros estados
            }
        }
    }



    val blePermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    } else {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val allGranted = blePermissions.all { results[it] == true }
        if (allGranted) {
            gestorAsistencia?.startReceiving()
        } else {
            Toast.makeText(
                context,
                "Debes otorgar permisos de ubicación y bluetooth para registrar asistencia",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    SystemBroadcastReceiver(android.location.LocationManager.PROVIDERS_CHANGED_ACTION) {
        val enabled = isLocationEnabled(context)
        if (!enabled) {
            Toast.makeText(context, "⚠️ La ubicación fue desactivada", Toast.LENGTH_LONG).show()
        }
    }

    SystemBroadcastReceiver(BluetoothAdapter.ACTION_STATE_CHANGED) {
        val enabled = BluetoothAdapter.getDefaultAdapter()?.isEnabled == true
        if (!enabled) {
            Toast.makeText(context, "⚠️ El Bluetooth fue desactivado", Toast.LENGTH_LONG).show()
        }
    }

    // UI Layout
    Image(
        painter = painterResource(id = R.drawable.gr),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = Modifier.fillMaxSize()
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize().padding(top = 50.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.logofisi),
            contentDescription = "Logo",
            modifier = Modifier.size(100.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Bienvenido ${usuario.u_nombres}",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        GradientButton("Horario y aula") {
            navController.navigate(DestinationScreen.VerHorarios.route)
        }

        Spacer(modifier = Modifier.height(15.dp))

        GradientButton("Historial de asistencia") {
            navController.navigate(DestinationScreen.HistorialA.route)
        }

        Spacer(modifier = Modifier.height(15.dp))

        GradientButton(
            text = "Registrar asistencia",
            enabled = puedeRegistrar.value
        ) {
            if (blePermissions.all {
                    ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
                }) {
                gestorAsistencia?.startReceiving()
            } else {
                permissionLauncher.launch(blePermissions)
            }
        }

        if (!puedeRegistrar.value) {
            Text(
                text = "⛔ Fuera del horario asignado",
                color = Color.Red,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 10.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(Color.Red)
        ) {
            Button(
                onClick = {
                    fbViewModel.logout(context)
                    navController.navigate(DestinationScreen.Login.route) {
                        popUpTo(DestinationScreen.Lobby.route) { inclusive = true }
                    }
                },
                colors = ButtonDefaults.buttonColors(Color.Transparent),
                modifier = Modifier
                    .width(300.dp)
                    .padding(10.dp)
            ) {
                Text(
                    text = "Cerrar sesión",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp
                )
            }
        }
    }
}


// Botón con gradiente dorado
@Composable
fun GradientButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFFFFD700), Color.White, Color(0xFFFFD700))
                )
            )
    ) {
        Button(
            onClick = onClick,
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(Color.Transparent),
            modifier = Modifier
                .width(300.dp)
                .padding(10.dp)
        ) {
            Text(
                text = text,
                color = if (enabled) Color.Black else Color.Gray,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp
            )
        }
    }
}

// Función para obtener el nombre del día actual
fun obtenerDiaActual(dia: Int): String = when (dia) {
    Calendar.MONDAY -> "Lunes"
    Calendar.TUESDAY -> "Martes"
    Calendar.WEDNESDAY -> "Miércoles"
    Calendar.THURSDAY -> "Jueves"
    Calendar.FRIDAY -> "Viernes"
    Calendar.SATURDAY -> "Sábado"
    Calendar.SUNDAY -> "Domingo"
    else -> "Desconocido"
}

// Mapea la hora del sistema a una hora pedagógica (1 a 7)
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

// Verifica si la ubicación está activada
fun isLocationEnabled(context: Context): Boolean {
    val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
            || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}

// Crea canal para notificaciones (requerido desde Android O)
fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Canal de asistencia"
        val descriptionText = "Notificaciones de clases próximas"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel("asistencia_channel", name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}






/*package com.example.beacons_app.auth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.beacons_app.*
import com.example.beacons_app.R
import com.example.beacons_app.models.Usuario
import com.example.beacons_app.permissions.SystemBroadcastReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun LobbyScreen(
    navController: NavController,
    fbViewModel: FbViewModel,
    asistenciaViewModel: AsistenciaViewModel,
    usuario: Usuario,
    sharedViewModel: SharedViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val puedeRegistrar = remember { mutableStateOf(false) }

    // Evaluar si el usuario puede registrar asistencia (según día y hora)
    LaunchedEffect(usuario.usuario_id) {
        scope.launch(Dispatchers.IO) {
            val ahora = Calendar.getInstance()
            val diaNombre = obtenerDiaActual(ahora.get(Calendar.DAY_OF_WEEK))
            val horaActual = obtenerHoraPedagogicaActual(ahora)

            fbViewModel.verificarAsistenciaPermitida(
                usuario.usuario_id,
                diaNombre,
                horaActual
            ) { permitido ->
                puedeRegistrar.value = permitido
            }
        }
    }

    // Permisos BLE y localización
    val blePermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    } else {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val allGranted = blePermissions.all { results[it] == true }
        if (allGranted) {
            asistenciaViewModel.registrarAsistencia(navController)
        } else {
            Toast.makeText(
                context,
                "Debes otorgar permisos de ubicación y bluetooth para registrar asistencia",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Observadores de cambios en ubicación o bluetooth
    SystemBroadcastReceiver(
        systemAction = android.location.LocationManager.PROVIDERS_CHANGED_ACTION
    ) {
        val enabled = isLocationEnabled(context)
        if (!enabled) {
            Toast.makeText(context, "⚠️ La ubicación fue desactivada", Toast.LENGTH_LONG).show()
        }
    }

    SystemBroadcastReceiver(
        systemAction = BluetoothAdapter.ACTION_STATE_CHANGED
    ) {
        val enabled = BluetoothAdapter.getDefaultAdapter()?.isEnabled == true
        if (!enabled) {
            Toast.makeText(context, "⚠️ El Bluetooth fue desactivado", Toast.LENGTH_LONG).show()
        }
    }

    // Fondo
    Image(
        painter = painterResource(id = R.drawable.gr),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = Modifier.fillMaxSize()
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 50.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.logofisi),
            contentDescription = "Logo",
            modifier = Modifier.size(100.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Bienvenido ${usuario.u_nombres}",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        GradientButton("Horario y aula") {
            navController.navigate(DestinationScreen.VerHorarios.route)
        }

        Spacer(modifier = Modifier.height(15.dp))

        GradientButton("Historial de asistencia") {
            navController.navigate(DestinationScreen.HistorialA.route)
        }

        Spacer(modifier = Modifier.height(15.dp))

        GradientButton(
            text = "Registrar asistencia",
            enabled = puedeRegistrar.value
        ) {
            if (blePermissions.all {
                    ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
                }) {
                asistenciaViewModel.registrarAsistencia(navController)
            } else {
                permissionLauncher.launch(blePermissions)
            }
        }

        if (!puedeRegistrar.value) {
            Text(
                text = "⛔ Fuera del horario asignado",
                color = Color.Red,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 10.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(Color.Red)
        ) {
            Button(
                onClick = {
                    fbViewModel.logout()
                    navController.navigate(DestinationScreen.Login.route) {
                        popUpTo(DestinationScreen.Lobby.route) { inclusive = true }
                    }
                },
                colors = ButtonDefaults.buttonColors(Color.Transparent),
                modifier = Modifier
                    .width(300.dp)
                    .padding(10.dp)
            ) {
                Text(
                    text = "Cerrar sesión",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp
                )
            }
        }
    }
}

@Composable
fun GradientButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFFFFD700), Color.White, Color(0xFFFFD700))
                )
            )
    ) {
        Button(
            onClick = onClick,
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(Color.Transparent),
            modifier = Modifier
                .width(300.dp)
                .padding(10.dp)
        ) {
            Text(
                text = text,
                color = if (enabled) Color.Black else Color.Gray,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp
            )
        }
    }
}

// Día a texto
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

// Hora a hora pedagógica
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

// Verifica si ubicación está activa
fun isLocationEnabled(context: Context): Boolean {
    val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
            || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}*/
