package com.example.beacons_app

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.beacons_app.models.Usuario
import com.example.beacons_app.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FbViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase
) : ViewModel() {

    companion object {
        private const val TAG = "FbViewModel"
    }

    // Estado de sesión iniciada
    val signedIn = mutableStateOf(false)
    // Estado de operación en progreso (ej. mostrando indicador de carga durante autenticación)
    val inProgress = mutableStateOf(false)
    // Evento de notificación para mostrar mensajes (éxito/error) a la IU
    val popupNotification = mutableStateOf<Event<String>?>(null)

    // Referencia a la raíz de la base de datos en tiempo real
    private val databaseRef = database.reference

    /**
     * Registra un nuevo usuario con correo y contraseña, luego guarda la información adicional del [usuario] en la base de datos.
     */
    fun onSignup(email: String, pass: String, usuario: Usuario) {
        inProgress.value = true
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    // Registro exitoso
                    signedIn.value = true
                    val userId = auth.currentUser?.uid ?: ""
                    // Obtener el próximo ID de usuario disponible y guardar datos en "users"
                    getNextUserId { nextId ->
                        val newUser = usuario.copy(usuario_id = nextId)
                        saveUserToDatabase(userId, newUser)
                    }
                    handleException(it.exception, "Registro exitoso")
                } else {
                    // Registro fallido
                    handleException(it.exception, "Registro fallido")
                }
                inProgress.value = false
            }
    }

    /**
     * Calcula el siguiente identificador de usuario (usuario_id) de forma secuencial consultando la base de datos.
     */
    private fun getNextUserId(callback: (Int) -> Unit) {
        databaseRef.child("users").orderByChild("usuario_id").limitToLast(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Tomar el mayor usuario_id existente y sumar 1
                        val lastId = snapshot.children.mapNotNull {
                            it.child("usuario_id").getValue(Int::class.java)
                        }.maxOrNull() ?: 0
                        callback(lastId + 1)
                    } else {
                        // Si no existe ningún usuario aún, comenzar IDs desde 1
                        callback(1)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    // En caso de error en la consulta, devolver 1 por defecto
                    callback(1)
                }
            })
    }

    /**
     * Almacena el objeto [usuario] bajo el ID de Firebase [userId] en la rama "users" de la base de datos.
     */
    fun saveUserToDatabase(userId: String, usuario: Usuario) {
        databaseRef.child("users").child(userId).setValue(usuario)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    popupNotification.value = Event("Usuario guardado exitosamente")
                } else {
                    handleException(it.exception, "Error al guardar usuario")
                }
            }
    }

    /**
     * Inicia sesión con el correo y contraseña proporcionados. Si tiene éxito, obtiene el objeto [Usuario] desde la base de datos y lo retorna vía [callback].
     */
    fun login(email: String, pass: String, callback: (Usuario?) -> Unit) {
        inProgress.value = true
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    signedIn.value = true
                    val userId = auth.currentUser?.uid ?: ""
                    // Leer datos del usuario desde "users/{userId}"
                    databaseRef.child("users").child(userId).get()
                        .addOnSuccessListener { snapshot ->
                            val usuario = snapshot.getValue(Usuario::class.java)
                            callback(usuario)
                        }
                        .addOnFailureListener {
                            callback(null)
                        }
                    handleException(task.exception, "Inicio exitoso")
                } else {
                    handleException(task.exception, "Inicio fallido")
                    callback(null)
                }
                inProgress.value = false
            }
    }

    /**
     * Cierra la sesión actual de Firebase.
     */
    fun logout() {
        auth.signOut()
        signedIn.value = false
    }

    /**
     * Maneja excepciones de Firebase u otras operaciones, registrando el mensaje de error y actualizando [popupNotification] con un mensaje para la IU.
     * Si se proporciona [customMessage], se mostrará junto con el mensaje de error para mayor claridad.
     */
    fun handleException(exception: Exception? = null, customMessage: String = "") {
        exception?.printStackTrace()
        val errorMsg = exception?.localizedMessage ?: ""
        val message = if (customMessage.isEmpty()) errorMsg else "$customMessage: $errorMsg"
        popupNotification.value = Event(message)
        if (message.isNotEmpty()) {
            Log.d(TAG, "PopupNotification: $message")
        }
    }
}

