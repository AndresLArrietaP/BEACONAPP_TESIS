package com.example.beacons_app.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.beacons_app.DestinationScreen
import com.example.beacons_app.FbViewModel
import com.example.beacons_app.R
import org.w3c.dom.Text

@Composable
fun SignupScreen(navController: NavController, vm: FbViewModel){
    val emty by remember{ mutableStateOf("") }
    var email by remember{ mutableStateOf("") }
    var password by remember{ mutableStateOf("") }
    var cpassword by remember{ mutableStateOf("") }
    var passwordVisibility by remember{ mutableStateOf(false) }
    var cpasswordVisibility by remember{ mutableStateOf(false) }
    var erroeE by remember{ mutableStateOf(false) }
    var erroeP by remember{ mutableStateOf(false) }
    var erroeCP by remember{ mutableStateOf(false) }
    var erroeC by remember{ mutableStateOf(false) }
    var plength by remember{ mutableStateOf(false) }

    Image(
        painter = painterResource(id = R.drawable.rd),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = Modifier.fillMaxSize()
    )
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        if(vm.inProgress.value){
            CircularProgressIndicator()
        }
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 150.dp)
            .verticalScroll(
                rememberScrollState()
            )
    ){
        Text(
            text = "Registro de usuario",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 40.sp
        )

        Spacer(modifier = Modifier.height(50.dp))

        if(erroeE){
            Text(
                text = "Ingresa correo",
                color = Color.Red,
                modifier = Modifier.padding(end=100.dp)
            )
        }
        TextField (
            value = email,
            onValueChange = {
                email = it
            },
            label = {
                Text(
                    text="Correo"
                )
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(id=R.drawable.baseline_person_24),
                    contentDescription = null
                )
            },
            trailingIcon = {
                if(email.isNotEmpty()){
                    Icon(
                        painter = painterResource(id=R.drawable.baseline_person_24),
                        contentDescription = null,
                        Modifier.clickable { email = emty }
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            textStyle = TextStyle(
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            ),

            shape = RoundedCornerShape(50.dp),
            modifier = Modifier
                .width(300.dp)
                .height(60.dp),
            colors = TextFieldDefaults.colors(
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                cursorColor = Color.Red,
                focusedContainerColor = Color(0x30FFFFFF),
                focusedLeadingIconColor = Color.White,
                unfocusedLeadingIconColor = Color.White,
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.White,
                focusedTrailingIconColor = Color.White,
                unfocusedTrailingIconColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(30.dp))

        if(erroeP){
            Text(
                text = "Ingresa contraseña",
                color = Color.White,
                modifier = Modifier.padding(end=100.dp)
            )
        }
        if(plength){
            Text(
                text = "Contraseña debe tener 6 caractéres",
                color = Color.Red,
                modifier = Modifier.padding(end=100.dp)
            )
        }
        TextField (
            value = password,
            onValueChange = {
                password = it
                plength = it.length<6
            },
            label = {
                Text(text = "Contraseña")
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(id=R.drawable.baseline_person_24),
                    contentDescription = null
                )
            },
            trailingIcon = {
                if(password.isNotEmpty()){
                    val visibilityIcon = if(passwordVisibility){
                        painterResource(id=R.drawable.baseline_person_24)
                    }else{
                        painterResource(id=R.drawable.baseline_person_24)
                    }
                    Icon(
                        painter = visibilityIcon,
                        contentDescription = if(passwordVisibility) "Hide Password" else "Show Password",
                        Modifier.clickable {
                            passwordVisibility = !passwordVisibility
                        }
                    )
                }
            },
            visualTransformation = if(passwordVisibility){
                VisualTransformation.None
            }else{
                 PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Password
            ),
            singleLine = true,
            textStyle = TextStyle(
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            ),

            shape = RoundedCornerShape(50.dp),
            modifier = Modifier
                .width(300.dp)
                .height(60.dp),
            colors = TextFieldDefaults.colors(
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                cursorColor = Color.Red,
                focusedContainerColor = Color(0x30FFFFFF),
                focusedLeadingIconColor = Color.White,
                unfocusedLeadingIconColor = Color.White,
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.White,
                focusedTrailingIconColor = Color.White,
                unfocusedTrailingIconColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(30.dp))

        if(erroeCP){
            Text(
                text = "Contraseña no coincide",
                color = Color.Red,
                modifier = Modifier.padding(end=100.dp)
            )
        }
        if(erroeC){
            Text(
                text = "Ingresa confirmación de contraseña",
                color = Color.Red,
                modifier = Modifier.padding(end=100.dp)
            )
        }
        TextField (
            value = cpassword,
            onValueChange = {
                cpassword = it
                plength = it.length<6
            },
            label = {
                Text(text = "Confirmar contraseña")
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(id=R.drawable.baseline_person_24),
                    contentDescription = null
                )
            },
            trailingIcon = {
                if(cpassword.isNotEmpty()){
                    val visibilityIcon = if(cpasswordVisibility){
                        painterResource(id=R.drawable.baseline_person_24)
                    }else{
                        painterResource(id=R.drawable.baseline_person_24)
                    }
                    Icon(
                        painter = visibilityIcon,
                        contentDescription = if(cpasswordVisibility) "Ocultar contraseña" else "Mostrar contraseña",
                        Modifier.clickable {
                            cpasswordVisibility = !cpasswordVisibility
                        }
                    )
                }
            },
            visualTransformation = if(cpasswordVisibility){
                VisualTransformation.None
            }else{
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Password
            ),
            singleLine = true,
            textStyle = TextStyle(
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            ),

            shape = RoundedCornerShape(50.dp),
            modifier = Modifier
                .width(300.dp)
                .height(60.dp),
            colors = TextFieldDefaults.colors(
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                cursorColor = Color.Red,
                focusedContainerColor = Color(0x30FFFFFF),
                focusedLeadingIconColor = Color.White,
                unfocusedLeadingIconColor = Color.White,
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.White,
                focusedTrailingIconColor = Color.White,
                unfocusedTrailingIconColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(50.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(
                    color = Color.White
                )
        ){
            Button(
                onClick = {
                    if(email.isNotEmpty()){
                        erroeE = false
                        if(password.isNotEmpty()){
                           erroeP = false
                            if(cpassword.isNotEmpty()){
                                erroeC = false
                                if(password == cpassword){
                                    erroeCP = false
                                    vm.onSingup(email,password)
                                }else{
                                    erroeCP = true
                                }
                            }else{
                                erroeC = true
                            }
                        }else{
                            erroeP = true
                        }

                    }else{
                        erroeE = true
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    Color.Transparent
                ),
                modifier = Modifier.width(200.dp)
            ){
                Text(
                    text = "Registrar",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp
                )
            }
            if(vm.signedIn.value){
                navController.navigate(DestinationScreen.Success.route)
            }
            vm.signedIn.value = false

        }
    }
}