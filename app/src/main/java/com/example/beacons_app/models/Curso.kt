package com.example.beacons_app.models

data class Curso(
    val id_curso: Long = 0,
    val nombre: String = "",
    val ciclo: Int = 0,
    val creditaje: Int = 0,
    val grupos: Int = 0,
    val codigo: String = ""
) {
    constructor() : this(0, "", 0, 0, 0, "")
}
