package com.example.beacons_app

import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat.MessagingStyle.Message
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FbViewModel @Inject constructor(
    val auth:FirebaseAuth
): ViewModel(){
    val signedIn = mutableStateOf(false)
    val inProgress = mutableStateOf(false)
    val popupNotification = mutableStateOf<Event<String>?>(null)

    fun onSingup(email:String,pass:String){
        inProgress.value = true
        auth.createUserWithEmailAndPassword(email,pass)
            .addOnCompleteListener{
                if(it.isSuccessful){
                    signedIn.value = true
                    handleException(it.exception, "Registro exitoso")
                }else{
                    handleException(it.exception, "Registro fallido")
                }
                inProgress.value = false
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