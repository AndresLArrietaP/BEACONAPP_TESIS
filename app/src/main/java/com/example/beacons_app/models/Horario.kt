package com.example.beacons_app.models

data class Horario(
    val id_horario: Long = 0,
    val id_curso: Long = 0,
    val id_modalidad: Long = 0,
    val dia: String = "",
    val horas: List<Long> = emptyList(),
    val aula: String = "",
    val grupo: Long = 0
) {
    constructor() : this(0, 0, 0, "", emptyList(), "", 0)
}



