package com.example.beacons_app.models

data class Horario(
    val horario_id: Int = 0,
    val modalidad_id: Int = 0,
    val nombre_curso: String = "",
    val creditos: Int = 0,
    val grupo: String = "",
    val dia: String = "",
    val aula: String = ""
) {
}