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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.example.beacons_app.models.Usuario
import kotlinx.coroutines.delay
import org.w3c.dom.Text

/*@Composable
fun SignupScreen(navController: NavController, vm: FbViewModel){
    val emty by remember{ mutableStateOf("") }
    var email by remember{ mutableStateOf("") }
    var password by remember{ mutableStateOf("") }
    var cpassword by remember{ mutableStateOf("") }
    var nombres by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var cu by remember { mutableStateOf("") }
    var nrotelefonico by remember { mutableStateOf("") }
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
            .padding(top = 50.dp)
            .verticalScroll(
                rememberScrollState()
            )
    ){
        Text(
            text = "Registro de usuario",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp
        )

        Spacer(modifier = Modifier.height(30.dp))

        TextField(
            value = nombres,
            onValueChange = { nombres = it },
            label = { Text(text = "Nombres") },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_person_24),
                    contentDescription = null
                )
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
                unfocusedLabelColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(25.dp))

        TextField(
            value = apellidos,
            onValueChange = { apellidos = it },
            label = { Text(text = "Apellidos") },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_person_24),
                    contentDescription = null
                )
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
                unfocusedLabelColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(25.dp))

        TextField(
            value = cu,
            onValueChange = { cu = it },
            label = { Text(text = "Código institucional") },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_person_24),
                    contentDescription = null
                )
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Phone
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
                unfocusedLabelColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(25.dp))

        TextField(
            value = nrotelefonico,
            onValueChange = { nrotelefonico = it },
            label = { Text(text = "Número Telefónico") },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_person_24),
                    contentDescription = null
                )
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Phone
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
                unfocusedLabelColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(25.dp))

        /*if(erroeE){
            Text(
                text = "Ingresa correo",
                color = Color.Red,
                modifier = Modifier.padding(end=100.dp)
            )
        }*/

        if (email.isEmpty()) {
            erroeE = true
        } else if (!email.contains("@") || !email.endsWith(".edu.pe")) {
            erroeE = true
        } else {
            erroeE = false
        }

        if (erroeE) {
            Text(
                text = "Correo no válido o vacío",
                color = Color.Red,
                modifier = Modifier.padding(end = 100.dp)
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

        Spacer(modifier = Modifier.height(25.dp))

        if(erroeP){
            Text(
                text = "Ingresa contraseña",
                color = Color.White
                //modifier = Modifier.padding(end=100.dp)
            )
        }
        if(plength){
            Text(
                text = "Contraseña debe tener 6 caractéres",
                color = Color.Red,
                //modifier = Modifier.padding(end=100.dp)
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

        Spacer(modifier = Modifier.height(25.dp))

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

        Spacer(modifier = Modifier.height(25.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF4CFF00), Color.White, Color(0xFF44FF00))
                    )
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
                                    val usuario = Usuario(
                                        u_nombres = nombres,
                                        u_apellidos = apellidos,
                                        cu = cu,
                                        u_contrasena = password,
                                        u_nrotelefonico = nrotelefonico
                                    )
                                    vm.onSingup(email,password,usuario)
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
            /*if(vm.signedIn.value){
                navController.navigate(DestinationScreen.Success.route)
            }
            vm.signedIn.value = false*/

            if (vm.signedIn.value) {
                LaunchedEffect(Unit) {
                    navController.navigate(DestinationScreen.Success.route) {
                        popUpTo(DestinationScreen.Signup.route) { inclusive = true }
                    }
                    delay(5000) // Adjusted delay to 5 seconds for testing
                    navController.navigate(DestinationScreen.Main.route) {
                        popUpTo(DestinationScreen.Success.route) { inclusive = true }
                    }
                }
                vm.signedIn.value = false
            }

        }
    }
}*/

