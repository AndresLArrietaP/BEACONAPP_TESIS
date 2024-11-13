package com.example.beacons_app.models

data class HorarioUsuario(
    val horario: Horario,
    val usuario: Usuario,
    val estado: Boolean = false
) {
}