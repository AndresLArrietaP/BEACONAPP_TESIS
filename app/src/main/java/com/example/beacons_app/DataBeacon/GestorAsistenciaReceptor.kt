package com.example.beacons_app.DataBeacon

import com.example.beacons_app.models.AsistenciaUsuario
import com.example.beacons_app.util.Recurso
import kotlinx.coroutines.flow.MutableSharedFlow

interface GestorAsistenciaReceptor {
    val data: MutableSharedFlow<Recurso<AsistenciaUsuario>>
    fun reconnect()
    fun disconnect()
    fun startReceiving()
    fun closeConnection()
}
