package com.example.beacons_app

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.beacons_app.auth.*
import com.example.beacons_app.main.NotificationMessage
import com.example.beacons_app.ui.docente.VerHorariosScreen
import com.example.beacons_app.ui.personal.CargarHorarioScreen
import com.example.beacons_app.ui.personal.ListaProfesoresScreen
import com.example.beacons_app.ui.theme.BEACONS_APPTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var bluetoothAdapter: BluetoothAdapter

    private var isBluetoothDialogAlreadyShown = false

    private val enableBtLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isBluetoothDialogAlreadyShown = false
        if (result.resultCode != Activity.RESULT_OK) {
            Toast.makeText(
                this,
                "La aplicación necesita Bluetooth activado",
                Toast.LENGTH_SHORT
            ).show()
            showBluetoothDialog()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BEACONS_APPTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AuthenticationApp()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        showBluetoothDialog()
    }

    private fun showBluetoothDialog() {
        if (!bluetoothAdapter.isEnabled && !isBluetoothDialogAlreadyShown) {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBtLauncher.launch(intent)
            isBluetoothDialogAlreadyShown = true
        }
    }
}

sealed class DestinationScreen(val route: String) {
    object Signup : DestinationScreen("signup")
    object Login : DestinationScreen("login")
    object Success : DestinationScreen("success")
    object Lobby : DestinationScreen("lobby")
    object LobbyPersonal : DestinationScreen("lobby_personal")
    object HistorialA : DestinationScreen("historial")
    object ListaProfesores : DestinationScreen("lista_profesores")
    object CargarHorario : DestinationScreen("cargar_horario/{usuarioId}")
    object VerHorarios : DestinationScreen("ver_horarios")
}

@Composable
fun AuthenticationApp() {
    val fbViewModel: FbViewModel = hiltViewModel()
    val sharedViewModel: SharedViewModel = hiltViewModel()
    //val asistenciaViewModel: AsistenciaViewModel = hiltViewModel()
    val navController = rememberNavController()

    NotificationMessage(fbViewModel.popupNotification)
    //NotificationMessage(asistenciaViewModel.popupNotification)

    NavHost(navController = navController, startDestination = DestinationScreen.Login.route) {

        composable(DestinationScreen.Login.route) {
            LoginScreen(navController, fbViewModel, sharedViewModel)
        }

        composable(DestinationScreen.Signup.route) {
            SignupScreen(navController, fbViewModel)
        }

        composable(DestinationScreen.Lobby.route) {
            sharedViewModel.usuario.value?.let { usuario ->
                LobbyScreen(navController, fbViewModel, usuario, sharedViewModel)
            }
        }

        composable(DestinationScreen.LobbyPersonal.route) {
            sharedViewModel.usuario.value?.let { usuario ->
                LobbyPersonalScreen(navController, usuario,fbViewModel)
            }
        }

        composable(DestinationScreen.HistorialA.route) {
            sharedViewModel.usuario.value?.let { usuario ->
                HistorialAScreen(navController, usuario)
            }
        }

        composable(DestinationScreen.Success.route) {
            SuccessScreen(navController, sharedViewModel.ultimaAsistencia.value)
        }


        composable(DestinationScreen.ListaProfesores.route) {
            ListaProfesoresScreen(navController, fbViewModel)
        }

        composable(DestinationScreen.CargarHorario.route) { backStackEntry ->
            val usuarioId = backStackEntry.arguments?.getString("usuarioId")?.toIntOrNull() ?: 0
            CargarHorarioScreen(navController, usuarioId, fbViewModel)
        }

        composable(DestinationScreen.VerHorarios.route) {
            sharedViewModel.usuario.value?.let { usuario ->
                VerHorariosScreen(usuario, fbViewModel, navController)
            }
        }

    }
}


/*package com.example.beacons_app

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.beacons_app.auth.*
import com.example.beacons_app.main.NotificationMessage
import com.example.beacons_app.models.Usuario
import com.example.beacons_app.ui.personal.CargarHorarioScreen
import com.example.beacons_app.ui.personal.ListaProfesoresScreen
import com.example.beacons_app.ui.theme.BEACONS_APPTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var bluetoothAdapter: BluetoothAdapter

    private var isBluetoothDialogAlreadyShown = false

    private val enableBtLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isBluetoothDialogAlreadyShown = false
        if (result.resultCode != Activity.RESULT_OK) {
            Toast.makeText(
                this,
                "La aplicación necesita Bluetooth activado",
                Toast.LENGTH_SHORT
            ).show()
            showBluetoothDialog()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BEACONS_APPTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AuthenticationApp()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        showBluetoothDialog()
    }

    private fun showBluetoothDialog() {
        if (!bluetoothAdapter.isEnabled && !isBluetoothDialogAlreadyShown) {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBtLauncher.launch(intent)
            isBluetoothDialogAlreadyShown = true
        }
    }
}

sealed class DestinationScreen(val route: String) {
    //object Main : DestinationScreen("main")
    object Signup : DestinationScreen("signup")
    object Login : DestinationScreen("login")
    object Success : DestinationScreen("success")
    object Lobby : DestinationScreen("lobby")
    object LobbyPersonal : DestinationScreen("lobby_personal")
    object HistorialA : DestinationScreen("historial")
    object ListaProfesores : DestinationScreen("lista_profesores")
    object CargarHorario : DestinationScreen("cargar_horario")

}

@Composable
fun AuthenticationApp() {
    val fbViewModel: FbViewModel = hiltViewModel()
    val sharedViewModel: SharedViewModel = hiltViewModel()
    val asistenciaViewModel: AsistenciaViewModel = hiltViewModel()
    val navController = rememberNavController()

    NotificationMessage(fbViewModel.popupNotification)
    NotificationMessage(asistenciaViewModel.popupNotification)

    NavHost(navController = navController, startDestination = DestinationScreen.Login.route) {

        /*composable(DestinationScreen.Main.route) {
            MainScreen(navController, fbViewModel)
        }*/

        composable(DestinationScreen.Login.route) {
            LoginScreen(navController, fbViewModel, sharedViewModel)
        }

        composable(DestinationScreen.Signup.route) {
            SignupScreen(navController, fbViewModel)
        }

        composable(DestinationScreen.Lobby.route) {
            sharedViewModel.usuario.value?.let { usuario ->
                LobbyScreen(navController, fbViewModel, asistenciaViewModel, usuario, sharedViewModel)
            }
        }

        composable(DestinationScreen.LobbyPersonal.route) {
            sharedViewModel.usuario.value?.let { usuario ->
                LobbyPersonalScreen(navController,usuario)
            }
        }

        composable(DestinationScreen.HistorialA.route) {
            sharedViewModel.usuario.value?.let { usuario ->
                HistorialAScreen(navController, usuario)
            }
        }

        composable(DestinationScreen.Success.route) {
            SuccessScreen(navController, asistenciaViewModel.ultimaAsistencia.value)
        }

        composable(DestinationScreen.ListaProfesores.route) {
            ListaProfesoresScreen(navController, fbViewModel)
        }

        composable("cargar_horario/{usuarioId}") { backStackEntry ->
            val usuarioId = backStackEntry.arguments?.getString("usuarioId")?.toIntOrNull() ?: 0
            CargarHorarioScreen(navController, usuarioId, fbViewModel)
        }

    }
}*/








