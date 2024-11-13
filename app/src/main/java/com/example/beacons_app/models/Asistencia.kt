package com.example.beacons_app.models

data class Asistencia(
    val asistencia_id: Int = 0,
    val beacons_id: Int = 0,
    val modalidad_id: Int = 0,
    val horario_id: Int = 0,
    val estado: Boolean = false,
    val descripcion: String = ""
) {
}