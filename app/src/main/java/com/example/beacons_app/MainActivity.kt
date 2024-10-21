package com.example.beacons_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.beacons_app.auth.LoginScreen
import com.example.beacons_app.auth.MainScreen
import com.example.beacons_app.auth.SignupScreen
import com.example.beacons_app.auth.SuccessScreen
import com.example.beacons_app.main.NotificationMessage
import com.example.beacons_app.ui.theme.BEACONS_APPTheme
import dagger.hilt.android.AndroidEntryPoint
//MAIN ACTIVITY
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            window.statusBarColor = getColor(R.color.black)
            window.navigationBarColor = getColor(R.color.black)
            BEACONS_APPTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ){
                    AuthenticationApp()
                }
            }
        }
    }
}

sealed class DestinationScreen(val route: String){
    object Main: DestinationScreen("main")
    object Signup: DestinationScreen("signup")
    object Login: DestinationScreen("login")
    object Success: DestinationScreen("success")
}
@Composable
fun AuthenticationApp(){
    val vm = hiltViewModel<FbViewModel>()
    val navController = rememberNavController()
    
    NotificationMessage(vm)
    
    NavHost(navController = navController, startDestination = DestinationScreen.Main.route){
        composable(DestinationScreen.Main.route){
            MainScreen(navController, vm)
        }
        composable(DestinationScreen.Signup.route){
            SignupScreen(navController, vm)
        }
        composable(DestinationScreen.Login.route){
            LoginScreen(navController, vm)
        }
        composable(DestinationScreen.Success.route){
            SuccessScreen(navController, vm)
        }
    }

}