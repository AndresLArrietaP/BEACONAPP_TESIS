package com.example.beacons_app.models

data class AsistenciaUsuario (
    val usuario: Usuario ,
    val asistencia: Asistencia,
    val hora_registro: String = "",
    val fecha_registro: String = ""
) {
}