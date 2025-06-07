package com.example.beacons_app.auth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
import com.example.beacons_app.main.NotificationMessage
import com.example.beacons_app.models.Usuario
import com.example.beacons_app.permissions.SystemBroadcastReceiver

@Composable
fun LobbyScreen(
    navController: NavController,
    fbViewModel: FbViewModel,
    asistenciaViewModel: AsistenciaViewModel,
    usuario: Usuario,
    sharedViewModel: SharedViewModel
) {
    val context = LocalContext.current

    // Permisos requeridos
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

    // Observador del Bluetooth o Ubicación apagados
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
            // navController.navigate(DestinationScreen.Schedule.route)
        }

        Spacer(modifier = Modifier.height(15.dp))

        GradientButton("Historial de asistencia") {
            navController.navigate(DestinationScreen.HistorialA.route)
        }

        Spacer(modifier = Modifier.height(15.dp))

        GradientButton("Registrar asistencia") {
            if (blePermissions.all {
                    ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
                }) {
                asistenciaViewModel.registrarAsistencia(navController)
            } else {
                permissionLauncher.launch(blePermissions)
            }
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
fun GradientButton(text: String, onClick: () -> Unit) {
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
            colors = ButtonDefaults.buttonColors(Color.Transparent),
            modifier = Modifier
                .width(300.dp)
                .padding(10.dp)
        ) {
            Text(
                text = text,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp
            )
        }
    }
}

// Utilidad: verifica si ubicación está activa
fun isLocationEnabled(context: Context): Boolean {
    val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
            || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}



/*package com.example.beacons_app.auth

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.beacons_app.AsistenciaViewModel
import com.example.beacons_app.DestinationScreen
import com.example.beacons_app.FbViewModel
import com.example.beacons_app.R
import com.example.beacons_app.models.Usuario

@Composable
fun LobbyScreen(
    navController: NavController,
    fbViewModel: FbViewModel,
    asistenciaViewModel: AsistenciaViewModel,
    usuario: Usuario
) {
    val context = LocalContext.current

    // Permisos necesarios para BLE (dependen de la versión de Android)
    val blePermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
    } else {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    // Launcher para solicitar permisos BLE en tiempo de ejecución
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        // Verificar si *todos* los permisos fueron concedidos
        if (blePermissions.all { results[it] == true }) {
            // Iniciar el registro de asistencia BLE utilizando el ViewModel de asistencia
            asistenciaViewModel.registrarAsistencia()
        } else {
            // Informar al usuario que faltan permisos
            val permisoNombre = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                "Nearby devices" else "Ubicación"
            Toast.makeText(
                context,
                "Debes permitir $permisoNombre para registrar asistencia",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Imagen de fondo de la pantalla de Lobby
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
        // Logo de la institución
        Image(
            painter = painterResource(id = R.drawable.logofisi),
            contentDescription = "Logo de la institución",
            modifier = Modifier.size(100.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Texto de bienvenida con el nombre del usuario logueado
        Text(
            text = "Bienvenido ${usuario.u_nombres}",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Ejemplo de porcentaje de asistencia del usuario
        Text(
            text = "Porcentaje de asistencia",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        Text(
            text = "100%",  // Valor fijo de ejemplo; podría ser dinámico en un caso real
            color = Color.Green,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Botón "Horario y aula" (placeholder deshabilitado)
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
                onClick = {
                    // Navegación a pantalla de Horario y Aula (no implementada)
                    /* navController.navigate(DestinationScreen.Schedule.route) */
                },
                enabled = false,  // Deshabilitado por ahora
                colors = ButtonDefaults.buttonColors(Color.Transparent),
                modifier = Modifier
                    .width(300.dp)
                    .padding(10.dp)
            ) {
                Text(
                    text = "Horario y aula",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Botón "Historial de asistencia" (placeholder deshabilitado)
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
                onClick = {
                    // Navegación a pantalla de Historial de asistencia (no implementada)
                    /* navController.navigate(DestinationScreen.History.route) */
                },
                enabled = false,  // Deshabilitado por ahora
                colors = ButtonDefaults.buttonColors(Color.Transparent),
                modifier = Modifier
                    .width(300.dp)
                    .padding(10.dp)
            ) {
                Text(
                    text = "Historial de asistencia",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Botón "Registrar asistencia"
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
                onClick = {
                    // Si los permisos BLE ya están otorgados, iniciar registro; si no, solicitarlos
                    if (blePermissions.all {
                            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
                        }) {
                        asistenciaViewModel.registrarAsistencia()
                    } else {
                        permissionLauncher.launch(blePermissions)
                    }
                },
                colors = ButtonDefaults.buttonColors(Color.Transparent),
                modifier = Modifier
                    .width(300.dp)
                    .padding(10.dp)
            ) {
                Text(
                    text = "Registrar asistencia",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Botón "Cerrar sesión"
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(Color.Red)
        ) {
            Button(
                onClick = {
                    fbViewModel.logout()
                    // Navegar de regreso a la pantalla de Login y limpiar el backstack
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
}*/



