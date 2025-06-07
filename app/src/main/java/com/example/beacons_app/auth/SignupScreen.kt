package com.example.beacons_app.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.beacons_app.DestinationScreen
import com.example.beacons_app.FbViewModel
import com.example.beacons_app.R
import com.example.beacons_app.models.Usuario
import kotlinx.coroutines.delay

@Composable
fun SignupScreen(navController: NavController, vm: FbViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var nombres by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var cu by remember { mutableStateOf("") }
    var nrotelefonico by remember { mutableStateOf("") }

    var passwordVisibility by remember { mutableStateOf(false) }
    var confirmPasswordVisibility by remember { mutableStateOf(false) }

    var isNombreValid by remember { mutableStateOf(true) }
    var isApellidosValid by remember { mutableStateOf(true) }
    var isCodigoValid by remember { mutableStateOf(true) }
    var isNumeroValid by remember { mutableStateOf(true) }
    var isEmailValid by remember { mutableStateOf(true) }
    var isPasswordValid by remember { mutableStateOf(true) }
    var isConfirmPasswordValid by remember { mutableStateOf(true) }

    var selectedTipo by remember { mutableStateOf("") }
    val tipos = listOf("Docente", "Administrativo", "Desarrollador")
    var expanded by remember { mutableStateOf(false) }
    var isTipoValid by remember { mutableStateOf(true) }

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
    ) {
        if (vm.inProgress.value) {
            CircularProgressIndicator()
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 50.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Registro de usuario",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp
        )

        Spacer(modifier = Modifier.height(30.dp))

        CustomTextField(
            value = nombres,
            label = "Nombres",
            onValueChange = {
                nombres = it
                isNombreValid = nombres.isNotEmpty()
            },
            leadingIcon = R.drawable.baseline_person_24
        )
        Box(modifier = Modifier.height(20.dp)) {
            if (!isNombreValid) {
                Text("Ingrese nombre", color = Color.Red, fontSize = 12.sp)
            }
        }

        CustomTextField(
            value = apellidos,
            label = "Apellidos",
            onValueChange = {
                apellidos = it
                isApellidosValid = apellidos.isNotEmpty()
            },
            leadingIcon = R.drawable.baseline_person_24
        )
        Box(modifier = Modifier.height(20.dp)) {
            if (!isApellidosValid) {
                Text("Ingrese apellidos", color = Color.Red, fontSize = 12.sp)
            }
        }

        CustomTextField(
            value = cu,
            label = "Código institucional",
            onValueChange = {
                cu = it
                isCodigoValid = cu.isNotEmpty()
            },
            leadingIcon = R.drawable.baseline_person_24
        )
        Box(modifier = Modifier.height(20.dp)) {
            if (!isCodigoValid) {
                Text("Código no válido o vacío", color = Color.Red, fontSize = 12.sp)
            }
        }

        CustomTextField(
            value = nrotelefonico,
            label = "Número Telefónico",
            onValueChange = { input ->
                val filteredInput = input.filter { it.isDigit() }.take(9)
                nrotelefonico = filteredInput
                isNumeroValid = filteredInput.length == 9
            },
            leadingIcon = R.drawable.baseline_person_24
        )
        Box(modifier = Modifier.height(20.dp)) {
            if (!isNumeroValid) {
                Text("Número no válido", color = Color.Red, fontSize = 12.sp)
            }
        }

        // ComboBox con tu estilo
        CustomComboBoxField(
            selectedValue = selectedTipo,
            options = tipos,
            onValueSelected = {
                selectedTipo = it
                isTipoValid = true
            },
            isValid = isTipoValid,
            expanded = expanded,
            onDropdownExpandedChange = { expanded = it }
        )
        Spacer(modifier = Modifier.height(10.dp))
        Box(modifier = Modifier.height(20.dp)) {
            if (!isTipoValid) {
                Text("Seleccione un tipo de usuario", color = Color.Red, fontSize = 12.sp)
            }
        }

        CustomTextField(
            value = email,
            label = "Correo",
            onValueChange = {
                email = it
                isEmailValid = email.isNotEmpty() && email.contains("@") && email.endsWith(".edu.pe")
            },
            leadingIcon = R.drawable.baseline_person_24
        )
        Box(modifier = Modifier.height(20.dp)) {
            if (!isEmailValid) {
                Text("Correo no válido o vacío", color = Color.Red, fontSize = 12.sp)
            }
        }

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
        Box(modifier = Modifier.height(20.dp)) {
            if (!isPasswordValid) {
                Text("Contraseña debe tener al menos 6 caracteres", color = Color.Red, fontSize = 12.sp)
            }
        }

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
        Box(modifier = Modifier.height(20.dp)) {
            if (!isConfirmPasswordValid) {
                Text("La confirmación de la contraseña no coincide", color = Color.Red, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(15.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                        if (isNombreValid && isApellidosValid && isNumeroValid &&
                            isCodigoValid && isEmailValid && isPasswordValid &&
                            isConfirmPasswordValid && isTipoValid
                        ) {
                            val usuario = Usuario(
                                u_nombres = nombres,
                                u_apellidos = apellidos,
                                cu = cu,
                                u_contrasena = password,
                                u_nrotelefonico = nrotelefonico,
                                u_tipo = selectedTipo
                            )
                            vm.onSignup(email, password, usuario)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    modifier = Modifier.width(150.dp)
                ) {
                    Text("Registrar", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }

                if (vm.signedIn.value) {
                    LaunchedEffect(Unit) {
                        navController.navigate(DestinationScreen.Login.route) {
                            popUpTo(DestinationScreen.Signup.route) { inclusive = true }
                        }
                    }
                    vm.signedIn.value = false
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color.White)
            ) {
                Button(
                    onClick = {
                        navController.navigate(DestinationScreen.Login.route) {
                            popUpTo(DestinationScreen.Signup.route) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(Color.Transparent),
                    modifier = Modifier
                        .width(100.dp)
                        .padding(10.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.baseline_person_24),
                        contentDescription = "Volver",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CustomComboBoxField(
    selectedValue: String,
    options: List<String>,
    onValueSelected: (String) -> Unit,
    isValid: Boolean,
    onDropdownExpandedChange: (Boolean) -> Unit,
    expanded: Boolean
) {
    Box(
        modifier = Modifier
            .width(300.dp)
            .height(60.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(if (isValid) Color(0x30FFFFFF) else Color(0x30FF0000))
            .clickable { onDropdownExpandedChange(true) }
            .padding(horizontal = 16.dp, vertical = 18.dp)
    ) {
        Text(
            text = if (selectedValue.isEmpty()) "Seleccione tipo de usuario" else selectedValue,
            color = if (selectedValue.isEmpty()) Color.Gray else Color.White,
            fontSize = 18.sp
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onDropdownExpandedChange(false) }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueSelected(option)
                        onDropdownExpandedChange(false)
                    }
                )
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