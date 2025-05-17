package com.example.beacons_app.main

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.platform.LocalContext
import com.example.beacons_app.Event

/**
 * Muestra un mensaje emergente (Toast) al usuario si hay contenido en el evento de notificación.
 * Consume el contenido del [notificationState] para que cada mensaje solo se muestre una vez.
 */
@Composable
fun NotificationMessage(notificationState: State<Event<String>?>) {
    // Extraer el mensaje de notificación (si no ha sido manejado previamente)
    val notifMessage = notificationState.value?.getContentOrNull()
    if (notifMessage != null) {
        // Mostrar el mensaje en un Toast corto
        Toast.makeText(LocalContext.current, notifMessage, Toast.LENGTH_SHORT).show()
    }
}


