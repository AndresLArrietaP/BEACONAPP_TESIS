package com.example.beacons_app

import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat.MessagingStyle.Message
import androidx.lifecycle.ViewModel
import com.example.beacons_app.models.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FbViewModel @Inject constructor(
    val auth:FirebaseAuth
): ViewModel(){
    val signedIn = mutableStateOf(false)
    val inProgress = mutableStateOf(false)
    val popupNotification = mutableStateOf<Event<String>?>(null)
    val database = Firebase.database.reference

    /*fun onSingup(email:String,pass:String){
        inProgress.value = true
        auth.createUserWithEmailAndPassword(email,pass)
            .addOnCompleteListener{
                if(it.isSuccessful){
                    signedIn.value = true
                    //val userId = auth.currentUser?.uid ?: ""
                    //saveUserToDatabase(userId, usuario)
                    handleException(it.exception, "Registro exitoso")
                }else{
                    handleException(it.exception, "Registro fallido")
                }
                inProgress.value = false
            }
    }*/

    fun onSingup(email: String, pass: String, usuario: Usuario) {
        inProgress.value = true
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    signedIn.value = true
                    val userId = auth.currentUser?.uid ?: ""
                    //saveUserToDatabase(userId, usuario)
                    getNextUserId { nextId ->
                        val newUser = usuario.copy(usuario_id = nextId)
                        saveUserToDatabase(userId, newUser)
                    }
                    handleException(it.exception, "Registro exitoso")
                } else {
                    handleException(it.exception, "Registro fallido")
                }
                inProgress.value = false
            }
    }

    /*private fun getNextUserId(callback: (Int) -> Unit) {
        database.child("users").orderByChild("usuario_id").limitToLast(1).get()
            .addOnSuccessListener { snapshot ->
                val lastId = snapshot.children.mapNotNull { it.child("usuario_id").getValue(Int::class.java) }.maxOrNull() ?: 0
                callback(lastId + 1)
            }
            .addOnFailureListener {
                callback(1) // Default to 1 if there's an error
            }
    }*/

    private fun getNextUserId(callback: (Int) -> Unit) {
        database.child("users").orderByChild("usuario_id").limitToLast(1).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lastId = snapshot.children.mapNotNull { it.child("usuario_id").getValue(Int::class.java) }.maxOrNull() ?: 0
                callback(lastId + 1)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(1) // Default to 1 if there's an error
            }
        })
    }
    fun saveUserToDatabase(userId: String, usuario: Usuario) {
        database.child("users").child(userId).setValue(usuario)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    popupNotification.value = Event("Usuario guardado exitosamente")
                } else {
                    handleException(it.exception, "Error al guardar usuario")
                }
            }
    }

    fun login(email:String,pass:String){
        inProgress.value = true
        auth.signInWithEmailAndPassword(email,pass)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    signedIn.value = true
                    handleException(it.exception, "Inicio exitoso")
                }else{
                    handleException(it.exception, "Inicio fallido")
                }
                inProgress.value = false
            }
    }

    fun handleException(exception:Exception? = null,customMessage: String = ""){
        exception?.printStackTrace()
        val errorMsg = exception?.localizedMessage ?: ""
        val message = if(customMessage.isEmpty()) errorMsg else "$customMessage: $errorMsg"
        popupNotification.value = Event(message)
    }
}