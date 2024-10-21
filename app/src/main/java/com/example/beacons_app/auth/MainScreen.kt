package com.example.beacons_app.auth

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
import androidx.compose.ui.graphics.painter.BrushPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.HorizontalAlignmentLine
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.beacons_app.DestinationScreen
import com.example.beacons_app.FbViewModel
import com.example.beacons_app.R

@Composable
fun MainScreen(navController: NavController, vm: FbViewModel){
    Image(
        painter = painterResource(id = R.drawable.bl),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = Modifier.fillMaxSize()
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 100.dp)
    ){
        Image(
            painter = painterResource(id = R.drawable.logofisi),
            contentDescription = null,
            Modifier.size(200.dp)
        )
        Text(
            text = "Bienvenido",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 40.sp
        )

        Spacer(modifier = Modifier.height(80.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFFFFD700), Color.White, Color(0xFFFFD700))
                    )
                )
        ){
            Button(
                onClick = {
                    navController.navigate(DestinationScreen.Signup.route)
                },
                colors = ButtonDefaults.buttonColors(
                    Color.Transparent
                ),
                modifier = Modifier.width(300.dp)
            ){
                Text(
                    text = "Registrarse",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFFFFD700), Color.White, Color(0xFFFFD700))
                    )
                )
        ){
            Button(
                onClick = {
                    navController.navigate(DestinationScreen.Login.route)
                },
                colors = ButtonDefaults.buttonColors(
                    Color.Transparent
                ),
                modifier = Modifier.width(300.dp)
            ){
                Text(
                    text = "Iniciar Sesión",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp
                )
            }
        }
    }
}