@Composable
fun SignupScreen(navController: NavController, vm: FbViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var nombres by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var cu by remember { mutableStateOf("") }
    var nrotelefonico by remember { mutableStateOf("") }

    // Control de visibilidad de contraseñas
    var passwordVisibility by remember { mutableStateOf(false) }
    var confirmPasswordVisibility by remember { mutableStateOf(false) }

    // Estados para errores
    var isNombreValid by remember { mutableStateOf(true) }
    var isApellidosValid by remember { mutableStateOf(true) }
    var isCodigoValid by remember { mutableStateOf(true) }
    var isNumeroValid by remember { mutableStateOf(true) }
    var isEmailValid by remember { mutableStateOf(true) }
    var isPasswordValid by remember { mutableStateOf(true) }
    var isConfirmPasswordValid by remember { mutableStateOf(true) }

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
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 50.dp)
            .verticalScroll(
                rememberScrollState()
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Título
        Text(
            text = "Registro de usuario",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Campo para Nombres
        CustomTextField(
            value = nombres,
            label = "Nombres",
            onValueChange = {
                nombres = it
                isNombreValid = nombres.isNotEmpty()},
            leadingIcon = R.drawable.baseline_person_24
        )

        // Reservar espacio para el mensaje de error del nombre
        Box(modifier = Modifier.height(20.dp)) {
            if (!isNombreValid) {
                Text(
                    text = "Ingrese nombre",
                    color = Color.Red,
                    fontSize = 12.sp
                )
            }
        }

        // Campo para Apellidos
        CustomTextField(
            value = apellidos,
            label = "Apellidos",
            onValueChange = {
                apellidos = it
                isApellidosValid = apellidos.isNotEmpty()},
            leadingIcon = R.drawable.baseline_person_24
        )

        // Reservar espacio para el mensaje de error de apellidos
        Box(modifier = Modifier.height(20.dp)) {
            if (!isApellidosValid) {
                Text(
                    text = "ingrese apellidos",
                    color = Color.Red,
                    fontSize = 12.sp
                )
            }
        }

        // Campo para Código
        CustomTextField(
            value = cu,
            label = "Código institucional",
            onValueChange = {
                cu = it
                isCodigoValid = cu.isNotEmpty()},
            leadingIcon = R.drawable.baseline_person_24
        )

        // Reservar espacio para el mensaje de error del codigo
        Box(modifier = Modifier.height(20.dp)) {
            if (!isCodigoValid) {
                Text(
                    text = "Código no válido o vacío",
                    color = Color.Red,
                    fontSize = 12.sp
                )
            }
        }

        // Campo para Telefono
        CustomTextField(
            value = nrotelefonico,
            label = "Número Telefónico",
            onValueChange = { input ->
                // Filtra solo dígitos y limita a 9 caracteres
                val filteredInput = input.filter { it.isDigit() }.take(9)
                nrotelefonico = filteredInput
                isNumeroValid = filteredInput.length == 9 // Verifica que tenga exactamente 9 dígitos
            },
            leadingIcon = R.drawable.baseline_person_24
        )

        // Reservar espacio para el mensaje de error del número
        Box(modifier = Modifier.height(20.dp)) {
            if (!isNumeroValid) {
                Text(
                    text = "Número no válido",
                    color = Color.Red,
                    fontSize = 12.sp
                )
            }
        }


        // Campo para Correo Electrónico
        CustomTextField(
            value = email,
            label = "Correo",
            onValueChange = {
                email = it
                isEmailValid = email.isNotEmpty() && email.contains("@") && email.endsWith(".edu.pe")
            },
            leadingIcon = R.drawable.baseline_person_24
        )

        // Reservar espacio para el mensaje de error del correo
        Box(modifier = Modifier.height(20.dp)) {
            if (!isEmailValid) {
                Text(
                    text = "Correo no válido o vacío",
                    color = Color.Red,
                    fontSize = 12.sp
                )
            }
        }

        //Spacer(modifier = Modifier.height(20.dp))

        // Campo para Contraseña
        CustomPasswordField(
            value = password,
            label = "Contraseña",
            onValueChange = {
                password = it
                isPasswordValid = password.length >= 6
            },
            visibility = passwordVisibility,
            onVisibilityChange = { passwordVisibility = !passwordVisibility }
        )

        // Reservar espacio para el mensaje de error de la contraseña
        Box(modifier = Modifier.height(20.dp)) {
            if (!isPasswordValid) {
                Text(
                    text = "Contraseña debe tener al menos 6 caracteres",
                    color = Color.Red,
                    fontSize = 12.sp
                )
            }
        }

        //Spacer(modifier = Modifier.height(20.dp))

        // Campo para Confirmar Contraseña
        CustomPasswordField(
            value = confirmPassword,
            label = "Confirmar contraseña",
            onValueChange = {
                confirmPassword = it
                isConfirmPasswordValid = confirmPassword == password
            },
            visibility = confirmPasswordVisibility,
            onVisibilityChange = { confirmPasswordVisibility = !confirmPasswordVisibility }
        )

        // Reservar espacio para el mensaje de error de confirmación de contraseña
        Box(modifier = Modifier.height(20.dp)) {
            if (!isConfirmPasswordValid) {
                Text(
                    text = "La confirmación de la contraseña no coincide",
                    color = Color.Red,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(15.dp))

        // Botón de Registro
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(40.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF4CFF00), Color.White, Color(0xFF44FF00))
                    )
                )
        ) {
            Button(
                onClick = {
                    /*
                    isEmailValid = email.isNotEmpty() && email.contains("@") && email.endsWith(".edu.pe")
                    isPasswordValid = password.isNotEmpty() && password.length >= 6
                    isConfirmPasswordValid = confirmPassword == password*/

                    // Si todos los campos son válidos, se intenta el registro
                    if (isNombreValid &&
                        isApellidosValid &&
                        isNumeroValid &&
                        isCodigoValid &&
                        isEmailValid &&
                        isPasswordValid &&
                        isConfirmPasswordValid) {

                        val usuario = Usuario(
                            u_nombres = nombres,
                            u_apellidos = apellidos,
                            cu = cu,
                            u_contrasena = password,
                            u_nrotelefonico = nrotelefonico
                        )
                        vm.onSingup(email, password, usuario)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.width(200.dp)
            ) {
                Text(
                    text = "Registrar",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp
                )
            }

            // Control de la navegación tras el registro exitoso
            if (vm.signedIn.value) {
                LaunchedEffect(Unit) {
                    navController.navigate(DestinationScreen.Success.route) {
                        popUpTo(DestinationScreen.Signup.route) { inclusive = true }
                    }
                    delay(5000) // Espera de 5 segundos
                    navController.navigate(DestinationScreen.Main.route) {
                        popUpTo(DestinationScreen.Success.route) { inclusive = true }
                    }
                }
                vm.signedIn.value = false
            }
        }
    }
}

@Composable
fun CustomTextField(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    leadingIcon: Int,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label) },
        leadingIcon = {
            Icon(
                painter = painterResource(id = leadingIcon),
                contentDescription = null
            )
        },
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
            unfocusedLabelColor = Color.White
        )
    )
}

@Composable
fun CustomPasswordField(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    visibility: Boolean,
    onVisibilityChange: () -> Unit
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label) },
        leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.baseline_person_24),
                contentDescription = null
            )
        },
        trailingIcon = {
            Icon(
                painter = painterResource(
                    if (visibility) R.drawable.baseline_person_24 else R.drawable.baseline_person_24
                ),
                contentDescription = if (visibility) "Hide Password" else "Show Password",
                Modifier.clickable { onVisibilityChange() }
            )
        },
        visualTransformation = if (visibility) VisualTransformation.None else PasswordVisualTransformation(),
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
            unfocusedLabelColor = Color.White
        )
    )
}